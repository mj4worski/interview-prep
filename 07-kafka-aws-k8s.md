# Kafka + AWS + Kubernetes — Pareto pod interview

Mów 60–90 s: co to jest → kiedy użyć → co się psuje.

---

## Kafka

### Model w 40 sekund

Topic = log. Partition = uporządkowany, append-only fragment. Offset = pozycja konsumenta.
**Kolejność jest per partycja, nie per topic.** Ten sam key → ta sama partycja → kolejność dla agregatu (orderId, userId).

Producer pisze. Consumer group: każda partycja do **jednego** konsumenta w grupie. 12 partycji + 12 konsumentów = max parallelism. 13. konsument siedzi bez roboty.

### Delivery

| Semantyka | Co znaczy | Jak |
|-----------|-----------|-----|
| At-most-once | możesz zgubić, nie zduplikujesz | commit przed processing |
| At-least-once | **default w praktyce** — duplikaty będą | process, potem commit |
| Exactly-once | marketingowo wąskie | idempotent producer + transakcje + idempotent consumer |

Zdanie na rozmowę: *„Zakładam at-least-once. Idempotencja po stronie konsumenta (klucz biznesowy / inbox table). Kafka EOS nie zwalnia mnie z tego na granicy z DB.”*

**Idempotent producer** (`enable.idempotence=true`): chroni przed duplikatem **w tej samej partycji przy retry producenta**. Nie chroni przed „user kliknął dwa razy” i nie przed dual-write do DB.

**Transakcje Kafka:** atomowy zapis do kilku partycji + commit offsetu. Nie robią atomowo „DB + Kafka”. Stąd **outbox**.

### Outbox (musisz narysować)

1. W jednej TX: biznes + wiersz `outbox(id, payload, published=false)`
2. Worker / Debezium czyta outbox i publikuje
3. Konsument idempotentny

Bez tego: zapisałeś zamówienie, Kafka padła, event nie poszedł — albo odwrotnie.

### Konsument — rzeczy, które wychodzą

- **Commit offset** po sukcesie. Auto-commit + wyjątek w połowie batcha = dziura albo duplikat. Na rozmowie: manual commit, batch świadomie.
- **Lag** = latest offset − committed. Alert na lag, nie na „czy żyje”.
- **Rebalance:** nowy konsument / crash / `max.poll.interval` przekroczony → pauza processing. Długie przetwarzanie w `poll` = rebalance storm. Fix: krótsza praca, pause/resume, cooperative rebalance, zwiększ interval świadomie.
- **Poison pill:** zły rekord blokuje partycję (kolejność!). Retry N razy → DLQ + alert. Świadomy skip psuje kolejność — powiedz to.
- **Retry:** osobny topic retry z backoff, nie `sleep` w konsumencie na 5 min.

### Retention, compaction

- Delete + retention (np. 7 dni) — log eventów
- Compaction — ostatnia wartość per key (`userId → profil`). Changelog, nie audit 1:1
- Tombstone (null value) kasuje key przy compaction

### Kiedy Kafka, kiedy nie

Tak: event stream, fan-out, replay, decoupling write path, CDC.
Nie: request-response synchroniczne, kolejka zadań z prostym retry (często SQS wystarczy), „bo microservices”.

**SQS vs Kafka:** SQS = managed, proste, competing consumers, brak replay po usunięciu, brak twardej kolejności globalnej (FIFO ograniczone). Kafka = retention, replay, partycje, wyższy koszt operacyjny.

### Design kotwica: order / notification

```
API → DB + outbox → Kafka topic orders
         ├─ inventory consumer (idempotent, key=orderId)
         ├─ notification consumer (wolno gubić więcej)
         └─ search indexer
```

Priority: osobny topic albo osobna grupa, nie „priority field i nadzieja”.
Backpressure: lag rośnie → shed, nie dopisuj partycji w panic.

---

## AWS

### 60-sekundowa mapa

- **Compute:** Lambda (krótki IO, spike) / ECS Fargate (kontenery bez cluster ops) / EKS (gdy naprawdę potrzebujesz K8s) / EC2 (legacy, specjalne).
- **Edge:** CloudFront + S3. ALB (HTTP, path, WAF) vs NLB (TCP, stałe IP, bardzo niski overhead).
- **Data:** RDS Multi-AZ (failover, ta sama okolica zapisu) vs Read Replica (skala czytań, lag). ElastiCache Redis. Dynamo gdy access pattern znany i naprawdę trzeba.
- **Async:** SQS (praca), SNS (fan-out pub/sub), EventBridge (routing eventów), MSK/Kinesis (stream).
- **IAM:** role na task/pod, nie access key w env. Least privilege. Prefer instance/task role.

### S3 + upload (klasyk design)

Przeglądarka **nie** pcha 200 MB przez API.
1. API auth → `presigned PUT/POST` (krótki TTL, content-type, max size)
2. Client → S3
3. S3 event → SQS/Lambda → skan/antivirus/thumbnail
4. Metadata w RDS. Public URL tylko przez CloudFront, nie otwarty bucket.

