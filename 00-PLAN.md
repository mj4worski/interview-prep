# Plan 14 dni — Java 17/21, Spring Boot, Kafka, AWS, K8s

Profil: 11 lat, 6 lat w jednej firmie, senior backend.
Cel: rdza z live codingu, prowadzenie system designu, stack z ogłoszenia na poziomie rozmowy — nie CKA i nie certyfikat AWS.

Założenie czasu: **2–3 h w dzień roboczy, 4 h w weekend**. Razem ~35 h. Wystarczy. Więcej godzin nie pomaga, jeśli nie mówisz na głos.

Kolejność stała:

```
Coding → System design → Spring/SQL → Kafka/AWS/K8s → Behavioral → Mocks
```

---

## Kalendarz

### Tydzień 1 — mięsień i rama

| Dzień | Blok A (główny) | Blok B (45–60 min) | Done gdy |
|-------|-----------------|--------------------|----------|
| **1** | Coding: wzorce 1–4 + P01, P02 na czas | Java 17/21 ściąga (`08`) | 2 zadania + recap na głos |
| **2** | Coding: wzorce 5–8 + P03, P04 | Java core: equals, pule, ConcurrentHashMap | 2 medium w ≤40 min |
| **3** | **System design:** rama + URL shortener + rate limiter | Spring: `@Transactional`, proxy, N+1 | 45 min design na głos, nagrany albo do lustra |
| **4** | Coding: grafy/intervale/LRU — P05, P06, P07 | SQL: indeksy, isolation, cursor pagination | LRU z głowy bez googla |
| **5** | **Design:** chat + news feed | Spring Boot produkcja: health, config, OSIV | 2 designy po 35–40 min |
| **6** | **Kafka Pareto** (`07`) + design: powiadomienia / order pipeline | Outbox, idempotencja, lag, rebalance | Umiesz narysować outbox i powiedzieć at-least-once |
| **7** | Mock coding 40 min + recap tygodnia | Behavioral: 3 historie STAR (impact, awaria, konflikt) | Wiesz, co Cię spowolniło w kodzie |

### Tydzień 2 — stack, mocks, sprzedaż

| Dzień | Blok A | Blok B | Done gdy |
|-------|--------|--------|----------|
| **8** | **AWS Pareto** + design: upload plików (S3 presign, CloudFront) | RDS Multi-AZ vs replica, SQS vs Kafka | 45 min design na AWS |
| **9** | **K8s Pareto** + Spring na klastrze | probes, HPA, rolling, requests/limits | Umiesz powiedzieć liveness vs readiness na przykładzie |
| **10** | **Design:** booking/inventory + SQL locking | Grill Java/Spring/SQL na głos | Booking bez double-sell |
| **11** | **Mock #1 pełny:** coding 40 + design 45 | Recenzja słabych punktów | Notatka: 3 rzeczy do poprawy |
| **12** | Dziury z mocka + Kafka/AWS grill | Behavioral: 6 STAR + exit po 6 latach | Historie z liczbami na kartce |
| **13** | **Mock #2 trudniejszy:** design (feed albo „Uber lite”) + coding | Pytania do nich | Drugi mock czystszy niż pierwszy |
| **14** | Tylko `06-cheat-sheet.md` + 1 easy warmup | Sen. Nic nowego. | Świeża głowa |

---

## Priorytet stacku z ogłoszenia

Must (padnie albo jako pytanie, albo wewnątrz designu):

- Java 17: records, sealed, switch/pattern matching, text blocks
- Java 21: virtual threads — kiedy tak, kiedy nie
- Spring Boot: beans, `@Transactional`, Data JPA N+1, actuator, config
- Kafka: partycje, consumer group, offset, at-least-once, idempotent producer, outbox, lag
- AWS: IAM role, S3, ALB, RDS, SQS/SNS, ECS vs EKS w 60 s
- K8s: Pod/Deploy/Service, probes, resources, rolling update

Should (odróżnia seniora):

- Kafka transactions / EOS ograniczenia
- rebalance storms, poison pill + DLQ
- AZ blast radius, Multi-AZ, RPO/RTO
- HPA vs replica Kafka (nie skalujesz konsumentów w ciemno)

Skip (nie na te 14 dni):

- CKA, Helm charts od zera, operator pattern
- VPC peering + Transit Gateway w detalach
- Kafka MirrorMaker internals
- Virtual threads JEP-y na pamięć

---

## Zasady treningu

1. Coding zawsze ze **zegarem 40 min** i **narracją**.
2. Design zawsze **na głos**, nawet sam. Piszesz API i liczby, nie tylko klocki.
3. Po każdym zadaniu 5 min: co poszło źle, jedna poprawka.
4. Nie otwieraj rozwiązań zanim nie minie 40 min albo nie utkniesz 15 min.
5. Dzień 14 nic nowego. Nowy temat w przeddzień = fałszywy spokój.

## Co NIE robić

- 100 easy na LeetCode
- Kurs Kubernetes 12h
- DDIA od deski
- Certyfikat AWS w 2 tygodnie
- 20 historii behavioral

## Jak ze mną pracować w tych 14 dniach

Ty wrzucasz rozwiązanie (kod albo design mówiony). Ja gram interviewerem: przerywam, dopytuję, oceniam jak na rozmowie, daję następne.

Dzisiejszy start: **P01 i P02** w `coding/`. Protokół w `02-coding-patterns.md`.
