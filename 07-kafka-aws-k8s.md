# Kafka + AWS + Kubernetes — Pareto pod interview

Mów 60–90 s: co to jest → kiedy użyć → co się psuje.

Pod spodem jest sekwencja, nie tylko hasło. Limity linków: `09-materialy.md`. Kafka 101 i artykuł Debezium o outboxie są na dzień 6. Dokumentacji brokera nie czytasz.

---

## Kafka

### Model w 40 sekund

Topic = log. Partition = uporządkowany, append-only fragment. Offset = pozycja konsumenta.
**Kolejność jest per partycja, nie per topic.** Ten sam key → ta sama partycja → kolejność dla agregatu (orderId, userId).

Producer pisze. Consumer group: każda partycja do **jednego** konsumenta w grupie. 12 partycji + 12 konsumentów = max parallelism. 13. konsument siedzi bez roboty.

### Co się dzieje przy zapisie i odczycie

Partycja ma lidera na jednym brokerze i repliki. Producer wysyła batch rekordów do lidera partycji wybranej kluczem (`hash(key) % liczba partycji`). Bez klucza rekordy rozkładają się po partycjach i **nie** ma kolejności między nimi.

`acks=0` — producer nie czeka. `acks=1` — czeka na lidera. Lider padnie, zanim replika nadąży, i zapis zniknie. `acks=all` — lider czeka na repliki z ISR. `min.insync.replicas=2` przy `acks=all` znaczy: commit, gdy zapis jest na liderze i jeszcze co najmniej jednej replice. To jest trwałość, której bronisz na rozmowie. Koszt to wyższe opóźnienie zapisu.

Idempotentny producer dokłada do rekordu identyfikator producenta i numer sekwencji **w tej partycji**. Broker odrzuca powtórzony numer, gdy retry po timeoutcie dotrze drugi raz. To łapie „sieć zgubiła ACK, producer wysłał jeszcze raz”. Nie łapie drugiego kliknięcia użytkownika (to jest drugi logiczny zapis, inna sekwencja) i nie łapie zapisu do bazy obok Kafki.

Konsument woła `poll`, dostaje batch, przetwarza, commituje offset do wewnętrznego topica `__consumer_offsets`. Offset to „następny rekord do czytania”, nie „ostatni przetworzony” w sensie biznesowym, dopóki sam tak nie zrobisz. Crash po przetworzeniu, przed commitem: po restarcie batch przyjdzie drugi raz. Stąd at-least-once jako tryb, w którym da się żyć, i idempotentny konsument.

Commit przed przetwarzaniem jest at-most-once: crash po commicie gubi rekordy. Na rozmowie mówisz, że tego nie wybierasz dla zamówień.

Grupa: jeden konsument dostaje wyłączność na partycję. Drugi konsument w tej samej grupie nie dostanie tej samej partycji. Dwie grupy na jednym topicu to dwa niezależne fan-outy (magazyn i powiadomienia), każda ma własny offset.

Więcej konsumentów niż partycji nic nie zrównolegla. Dorzucenie partycji na produkcji zmienia hash klucza i na chwilę psuje kolejność per klucz, dopóki nie ułożysz partycji od nowa. To nie jest pokrętło na incydent.

