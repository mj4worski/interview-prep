# Materiały — jak czytać, żeby zostało

Pliki `01`–`05`, `07` i `08` oraz wzorce w `02` są teraz do nauki: mechanizm, przykład, pułapka, link. `06-cheat-sheet.md` zostaje ściągą na 20 minut przed rozmową. Tego pliku nie rozbudowujemy.

## Zasada

Jeden temat = sekcja w pliku, potem zamknięcie karty i 60–90 sekund na głos. Link otwierasz, gdy mechanizmu nie umiesz powtórzyć. Limit z tabeli jest twardy.

Rozwiązań zadań (NeetCode, LeetCode Discuss, „solution” na YouTube) nie otwieraj przed własnymi 40 minutami albo zanim nie utkniesz 15 minut. Wizualizacje struktur i dokumentacja są przed kodem. Nagrania rozwiązań są po Twoim podejściu.

Kurs Kubernetes, certyfikat AWS i DDIA od deski nadal odpadają. Poniżej są rozdziały, nie książki.

## Mapa 14 dni

Czas przy linku jest dodatkiem do kodu i mówienia, nie zamiast nich.

| Dzień | Najpierw w repo | Potem link (limit) | Zostaje w głowie, gdy... |
|-------|-----------------|--------------------|--------------------------|
| 1 | `02` wzorce 1–4, potem P01 i P02 | [VisuAlgo Hashtable](https://visualgo.net/en/hashtable) 15 min. Big-O z sekcji w `02`, bez wideo z rozwiązaniem | Umiesz przejść Two Sum i okno na przykładzie palcem |
| 2 | `02` wzorce 5–8, P03 i P04 | [VisuAlgo DFS/BFS](https://visualgo.net/en/dfsbfs) 15 min, heap 10 min | BFS vs DFS i „kopiec rozmiaru k” mówisz bez kartki |
| 3 | `01` rama + URL shortener + rate limiter | [Delivery framework](https://www.hellointerview.com/learn/system-design/in-a-hurry/delivery) 25 min. Po swoim designie: [Bitly](https://www.hellointerview.com/learn/system-design/problem-breakdowns/bitly) i [rate limiter](https://www.hellointerview.com/learn/system-design/problem-breakdowns/distributed-rate-limiter) po 20 min | 45 min designu na głos, z liczbami |
| 4 | P05–P07 + `04` indeksy, izolacja, cursor | [Anatomy of an index](https://use-the-index-luke.com/sql/anatomy) 25 min i [concatenated keys](https://use-the-index-luke.com/sql/where-clause/the-equals-operator/concatenated-keys) 20 min | Left-prefix i write skew na przykładzie lekarzy |
| 5 | `01` chat + feed | Po swoim designie: [WhatsApp](https://www.hellointerview.com/learn/system-design/problem-breakdowns/whatsapp) i [news feed](https://www.hellointerview.com/learn/system-design/problem-breakdowns/fb-news-feed) po 25 min | Hybrid fan-out i seq w konwersacji |
| 6 | `07` Kafka + outbox | [Kafka 101, moduł Events](https://developer.confluent.io/courses/apache-kafka/events/) ~30 min i [outbox Debezium](https://debezium.io/blog/2019/02/19/reliable-microservices-data-exchange-with-the-outbox-pattern/) 25 min | Rysujesz outbox i mówisz, czego EOS nie załatwia |
| 7 | mock coding + `05` trzy historie | [Behavioral interview](https://www.techinterviewhandbook.org/behavioral-interview/) 20 min | Trzy STAR z liczbą i czasownikiem „ja” |
| 8 | `07` AWS + upload | [Presigned URL](https://docs.aws.amazon.com/AmazonS3/latest/userguide/using-presigned-url.html) 15 min i [RDS Multi-AZ](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/Concepts.MultiAZ.html) 15 min | Presign i różnica Multi-AZ vs replica |
| 9 | `07` K8s + Spring na klastrze | [Probes](https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle/#container-probes) 20 min i [Spring liveness/readiness](https://spring.io/blog/2020/03/25/liveness-and-readiness-probes-with-spring-boot) 15 min | Liveness bez bazy, readiness z bazą |
| 10 | `01` booking + `04` locki | Po swoim designie: [Ticketmaster](https://www.hellointerview.com/learn/system-design/problem-breakdowns/ticketmaster) 30 min. Izolacja: [Postgres transaction isolation](https://www.postgresql.org/docs/current/transaction-iso.html) sekcja o write skew, 20 min | UPDATE z warunkiem, 0 wierszy = zajęte |
| 11 | mock | Tylko notatka 3 dziur. Link dopiero do dziury | Wiesz, co poprawić, nie „jeszcze jeden artykuł” |
| 12 | dziury + `05` sześć historii | [Idempotent APIs](https://aws.amazon.com/builders-library/making-retries-safe-with-idempotent-APIs/) 25 min, jeśli dziura jest w retry | Historie mają liczbę |
| 13 | drugi mock | Nic nowego. Porównanie z mockiem 1 | Drugi raz jest ciaśniejszy |
| 14 | tylko `06` + 1 easy | Zero nowych linków | Sen |

## Coding

Teoria struktur, zanim usiądziesz do zegara:

- [NeetCode roadmap](https://neetcode.io/roadmap) — mapa wzorców, 10 min na orientację. Odtwarzacz z rozwiązaniem dopiero po swoim kodzie.
- [Tech Interview Handbook — coding](https://www.techinterviewhandbook.org/coding-interview-prep/) — jak trenować, nie lista 300 zadań. Sekcja o komunikacji na rozmowie.
- [VisuAlgo](https://visualgo.net/en) — hashtable, heap, graph traversal, BST. Kliknij operację, nie czytaj eseju.

Zadania z planu (treść, nie rozwiązania):

| Wzorzec | Treść |
|---------|--------|
| Hash / two pointers | [Two Sum](https://leetcode.com/problems/two-sum/), [3Sum](https://leetcode.com/problems/3sum/) |
| Okno | [Longest Substring Without Repeating](https://leetcode.com/problems/longest-substring-without-repeating-characters/), [Minimum Window Substring](https://leetcode.com/problems/minimum-window-substring/) |
| Prefix | [Subarray Sum Equals K](https://leetcode.com/problems/subarray-sum-equals-k/) |
| Stos | [Daily Temperatures](https://leetcode.com/problems/daily-temperatures/) |
| Graf | [Number of Islands](https://leetcode.com/problems/number-of-islands/), [Course Schedule](https://leetcode.com/problems/course-schedule/) |
| Kopiec | [Top K Frequent](https://leetcode.com/problems/top-k-frequent-elements/) |
| Interwały | [Merge Intervals](https://leetcode.com/problems/merge-intervals/) |
| Design | [LRU Cache](https://leetcode.com/problems/lru-cache/) |

## System design

Najpierw rama z `01`, potem jeden breakdown Hello Interview **po** własnym mówieniu. Czytanie cudzego designu przed próbą daje złudzenie, że umiesz prowadzić.

- [System Design in a Hurry — intro](https://www.hellointerview.com/learn/system-design/in-a-hurry/introduction)
- [Core concepts](https://www.hellointerview.com/learn/system-design/in-a-hurry/core-concepts) — cache, sharding, kolejki. Wybierz 3 hasła z `01`, nie cały spis.
- [System Design Primer](https://github.com/donnemartin/system-design-primer#study-guide) — słownik i liczby. Pomiń działy, których nie ma w kalendarzu.
- [Please stop calling databases CP or AP](https://martin.kleppmann.com/2015/05/11/please-stop-calling-databases-cp-or-ap.html) — 15 min, żeby CAP nie był naklejką.
- [DDIA](https://dataintensive.net/) — tylko rozdziały 5 (replikacja), 7 (transakcje), 11 (strumienie). Reszta po ofertach, jeśli w ogóle.
- [Timeouts, retries, backoff](https://aws.amazon.com/builders-library/timeouts-retries-and-backoff-with-jitter/) — gdy design schodzi w awarie.

## Java, Spring, SQL

- [equals i hashCode](https://www.baeldung.com/java-equals-hashcode-contract) — kontrakt i przykład zepsutej pary. 20 min.
- [Java Memory Model, Jenkov](https://jenkov.com/tutorials/java-concurrency/java-memory-model.html) — happens-before. Sekcje o volatile i locku, nie cały tutorial wątków.
- [Spring @Transactional in depth, Marco Behler](https://www.marcobehler.com/guides/spring-transaction-management-transactional-in-depth) — proxy, propagacja, rollback. To jest tekst do dnia 3, blok B. Daj mu 45 min.
- [Dokumentacja @Transactional](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html) — ściąga atrybutów, gdy Behler już siedzi.
- [N+1, Vlad Mihalcea](https://vladmihalcea.com/n-plus-1-query-problem/) i [Open Session in View](https://vladmihalcea.com/the-open-session-in-view-anti-pattern/) — po 15 min.
- [Use The Index, Luke — Where Clause](https://use-the-index-luke.com/sql/where-clause) i [funkcje w WHERE](https://use-the-index-luke.com/sql/where-clause/functions) — dlaczego `LOWER(email)` omija indeks.
- [Keyset pagination](https://use-the-index-luke.com/sql/partial-results/fetch-next-page) i [ten sam temat u Vlada](https://vladmihalcea.com/sql-seek-keyset-pagination/).
- [Postgres MVCC](https://www.postgresql.org/docs/current/mvcc-intro.html), [WAL](https://www.postgresql.org/docs/current/wal-intro.html), [EXPLAIN](https://www.postgresql.org/docs/current/using-explain.html).
- [HikariCP — About Pool Sizing](https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing) — skąd się bierze „nie 200 połączeń”.

## Kafka, AWS, Kubernetes

- [Kafka documentation — design](https://kafka.apache.org/documentation/#design) i [semantics](https://kafka.apache.org/documentation/#semantics). Introducja, nie konfiguracja brokera.
- [Exactly-once w Kafka, Confluent](https://www.confluent.io/blog/exactly-once-semantics-are-possible-heres-how-apache-kafka-does-it/) — przeczytaj ograniczenia, nie marketing.
- [Transactional outbox, microservices.io](https://microservices.io/patterns/data/transactional-outbox.html) — krótki opis wzorca obok artykułu Debezium.
- [IAM roles](https://docs.aws.amazon.com/IAM/latest/UserGuide/id_roles.html) — dlaczego task role, a klucz w zmiennej środowiskowej jest słaby.
- [Requests i limits](https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/) oraz [rolling update](https://kubernetes.io/docs/tutorials/kubernetes-basics/update/update-intro/).

## Java 17 / 21

Oficjalne, krótkie. JEP czytasz pod „motywacja + kilka akapitów”, nie pod historię dyskusji.

- [Records w języku](https://docs.oracle.com/en/java/javase/21/language/records.html) i [JEP 395](https://openjdk.org/jeps/395)
- [Sealed classes](https://docs.oracle.com/en/java/javase/21/language/sealed-classes-and-interfaces.html) i [JEP 409](https://openjdk.org/jeps/409)
- [Pattern matching for switch, JEP 441](https://openjdk.org/jeps/441)
- [Virtual threads, Oracle](https://docs.oracle.com/en/java/javase/21/core/virtual-threads.html) i [JEP 444](https://openjdk.org/jeps/444)
- [Spring Boot — virtual threads](https://docs.spring.io/spring-boot/reference/features/spring-application.html#features.spring-application.virtual-threads) — flaga, daemon threads, `spring.main.keep-alive`

## Behavioral

- [Tech Interview Handbook — behavioral](https://www.techinterviewhandbook.org/behavioral-interview/) — struktura odpowiedzi i pytania do nich.
- Reszta jest w `05-behavioral.md`. Cudze historie z blogów nie zastąpią Twoich liczb.
