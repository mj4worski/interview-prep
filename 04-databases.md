# Bazy danych — Pareto pod interview

Myśl: "jaki plan wykona silnik" i "co się psuje przy współbieżności".

Pod spodem jest przejście po indeksie i oś czasu izolacji. Linki z limitem są w `09-materialy.md`. Na dzień 4 czytasz anatomię indeksu, nie cały podręcznik Postgresa.

## Indeksy

B-tree (domyślny): posortowany, dobry do `=`, `>`, `BETWEEN`, `ORDER BY`, prefix `LIKE 'abc%'`.
Nie pomoże: `LIKE '%abc'`, funkcja na kolumnie `LOWER(email)=`, luźny typ.

**Composite:** `(a, b, c)` działa na `a`, `a+b`, `a+b+c`. Nie na samym `b` (left-prefix).
**Covering:** indeks ma wszystkie kolumny z SELECT — index-only scan, bez heap.
**Partial / filtered:** `WHERE deleted_at IS NULL` — cieńszy indeks.
**Unique:** indeks + constraint. Źródło idempotencji.

Koszt: wolniejszy write, miejsce, vacuum/maintenance.
Na rozmowie: "najpierw EXPLAIN ANALYZE na wolnym query, nie indeks na przeczucie".

### Jak B-tree naprawdę szuka

Indeks to osobna struktura, kopia kolumn plus wskaźnik do wiersza. Liście są posortowane i spięte w listę. Nad nimi jest wyważone drzewo, które w kilku skokach (dla milionów wierszy często 3–4) schodzi do właściwego liścia.

`WHERE email = 'a@b.c'` schodzi drzewem do liścia i czyta kilka sąsiednich wpisów. `WHERE created_at >= '2024-01-01'` schodzi do pierwszego pasującego liścia i idzie listą liści, dopóki warunek żyje. `LIKE 'abc%'` jest zakresem na prefiksie, więc też. `LIKE '%abc'` nie ma gdzie zacząć zejścia, bo posortowane jest od lewej. `LOWER(email) = 'a@b.c'` szuka wartości, której w indeksie na `email` nie ma, bo leżą tam oryginały. Indeks na wyrażeniu `(LOWER(email))` jest osobną decyzją.

Złożony `(a, b, c)` jest posortowany najpierw po `a`, w ramach tego samego `a` po `b`, potem po `c`. Zapytanie po samym `b` nie ma punktu startu. Zapytanie `a = ? AND b > ?` ma. Zapytanie `a > ? AND b = ?` użyje `a` jako zakresu, a `b` już tylko odfiltruje. To jest left-prefix, nie „indeks działa na dowolną kolumnę ze środka”.

Każdy INSERT/UPDATE kolumny z indeksu dopisuje też indeks. Pięć indeksów „na wszelki wypadek” spowalnia zapis i i tak nie ratuje zapytania, którego nie zmierzyłeś.