[Design Kafki](https://kafka.apache.org/documentation/#design), [semantyka dostarczenia](https://kafka.apache.org/documentation/#semantics), krótki moduł [Events](https://developer.confluent.io/courses/apache-kafka/events/). Ograniczenia EOS, nie slajd marketingowy: [exactly-once, Confluent](https://www.confluent.io/blog/exactly-once-semantics-are-possible-heres-how-apache-kafka-does-it/).

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

Oś czasu dual-write. `INSERT order` commitnął. `kafkaTemplate.send` rzuca timeout. Aplikacja zwraca 500. Klient ponawia. Drugi `INSERT` robi drugie zamówienie albo wpada w unique i nie wysyła eventu. W bazie jest zamówienie bez zdarzenia do magazynu. Odwrotna kolejność (najpierw Kafka) zostawia zdarzenie bez wiersza, gdy baza odrzuci commit.

Outbox zamyka to w jednym commicie bazy. Publisher jest osobno i wolno mu wysłać dwa razy: oznacza `published=true` po ACK, a jak padnie między ACK a UPDATE, wyśle wiersz ponownie. Konsument ma `inbox(event_id PRIMARY KEY)` albo robi skutek przez `UPDATE ... WHERE processed=false`. Drugi raz zmienia 0 wierszy.

Polling outboxa co 200 ms jest wystarczający na rozmowę i w wielu systemach. Debezium czyta WAL i jest szybsze, kosztem złącza. Mechanizm jest ten sam: źródłem zdarzenia jest commit bazy, nie wywołanie klienta Kafki w kodzie requestu.

[Wzorzec](https://microservices.io/patterns/data/transactional-outbox.html), [przejście z Debezium](https://debezium.io/blog/2019/02/19/reliable-microservices-data-exchange-with-the-outbox-pattern/).

### Konsument — rzeczy, które wychodzą

- **Commit offset** po sukcesie. Auto-commit + wyjątek w połowie batcha = dziura albo duplikat. Na rozmowie: manual commit, batch świadomie.
- **Lag** = latest offset − committed. Alert na lag, nie na „czy żyje”.
- **Rebalance:** nowy konsument / crash / `max.poll.interval` przekroczony → pauza processing. Długie przetwarzanie w `poll` = rebalance storm. Fix: krótsza praca, pause/resume, cooperative rebalance, zwiększ interval świadomie.
- **Poison pill:** zły rekord blokuje partycję (kolejność!). Retry N razy → DLQ + alert. Świadomy skip psuje kolejność — powiedz to.
- **Retry:** osobny topic retry z backoff, nie `sleep` w konsumencie na 5 min.

**Lag liczbowo.** Partycja ma latest offset 1000, commit grupy 900. Lag = 100. Alert na „proces żyje” jest zielony, a magazyn jest 100 zdarzeń do tyłu i rośnie. Próg ustawiasz w zdarzeniach albo w czasie (lag / tempo produkcji). Skalowanie konsumenta pomaga, dopóki masz wolne partycje.

**Rebalance.** W czasie rebalansu partycja jest na chwilę odbierana i przetwarzanie stoi. Heartbeat leci w wątku konsumenta. Jak `poll` nie wróci przed `max.poll.interval.ms`, broker uzna konsumenta za martwego i odbierze mu partycje, często w trakcie gdy ten nadal pracuje. Po powrocie znowu rebalance. Pętla z tego to rebalance storm. Przetwarzanie wynosi się poza ciasny `poll` (pause, ręczne wznawianie, krótsza praca), interval podnosisz świadomie, nie „żeby przestało krzyczeć”.

**Poison pill.** Jeden rekord rzuca przy deserializacji albo łamie invariant za każdym razem. Partycja stoi, bo nie chcesz przeskoczyć go w ciszy: kolejność za nim jest bez znaczenia, jeśli ten miał się wykonać. Po N próbach w topic DLQ, metryka, człowiek. Skip bez śladu gubi zdarzenie i mówisz o tym wprost, jeśli biznes na to pozwala (np. statystyka kliknięć, nie płatność). `sleep(5 min)` w konsumenta trzyma partycję i prowokuje rebalance.

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

### Ścieżka uploadu, po kolei

1. Klient woła Twoje API: „chcę wgrać `image/jpeg`, 20 MB”. API sprawdza auth, typ, rozmiar.
2. API woła S3 o URL podpisany kluczem, którego klient nie ma. W podpisie siedzi metoda, bucket, klucz obiektu, czas życia (minuty), content-type. Kto przerobi URL na inny typ albo inny bucket, dostanie odmowę.
3. Przeglądarka robi `PUT` prosto na S3. Twoje API nie przepycha 20 MB i nie trzyma połączenia.
4. S3 po zapisie wystawia zdarzenie (kolejka). Worker robi miniaturę, skan, zapis metadanych w RDS. Do tego momentu obiekt jest w buckecie prywatnym, a w bazie ma status `pending`.
5. Dopiero status `ready` daje klientowi URL przez CloudFront. Publiczny bucket z listy obiektów jest osobnym incydentem.

RDS Multi-AZ trzyma standby w innej strefie i przy awarii primary przepina DNS na niego. Zapis nadal idzie w jedno miejsce. Read replica to drugi serwer, na który leci strumień z opóźnieniem. Raport na replice jest OK. Odczyt „czy mój zapis już widać” na replice jest nie OK. [Presigned URL](https://docs.aws.amazon.com/AmazonS3/latest/userguide/using-presigned-url.html), [Multi-AZ](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/Concepts.MultiAZ.html).

Rola IAM jest dołączona do taska ECS albo do service accountu poda. SDK bierze krótko żyjące poświadczenia z metadanych. Klucz `AKIA...` w zmiennej środowiskowej żyje latami, kopiuje się do logów i do obrazu. [Role](https://docs.aws.amazon.com/IAM/latest/UserGuide/id_roles.html).

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

**Awaria RDS przy złym liveness.** Liveness woła `SELECT 1`. Baza pada. Kubelet uznaje kontener za martwy i restartuje go. Wszystkie pody robią to naraz. Baza wstaje i dostaje falę startów JVM, pul połączeń i migracji. Znowu pada. Readiness przy tej samej awarii wypina pody z Service. Proces żyje, pula nie otwiera nowych połączeń w panice, ruch dostaje 503 z góry albo idzie w stronę, która jeszcze ma bazę. Różnica jest w tym, czy zabijasz proces.

Startup probe daje JVM czas na start, zanim liveness zacznie liczyć porażki. Bez tego wolny start wygląda jak deadlock i kubelet zabija poda w kółko. [Probes](https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle/#container-probes), [Spring](https://spring.io/blog/2020/03/25/liveness-and-readiness-probes-with-spring-boot).

### Resources

`requests` = ile scheduler rezerwuje. `limits` = strop (CPU throttle, memory OOMKill).
Bez requestów: noisy neighbor. Limit pamięci < realny heap+metaspace+direct = restart w pętli.
JVM: heap musi być świadomy cgroup (`-XX:+UseContainerSupport`, już default).

Liczby, które mówisz na głos. Kontener ma limit pamięci 1 GB. Heap `-Xmx768m` plus metaspace, wątki, bufory bezpośrednie Netty i nic na resztę. OOMKill przychodzi od kubeleta, nie jako `OutOfMemoryError` z Javy. W logu poda jest `OOMKilled`, a w aplikacji cisza. Najpierw: limit vs rzeczywisty heap, potem dump, potem „czy limit w ogóle był”. Samo podniesienie limitu bez liczby jest zgadywaniem.

Requests 100m CPU przy limicie 2 CPU: scheduler pakuje gęsto, a w ruchu pod dostaje throttle do 2. Brak requestów: scheduler myśli, że pod nic nie je, i kładzie go na węźle, który już jest pełny. [Requests i limits](https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/).

Rolling: nowy pod dostaje ruch dopiero po readiness. `preStop` usypia kilka sekund, żeby endpoint zdążył wypaść z EndpointSlice, zanim proces zacznie odrzucać połączenia. `server.shutdown=graceful` kończy żądania, które już weszły. `terminationGracePeriodSeconds` musi być dłuższe niż ten drain. Krótsze ucina requesty w połowie i na deployu widać 502. [Rolling update](https://kubernetes.io/docs/tutorials/kubernetes-basics/update/update-intro/).

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