### RDS, które musisz powiedzieć

- Multi-AZ: synchroniczny standby, failover, **nie** skaluje read.
- Replica: async, lag, raporty / read-your-writes problem.
- Connection pool (Hikari) + RDS Proxy przy dużej liczbie bursty Lambd.
- Backup + PITR. RPO/RTO na głos.

### Sieć i bezpieczeństwo (wystarczy szkic)

- Prywatne subnety na app i DB. Public tylko ALB / NAT.
- SG = stateful firewall przy ENI. Nie otwieraj 0.0.0.0/0 do RDS.
- Secrets Manager / SSM, nie ConfigMap z hasłem.
- Szyfrowanie at rest (RDS, S3) i TLS in transit.

### Obserwowalność

CloudWatch metrics + alarmy na p99, 5xx, saturację puli, consumer lag.
Logi strukturalne, correlation id. X-Ray / OTel gdy ciągną w tracing.

### Zdania-kotwice

- „Jeden region, 2–3 AZ, RDS Multi-AZ, app stateless za ALB.”
- „IAM task role, zero long-lived keys.”
- „S3 + presign, metadata w Postgres, event na przetwarzanie.”
- „EKS tylko gdy portability / ekosystem K8s jest wymaganiem. Inaczej ECS Fargate jest tańszy operacyjnie.”

---

## Kubernetes

### Obiekty, które wystarczą

| Obiekt | Rola |
|--------|------|
| Pod | najmniejsza jednostka, 1+ kontener, efemeryczny |
| Deployment | replica, rolling update |
| Service | stabilny DNS/IP wewnątrz klastra (ClusterIP / NLB) |
| Ingress / Gateway | HTTP z zewnątrz |
| ConfigMap / Secret | config vs sekrety (Secret to tylko base64, nie magia) |
| HPA | skala replica po CPU/custom metrics |

### Probes — to pytanie padnie

- **Liveness:** „zabić i postawić”. Zły liveness na zależność (DB down) = restart loop całego klastra.
- **Readiness:** „wypisz z Service”. DB down → nie zabijaj, przestań słać ruch.
- **Startup:** wolny start JVM, żeby liveness nie zabił podczas warmupu.

Przykład Spring: readiness = `/actuator/health/readiness` (DB). Liveness = `/actuator/health/liveness` (proces żyje, **bez** twardej zależności od DB).

### Resources

`requests` = ile scheduler rezerwuje. `limits` = strop (CPU throttle, memory OOMKill).
Bez requestów: noisy neighbor. Limit pamięci < realny heap+metaspace+direct = restart w pętli.
JVM: heap musi być świadomy cgroup (`-XX:+UseContainerSupport`, już default).

### Rolling update

`maxUnavailable` / `maxSurge`. Przy 2 replica i maxUnavailable=1 możesz zejść do 1.
Readiness musi być zielone zanim stary pod padnie, inaczej 502.
Migracje DB: expand/contract, nie „nowy kod + breaking schema w jednym deploy”.

### 12-factor na klastrze

- Stateless app. Sesja w Redis, nie w podzie.
- Jeden proces. Sidecar tylko świadomie (mesh, log).
- Graceful shutdown: `preStop` + Spring `server.shutdown=graceful` + `terminationGracePeriodSeconds`. ALB/K8s przestaje słać, kończysz requesty, potem kill.
- Nie przechowuj uploadów na dysku poda.

### Kafka na K8s — pułapka

Nie skaluj Deployment konsumentów powyżej liczby partycji. HPA na CPU konsumenta Kafka często jest złym sygnałem — lag jest lepszy (KEDA). Rebalance przy każdym scale.

### Zdania-kotwice

- „Liveness = proces, readiness = mogę brać ruch.”
- „Requests do schedulera, limits przeciwko sąsiadom, heap < limit.”
- „Rolling + readiness + graceful shutdown, inaczej drop podczas deploy.”
- „Dwa replica minimum za Service, PDB żeby nikt nie zabrał obu.”

---

## Grill 15 pytań (odpowiadaj na głos)

1. Dlaczego kolejność w Kafka nie jest globalna?
2. Co się stanie jak masz więcej konsumentów niż partycji?
3. Czym outbox różni się od „zapis i potem `kafkaTemplate.send`”?
4. At-least-once vs idempotent producer — co który problem rozwiązuje?
5. Co to consumer lag i jaki alert postawisz?
6. Poison pill — skip czy DLQ i dlaczego?
7. SQS czy Kafka do maili powitalnych? Uzasadnij.
8. Multi-AZ vs read replica — co ratuje failover, co skaluje read?
9. Po co presigned URL?
10. IAM user access key w podzie — czemu nie?
11. Liveness sprawdzający DB — co się stanie przy awarii RDS?
12. OOMKill poda Java — pierwsze 3 rzeczy które sprawdzisz?
13. HPA + Kafka consumer — jaki jest problem?
14. ECS Fargate vs EKS — kiedy które?
15. Deploy z breaking change kolumny — jak bez downtimu?
