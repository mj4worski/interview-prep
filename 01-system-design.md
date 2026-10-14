# Nie na screen Karat

20 minut na Karacie to krótkie scenariusze z `karat/02-design-20.md`. Ta rama jest na 45 minut i na rozmowę z zespołem, już po screenie.

# System design — rama + systemy, które wychodzą w 80% rozmów

Masz 11 lat. Nie rysuj klocków. Prowadź decyzje.

## Rama 7 kroków (45 minut)

Trzymaj się tego jak protokołu. Interviewer Cię za to lubi.

### 1. Wymagania (4–5 min)

Najpierw funkcjonalne, potem niefunkcjonalne. Zawsze dopytaj:

- Kto jest userem? Read-heavy czy write-heavy?
- Skala: DAU, QPS, size of data, retention
- Latency: p99 read / write
- Consistency: czy wolno być stale?
- Offline / mobile?
- Co jest out of scope? (płatności, admin, ML ranking — wytnij)

Powiedz na głos: "Zakładam X, jeśli to złe — popraw mnie."

### 2. Back-of-envelope (3 min)

Nie licz jak księgowy. Pokaż, że skala steruje architekturą.

Szybkie stałe:

- 1 day ≈ 10^5 s
- 1000 DAU * 10 akcji = 10k/day ≈ 0.1 QPS (nic nie skalujesz)
- 100 mln DAU * 20 akcji ≈ 20k QPS średnio, peak 3–5×
- 1 rekord 1 KB * 100 mln = 100 GB
- 1 rok logów 1 KB * 10k QPS * 10^5 s * 365 ≈ dużo TB → object storage, nie Postgres na wszystko

Powiedz: "To read-heavy ~10:1, p99 200 ms, dane ~2 TB/rok. Postgres + cache wystarczy na start, shard później."

### 3. API (3 min)

REST albo RPC. 4–7 endpointów. Request/response, auth, idempotency key na write.

```
POST /v1/urls
  {url, custom_slug?} → {code, short_url}
GET  /{code} → 302 Location
GET  /v1/urls/{code}/stats
```

### 4. Model danych (5 min)

Tabele albo kolekcje + klucze + indeksy + TTL.
Od razu: co jest PK, co unique, co denormalizujesz.

### 5. High-level (5 min)

Klient → LB → app → cache → DB → (queue) → workers → object storage.
Narysuj 6–8 pudełek. Potem idź w głąb tam, gdzie interviewer ciągnie.

### 6. Deep dives (20 min)

Wybierz 2–3 twarde miejsca. Senior idzie tu, nie w "dodamy Redis".

Typowe deep dives:

- hotspot key / celebrity problem
- exactly-once vs at-least-once
- pagination consistency
- cache invalidation
- unique ID generation
- fan-out on write vs read
- multi-region
- failure + retry + idempotency

### 7. Ryzyka i ewolucja (5 min)

- Co padnie jako pierwsze?
- Jak mertrics / alerty (QPS, p99, error rate, queue lag, cache hit)
- Co zmienisz przy 10× skali

## Słownik, którym mówisz jak senior

| Temat | Co powiedzieć w 20 sekund |
|-------|---------------------------|
| CAP | W partycji wybierasz C albo A. W praktyce: RDBMS = CP-ish, Dynamo/Cassandra = AP + eventual. |
| Consistency | Strong / read-your-writes / eventual. "Feed może być stale 2s, płatność nie." |
| Cache | Cache-aside: app czyta cache, miss → DB → set. Invalidacja po write. TTL jako siatka bezpieczeństwa. |
| Stampede | Hot key wygasa → thundering herd. Lock, singleflight, stale-while-revalidate, jittered TTL. |
| Idempotencja | Ten sam request 2× = 1 efekt. Key od klienta + unique constraint / dedup table. |
| Outbox | Zapis do DB + event w tej samej transakcji. Worker publikuje. Brak "zapisane, Kafka padła". |
| At-least-once | Duplikaty się zdarzą. Konsument musi być idempotentny. |
| Backpressure | Kolejka rośnie → odrzucaj, degraduj, shed load. Nie powiększaj w nieskończoność. |
| SLO | p99 latency, availability 99.9% = 43 min downtime/mies. |

