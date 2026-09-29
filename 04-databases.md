# Bazy danych — Pareto pod interview

Myśl: "jaki plan wykona silnik" i "co się psuje przy współbieżności".

## Indeksy

B-tree (domyślny): posortowany, dobry do `=`, `>`, `BETWEEN`, `ORDER BY`, prefix `LIKE 'abc%'`.
Nie pomoże: `LIKE '%abc'`, funkcja na kolumnie `LOWER(email)=`, luźny typ.

**Composite:** `(a, b, c)` działa na `a`, `a+b`, `a+b+c`. Nie na samym `b` (left-prefix).
**Covering:** indeks ma wszystkie kolumny z SELECT — index-only scan, bez heap.
**Partial / filtered:** `WHERE deleted_at IS NULL` — cieńszy indeks.
**Unique:** indeks + constraint. Źródło idempotencji.

Koszt: wolniejszy write, miejsce, vacuum/maintenance.
Na rozmowie: "najpierw EXPLAIN ANALYZE na wolnym query, nie indeks na przeczucie".

### Selektywność

Indeks na `gender` (2 wartości) zwykle zły. Bitmap / sequential scan lepszy.
Indeks na `email`, `order_id` — tak.

## EXPLAIN, które musisz umieć przeczytać

Szukasz:

- Seq Scan na dużej tabeli + filter → brak / zły indeks
- Nested Loop * wiele wierszy → często N+1 albo zły join order
- Sort + duży memory → brak indeksu pod ORDER BY, spill na disk
- est rows vs actual rows — statystyki nieaktualne (`ANALYZE`)

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