Anatomia, z obrazkami: [Use The Index, Luke](https://use-the-index-luke.com/sql/anatomy). Złożone klucze: [concatenated keys](https://use-the-index-luke.com/sql/where-clause/the-equals-operator/concatenated-keys). Funkcja na kolumnie: [functions in WHERE](https://use-the-index-luke.com/sql/where-clause/functions). Postgres o kolejności kolumn: [multicolumn indexes](https://www.postgresql.org/docs/current/indexes-multicolumn.html).

### Selektywność

Indeks na `gender` (2 wartości) zwykle zły. Bitmap / sequential scan lepszy.
Indeks na `email`, `order_id` — tak.

## EXPLAIN, które musisz umieć przeczytać

Szukasz:

- Seq Scan na dużej tabeli + filter → brak / zły indeks
- Nested Loop * wiele wierszy → często N+1 albo zły join order
- Sort + duży memory → brak indeksu pod ORDER BY, spill na disk
- est rows vs actual rows — statystyki nieaktualne (`ANALYZE`)

Przykład, który czytasz w 30 sekund. Tabela `orders` ma miliony wierszy, jest indeks `(status, created_at)`.

```text
Index Scan using orders_status_created_idx on orders
  Index Cond: ((status = 'PAID') AND (created_at >= '2024-01-01'))
  Rows Removed by Filter: 12
  actual time=0.04..1.2 rows=340
```

`Index Cond` zszedł w indeks. `Rows Removed by Filter` małe: filtr prawie nic nie wyrzucił po fakcie. `actual rows` blisko tego, czego się spodziewasz.

Sygnał alarmowy wygląda inaczej: `Seq Scan`, `Filter: status = 'PAID'`, `Rows Removed by Filter: 4800000`, `actual rows=340`. Silnik przeczytał tabelę, żeby oddać 340 wierszy. Albo nie ma indeksu, albo warunek jest na wyrażeniu, albo statystyki mówią planerowi, że status i tak wybierze pół tabeli.

`Nested Loop` przy zewnętrznej pętli 10k wierszy i wewnętrznym Index Scan na każdy to często N+1 albo join, który powinien być hashem. Sam nested loop na 20 wierszach jest w porządku.

`Sort Method: external merge` znaczy, że sort nie zmieścił się w `work_mem` i poszedł na dysk. Indeks zgodny z `ORDER BY` potrafi ten sort zdjąć.

Różnica `estimated rows=10` vs `actual rows=100000` to nie „zły indeks”, tylko nieaktualne statystyki albo skorelowane kolumny, których planer nie widzi. `ANALYZE`, potem dopiero nowy indeks.

[EXPLAIN w Postgresie](https://www.postgresql.org/docs/current/using-explain.html).

## Transakcje i izolacja

ACID w 20 s:
- Atomic — wszystko albo nic
- Consistent — constraintów nie złamiesz
- Isolated — równoległe tx nie płączą się bardziej niż poziom izolacji
- Durable — po commicie przeżyje crash (WAL)

### Poziomy (Postgres-centric, najczęściej na rozmowach)

| Poziom | Dirty read | Non-repeatable | Phantom | Kiedy |
|--------|------------|----------------|---------|-------|
| Read Uncommitted | możliwe* | tak | tak | prawie nigdy |
| Read Committed | nie | tak | tak | **default PG** |
| Repeatable Read | nie | nie | PG: nie** | raporty, ten sam odczyt 2× |
| Serializable | nie | nie | nie | pieniądze, inventory gdy trzeba |

\*PG traktuje RU jak RC.  
\**PG RR używa snapshot — phantomów praktycznie nie ma jak w teorii SQL.

**Dirty read:** czytasz niecommitowane.  
**Non-repeatable:** ten sam SELECT 2×, inna wartość wiersza.  
**Phantom:** pojawia się nowy wiersz spełniający WHERE.

**Write skew:** dwa tx czytają, oba widzą OK, oba zapisują, invariant pada. Dopiero Serializable (albo świadomy lock) łapie. Przykład: dyżur — dwóch lekarzy się wypisuje, zostaje 0 na shift.

### Oś czasu, nie tabela

Non-repeatable read, poziom Read Committed. Konto ma saldo 100.

| Czas | Transakcja A | Transakcja B |
|------|----------------|--------------|
| 1 | BEGIN | |
| 2 | SELECT saldo → 100 | |
| 3 | | BEGIN; UPDATE saldo=80; COMMIT |
| 4 | SELECT saldo → 80 | |
| 5 | decyduje na podstawie 80, a w kroku 2 widziała 100 | |

W Read Committed każde polecenie widzi commit, który już jest. Drugi SELECT jest legalnie inny. Repeatable Read w Postgresie bierze snapshot na pierwsze zapytanie transakcji i krok 4 nadal widzi 100. Snapshot trzyma stare wersje wierszy (MVCC), więc czytelnik nie blokuje pisarza.

Write skew, ten sam mechanizm, gorszy skutek. Reguła: na dyżurze zostaje co najmniej jeden lekarz. Jest dwóch, obie transakcje na poziomie Repeatable Read.

| Czas | Lekarz A | Lekarz B |
|------|----------|----------|
| 1 | SELECT count(*) WHERE on_call → 2 | |
| 2 | | SELECT count(*) → 2 |
| 3 | UPDATE siebie SET on_call=false | |
| 4 | | UPDATE siebie SET on_call=false |
| 5 | COMMIT | COMMIT |

Żaden wiersz nie był edytowany przez obu. Każdy snapshot widział dwójkę. Po commicie zostało zero. Repeatable Read tego nie złapie, bo nikt nie zmienił wiersza, który czytał drugi. Serializable w Postgresie (SSI) wykrywa taką zależność i jedną transakcję wywali z `40001`. Świadomy `SELECT ... FOR UPDATE` na wierszach dyżuru albo na wierszu „stan dyżuru” serializuje ich ręcznie.

Booking z `UPDATE ... WHERE status='free'` nie potrzebuje serializable na cały świat: warunek w UPDATE jest lockiem tego jednego wiersza i drugi dostaje 0 zmienionych wierszy.

Postgres trzyma wersje wierszy, nie nadpisuje w miejscu. Stąd czytelnik bez locka. Stąd też vacuum, który sprząta wersje niewidoczne dla nikogo. [MVCC](https://www.postgresql.org/docs/current/mvcc-intro.html), [izolacja](https://www.postgresql.org/docs/current/transaction-iso.html) (jest tam przykład z lekarzami).

### Locki

- Row lock przy `UPDATE`/`SELECT FOR UPDATE`
- Deadlock: A czeka na B, B na A → DB zabija jedną tx (`40001`). Retry po swojej stronie.
- Optimistic: kolumna `version`. `UPDATE ... WHERE id=? AND version=?`. 0 rows → konflikt.
- Pessimistic: `FOR UPDATE` gdy konflikt częsty (booking miejsca).

## N+1

Objaw: 1 query na listę + N na relację.
Fix: join fetch / 2 query (id IN (...)) / batch.
W logach: 201 zapytań na jeden request.

## Paginacja

`OFFSET 100000` jest wolny — przelatuje puste wiersze.
Cursor / keyset: `WHERE (created_at, id) < (?, ?) ORDER BY created_at DESC, id DESC LIMIT 20`.
Stabilny klucz sortowania obowiązkowy (nie samo `created_at`).

`OFFSET` każe przejść i wyrzucić poprzednie wiersze. Strona 5000 kosztuje więcej niż strona 1, mimo że oddajesz 20 rekordów. Kursor trzyma ostatni widziany `(created_at, id)` i prosi o następne, które są ściśle za nim. Indeks schodzi w to miejsce. Dwa wiersze z tym samym `created_at` bez `id` w kluczu albo wpadną dwa razy, albo zginą na granicy strony.

Obrazek i powód: [fetch next page](https://use-the-index-luke.com/sql/partial-results/fetch-next-page). Ten sam SQL: [keyset pagination](https://vladmihalcea.com/sql-seek-keyset-pagination/).

## Normalizacja vs denormalizacja

3NF na source of truth. Denormalizujesz pod read path (feed, counters).
Licznik `likes_count` na poście = szybszy read, ból przy reconcile. Albo Redis + rebuild.

## Transakcje rozproszone

2PC prawie nigdy na start. Mówisz:

- **Outbox** w tej samej TX co biznes
- **Saga** z kompensacją gdy wiele serwisów
- **Idempotentny konsument**

"Mamy Kafka więc exactly-once" — nie. Kafka EOS jest ograniczone. Zakładaj at-least-once.

## Postgres vs MySQL vs Redis vs ES vs Kafka (kiedy co)

| System | Użyj gdy |
|--------|----------|
| Postgres | relacje, constraint, TX, większość CRUD |
| MySQL | to samo; uważaj na historyczne różnice izolacji |
| Redis | cache, lock, rate limit, session, ranking ZSET — nie jedyny source of truth na pieniądze |
| Elasticsearch | full-text, log search. Nie primary store. |
| Kafka | event stream, decoupling, replay. Nie request-response. |
| S3 | bloby. Metadata w DB. |

## Schema change na produkcji

- Expand/contract: dodaj kolumnę nullable → backfill → przełącz kod → drop stare
- Unikaj long lock (`ALTER` na ogromnej tabeli) — w PG nowsze wersje częściej online, i tak planuj
- Indeks: `CREATE INDEX CONCURRENTLY`
- Nigdy "drop column + deploy w jednej chwili" gdy stare instancje jeszcze piszą

## SQL, które warto napisać na tablicy

Top N per group (najnowsze zamówienie per user):

```sql
SELECT *
FROM (
  SELECT o.*,
         ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY created_at DESC) AS rn
  FROM orders o
) t
WHERE rn = 1;
```

Albo `DISTINCT ON (user_id)` w PG.

Suma z filtrem + indeks:

```sql
SELECT customer_id, SUM(amount)
FROM orders
WHERE created_at >= :from AND status = 'PAID'
GROUP BY customer_id;
-- indeks (status, created_at) albo (created_at) + filter; zależy od selektywności
```

Deadlock-aware booking:

```sql
UPDATE seats
SET status = 'held', hold_until = now() + interval '10 minutes'
WHERE id = :id AND status = 'free';
-- 0 rows => zajęte
```

## Pytania grill

**Co to WAL?**
Log sekwencyjny zmian. Commit = WAL na dysk. Crash recovery odtwarza. Replika czyta WAL / ship.

Silnik dopisuje opis zmiany na koniec jednego pliku sekwencyjnie, zanim uzna commit. Dopisywanie na koniec jest tańsze niż losowy zapis po stronach tabeli. Po crashu odtwarza z ostatniego checkpointu, nakładając WAL. Replika dostaje ten sam strumień i aplikuje go u siebie. Async znaczy, że commit na primary wraca do klienta, zanim replica skończy. Stąd lag i stąd read-your-writes, którego replica nie daje. [WAL](https://www.postgresql.org/docs/current/wal-intro.html).

**Rozmiar puli.** Każde połączenie na serwerze Postgresa to proces i pamięć. Pula 200 przy 4 vCPU nie robi 200 równoległych zapytań, robi kolejkę wewnątrz bazy i kontekst-switch. Hikari po stronie aplikacji trzyma małą pulę (rząd: rdzenie bazy razy kilka, nie razy liczba podów bez liczenia). 30 podów po 20 połączeń to 600 sesji na jednym RDS. [About Pool Sizing](https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing).

**Replica lag?**
Async replica może być z tyłu. Read-your-writes: czytaj primary albo sticky. Raporty — replica OK.

**Connection pool?**
Hikari. Rozmiar ≈ `connections = (core_count * 2) + spindle` to mit-punkt start, nie dogma. Za duża pula = kolejka w DB, nie szybciej. Nigdy nie otwieraj połączenia per request ręcznie.

**NULL w unique?**
W PG wiele NULL w unique jest dozwolone (NULL ≠ NULL). Partial unique index gdy "jeden aktywny".

**CHAR vs VARCHAR vs TEXT?**
W PG `TEXT`/`VARCHAR` praktycznie to samo. Nie mikrooptymalizuj.

**Kiedy denormalizować counter w Redis a kiedy w SQL?**
Hot, przybliżony, wolno stracić przy evict → Redis. Pieniądze, audyt → SQL jako prawda, Redis jako cache.