## 5 systemów, które musisz umieć prowadzić

Poniżej jest poziom "mówię na rozmowie", nie encyklopedia.

---

### A. URL shortener

**Wymagania:** skracanie, redirect 301/302, custom alias, opcjonalnie TTL i stats. 100 mln URL, 10k read QPS, 200 write QPS.

**API:** `POST /urls`, `GET /{code}`.

**Kod:** base62 z 7–8 znaków. 62^7 ≈ 3.5e12. Wystarczy.

**ID:**

- Snowflake / epoch+worker+seq — sortowalne, bez koordynacji
- albo hash URL + collision retry
- nie autoincrement publicznie (enumeracja)

**Data:**

```
urls(code PK, long_url, user_id, created_at, expires_at)
unique(long_url) opcjonalnie
```

**Read path:** cache `code → long_url` (Redis), TTL 1 dzień. 301 vs 302: 301 cache'uje przeglądarka — trudniej liczyć kliki i rotować.

**Stats:** nie pisz do Postgres przy każdym redir. `redirect event → Kafka → aggregator`. Albo Redis INCR + flush.

**Deep dive:** kolizje kodu, preview vs phishing, custom alias unique, shard po code prefix jeśli > kilku TB.

**Tradeoff:** KVS (Dynamo) vs Postgres. Na start Postgres wygrywa prostotą. Przy ekstremalnym read — edge cache / CDN na 302.

---

### B. Rate limiter

Wychodzi samo albo jako kawałek większego designu. Musisz umieć.

**Cel:** 100 req / user / 60s, sprawiedliwie, niski overhead, multi-instance.

**Algorytmy (wystarczą 3):**

1. **Fixed window** — proste, burst na granicy okna (100 na końcu + 100 na starcie).
2. **Sliding window log / counter** — dokładniej, drożej.
3. **Token bucket** — burst do pojemności, refill stały. Najczęściej "wystarczy".

**Implementacja token bucket w Redis:**

- klucz `rl:{user}` → tokens + last_refill
- Lua script atomowo: refill, check, decr
- albo `INCR` + `EXPIRE` (fixed window) na start

**Gdzie stoi:** API gateway / sidecar, nie w każdym serwisie od zera.

**Distributed:** jeden Redis cluster. Lokalny limiter = niesprawiedliwy (5 instancji × 100 = 500).

**Deep dive:** hierarchia limitów (user, IP, endpoint), 429 + `Retry-After`, bypass dla internal, degradacja gdy Redis padnie (fail-open vs fail-closed — powiedz tradeoff: fail-open = ryzyko abuse, fail-closed = outage).

---

### C. Chat (1:1 + group)

**Wymagania:** 1:1, grupy do 100, delivery online, historia, unread. Nie Slack dla 10k członków na start.

**Kluczowa decyzja:** WebSocket do online, HTTP do historii.

**Model:**

```
conversations(id, type, last_message_at)
members(conversation_id, user_id, last_read_seq)
messages(conversation_id, seq, sender, body, created_at)
PK (conversation_id, seq)
```

Seq monotoniczny w konwersacji = łatwy unread i paginacja.

**Online path:**

- conn service trzyma `user → ws connection`
- presence w Redis
- send: zapisz message (source of truth) → push do online members
- offline: badge / push notification

**Group fan-out:** do 100 osób możesz pushować na żywo. Przy 10k (Slack channel) nie. Wtedy fan-out on read albo hybrid.

**Ordering:** w jednej konwersacji serializuj zapis (DB sequence / single partition Kafka). Cross-conversation nie musi być globalny.

