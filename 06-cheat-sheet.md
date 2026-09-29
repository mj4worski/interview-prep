# Ściąga na 20 minut przed rozmową

Ten plik zostaje krótki. Teoria, przykłady i linki są w `01`–`05`, `07`, `08` i w mapie `09-materialy.md`. Tutaj tylko kotwice, które masz powiedzieć, gdy głowa jest pusta.

## Coding — 8 linijek w głowie

1. Powtórz zadanie + constraints + przykład
2. Brute force na głos → cel złożoności
3. Hash / pointers / window / heap / BFS / binsearch / prefix
4. `HashMap`, `ArrayDeque`, `PriorityQueue`, `long` na sumy
5. Dry-run + empty / n=1 / duplikaty
6. Time / space
7. Cisza? Mów co sprawdzasz
8. Działa brzydko > nie działa mądrze

## System design — 7 kroków

Wymagania → liczby → API → dane → diagram → 2 deep dive → awarie/metryki

Zdania-kotwice:

- "Na start jeden region, Postgres, cache-aside, kolejkę tam gdzie write nie musi być sync."
- "Idempotency key + unique constraint. At-least-once, konsument odporny na duplikaty."
- "Celebrity / hotspot: hybrid fan-out albo key split / local cache."
- "Fail Redis: rate limiter fail-open/closed — świadomy tradeoff."
- "10× skala: shard po X, nie wcześniej."

## Java

- equals/hashCode kontrakt, nie mutuj klucza
- ConcurrentHashMap, bounded thread pool, volatile ≠ atomic ++
- Deadlock = kolejność locków
- Optional tylko jako return, orElseGet
- GC: G1, mierz JFR, nie zgaduj heap

## Spring

- Constructor injection
- @Transactional = proxy, public, rollback na Runtime, self-invocation nie działa
- Długa TX nie woła HTTP
- LAZY poza TX = boom, OSIV ostrożnie
- N+1: loguj SQL, fetch/join/batch

## SQL

- Indeks composite left-prefix, covering, CONCURRENTLY
- PG default = Read Committed
- Write skew → Serializable albo lock
- OFFSET zły, cursor dobry
- Outbox > 2PC

## Behavioral — 4 zdania o sobie

"Senior backend, 11 lat, ostatnie 6 w [X] przy [produkt].
Najbardziej dumny z [liczba].
Szukam [typ problemu].
Dlatego ta rola."

## Czerwone flagi, których unikasz

- "Po prostu dodamy Kafkę / Kubernetes / microservices"
- Kod w ciszy przez 10 minut
- Design bez liczb
- "U nas zawsze tak było"
- Plucie na obecną firmę
- "Nie pamiętam, nie używałem 6 lat" bez mostu do tego, jak to ogarniesz