**Deep dive:** exactly-once nie dasz na WS. At-least-once + client dedup po message_id. Read receipts osobnym eventem, nie w hot path.

**Media:** upload do object storage, w message tylko URL.

---

### D. News feed

**Wymagania:** post, follow, home feed. 10 mln DAU. Celebrity problem istnieje.

**Fan-out on write:** przy poście wstawiasz do timeline followerów.
- + szybki read
- − wolny write przy 10 mln followers (celebrity)

**Fan-out on read:** przy otwarciu feeda składasz z recent posts followees.
- + tani write
- − drogi read, trudny p99

**Hybrid (to chcesz powiedzieć):**
- zwykli userzy: fan-out on write do Redis/Cassandra `timeline:{user}` (k-ostatnich)
- celebrity: nie fan-out, wstrzykujesz ich posty przy read
- cache 1–2k ostatnich postów na usera

**Data:**

```
posts(id, author_id, body, created_at)  -- id = snowflake
follows(follower, followee)
timeline(user_id, post_id, ts)         -- denormalizacja
```

**Ranking:** na rozmowie: recency + trochę signals. Nie buduj ML. "Najpierw chronological, ranking jako osobny worker."

**Deep dive:** pagination (cursor = snowflake, nie OFFSET), undelete/edit (feed ma stare kopie — albo pointer do post store), consistency "nie widzę swojego posta" = read-your-writes z sticky session albo write-through do własnego timeline.

---

### E. Ticket booking / inventory (koncert, pociąg)

Wychodzi, bo sprawdza transakcje i race.

**Problem:** 1 miejsce, 5k osób klika. Nie sprzedać 2×.

**Flow:**

1. Hold: `UPDATE seats SET status='held', hold_until=now()+10min WHERE id=? AND status='free'`
2. Płatność asynchroniczna
3. Confirm albo timeout worker zwalnia hold

**Dlaczego nie "sprawdź i zapisz":** TOCTOU. Warunek w UPDATE + row lock / version.

**Warianty:**

- SQL `version` optimistic locking
- Redis SETNX na `seat:{id}` + TTL hold
- jedna partycja / jeden writer na event (kolejka per event_id) — serializacja

**Deep dive:** overbooking świadomy (linie lotnicze), calendar view cache vs source of truth, payment webhook idempotentny, "user odświeża 10 razy" = ten sam hold.

---

## Bonus, jeśli zostaje czas

### File / image storage (Dropbox lite)

- client upload: app daje presigned URL → S3 bezpośrednio
- metadata w DB: path, version, size, checksum
- sync: change feed per user
- dedup po hash
- block-level sync tylko jeśli ciągną w to

### Notification system

- producer → Kafka topic per priority
- workers: email / push / SMS
- user prefs w cache
- templating
- retry z backoff + DLQ
- quiet hours
- nie blokuj biznesowego requestu na SMTP

### Autocomplete / typeahead

- trie w pamięci per shard prefix
- albo Redis ZSET: `prefix → {query: frequency}`
- cache popularnych prefixów
- update async z query log
- relevancy prosta: frequency * recency

## Pytania, które zadajesz Ty (wygląda na seniora)

- Jaki p99 i z jakiego regionu?
- Czy read-your-writes jest wymagane?
- Jaki budżet / zespół / time-to-market? (monolit na start jest OK)
- Co jest compliance? (PII, GDPR, retention)
- Jaki jest failure budget?

## Checklista 45-minutowego designu

- [ ] Wymagania i out-of-scope
- [ ] Liczby rzędu wielkości
- [ ] API + model
- [ ] Diagram
- [ ] 2 deep dive (nie 10 płytkich)
- [ ] Awarie
- [ ] Observability
- [ ] Co zmienię przy 10×

Jeśli interviewer ciągnie w jedną stronę — idź. Rama jest po to, żeby nie błądzić, nie żeby ją odczytać jak checklistę.
