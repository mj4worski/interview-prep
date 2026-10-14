# System design — rama + systemy, które wychodzą w 80% rozmów

Masz 11 lat. Nie rysuj klocków. Prowadź decyzje.

Czytanie: rama i słownik poniżej, potem jeden system na głos, potem link z `09-materialy.md`. Breakdown Hello Interview otwierasz po własnej próbie, inaczej powtarzasz cudzy rysunek.

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

### Skąd te stałe i jeden pełny rachunek

Dobę zaokrąglasz do `10^5` sekund (86 400). Miesiąc ≈ `2.5 × 10^6` s. Rok ≈ `3 × 10^7` s. QPS średnie = akcje / sekundy. Szczyt bierz 2–5× średniej, chyba że produkt ma znany spike (bilet o 10:00).

URL shortener, liczby które masz umieć policzyć na głos:

- 100 mln nowych URL-i łącznie, nie dziennie.
- Odczyt 10 000 QPS, zapis 200 QPS. Stosunek ~50:1, system jest read-heavy. Optymalizujesz redirect, nie insert.
- Rekord: kod 8 B + URL średnio 100 B + metadane ≈ 200 B. 100 mln × 200 B ≈ 20 GB. To jest jeden Postgres, nie klaster.
- Ruch redirectu: 10k QPS × ~500 B odpowiedzi ≈ 5 MB/s. Sieciowo nic.
- Cache: załóż, że 20% kodów to 80% ruchu. 20 mln gorących × 150 B ≈ 3 GB Redisa. Reszta idzie do bazy przy missie.
- Pisz, gdzie rachunek jest założeniem: „średni URL 100 B, jak macie więcej, storage rośnie liniowo, architektura ta sama”.

Rachunek ma zmienić decyzję. 20 GB nie uzasadnia shardingu. 10k QPS odczytu uzasadnia cache przed bazą. 200 QPS zapisu nie uzasadnia Kafki na ścieżce tworzenia linku. Kafka wchodzi przy statystykach kliknięć, bo tam zapis jest na każdym redir i nie musi być synchroniczny.

### Co mówić, gdy liczba wyjdzie niewygodna

Powiedz rząd wielkości i wniosek. „Około 20 GB, więc jeden primary”. Pomyłka o 2× nic nie zmienia. Pomyłka o 1000× (pomyliłeś dziennie z łącznie) zmienia i interviewer to usłyszy. Dlatego najpierw jednostka: dziennie, łącznie, szczyt.

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

## Mechanizmy, które stoisz za hasłem

Hasło z tabeli wystarczy na 20 sekund. Na dopytanie masz zejść w sekwencję.

### CAP bez naklejki

CAP mówi o zachowania **w trakcie partycji sieci** między replikami. Wtedy system albo dalej przyjmuje zapisy po obu stronach (availability, kosztem zgodności), albo część klientów dostaje odmowę, żeby nie rozjechać danych (consistency).

Poza partycją normalna baza robi i C, i A. Dlatego zdanie „Postgres jest CP, Cassandra jest AP” jest skrótem na rozmowę, nie twierdzeniem o każdej sekundzie życia systemu. Kleppmann rozbija to dokładniej: [please stop calling databases CP or AP](https://martin.kleppmann.com/2015/05/11/please-stop-calling-databases-cp-or-ap.html). Na rozmowie wystarczy dopisek: „mówię o zachowaniu przy rozcięciu sieci”.

Spójność, o którą naprawdę pytają, jest produktem:

- **strong** — odczyt widzi ostatni commit. Płatność, stan miejsca w samolocie.
- **read-your-writes** — ten sam użytkownik widzi swój zapis, reszta może chwilę nie. Własny post na feedzie.
- **eventual** — każdy w końcu zobaczy, bez gwarancji kiedy. Licznik lajków, indeks wyszukiwarki.

„Feed może być 2 s do tyłu, saldo nie” jest lepsze niż sama nazwa poziomu.

### Cache-aside, krok po kroku

1. Aplikacja czyta klucz z Redisa.
2. Hit: zwraca, baza nie istnieje w tym żądaniu.
3. Miss: czyta bazę, wstawia do Redisa z TTL, zwraca.
4. Zapis idzie do bazy, potem kasujesz klucz albo nadpisujesz. TTL jest siatką, gdy invalidacja zginie.

Kolejność przy zapisie ma znaczenie. Skasowanie klucza **przed** commitem otwiera okno: inny request czyta starą bazę i wkłada starą wartość do cache już po Twoim commicie. Częsty wybór: commit, potem delete. I tak zostaje wąskie okno dwóch zapisów. Na rozmowie nazwij okno, nie udawaj że go nie ma.

Stampede: gorący klucz wygasa, 500 requestów jednocześnie ma miss i wali w bazę. Środki: lock na czas uzupełnienia (jeden leci do bazy, reszta czeka), oddawanie starej wartości w trakcie odświeżania, TTL z losowym jitterem, żeby klucze nie wpadały w tej samej sekundzie.

### Idempotencja

„Ten sam skutek przy powtórzeniu” nie wynika z dobrych chęci. Wynika z ograniczenia, które drugi raz nie przepuści.

Klient dostaje albo sam generuje `Idempotency-Key` (UUID na operację biznesową, nie na kliknięcie HTTP). Serwer w jednej transakcji:

1. `INSERT INTO idempotency(key, request_hash, response) ...` z unique na `key`.
2. Robi skutek (hold miejsca, zapis zamówienia).
3. Przy konflikcie unique oddaje zapisaną odpowiedź i nie robi skutku drugi raz.

Sam retry bez klucza utworzy drugi wiersz. Sam klucz bez unique w bazie wyścigu dwóch requestów nie złapie. Timeout klienta jest normalny: klient nie wie, czy serwer skończył, więc ponawia. [Making retries safe](https://aws.amazon.com/builders-library/making-retries-safe-with-idempotent-APIs/) jest tekstem do dnia, w którym design schodzi w awarie. Retry z backoff i jitter: [timeouts, retries, backoff](https://aws.amazon.com/builders-library/timeouts-retries-and-backoff-with-jitter/).

### Outbox

Dwa zasoby (baza i Kafka) nie wejdą w jedną transakcję w sposób, na którym chcesz oprzeć system.

Zła sekwencja A: `INSERT order`, commit, `kafka.send`. Kafka niedostępna → zamówienie jest, zdarzenia nie ma, magazyn się nie ruszy.

Zła sekwencja B: `kafka.send`, potem `INSERT`. Baza padnie → zdarzenie mówi „zamówione”, wiersza nie ma.

Outbox: w **jednej** transakcji bazy wstawiasz zamówienie i wiersz `outbox(id, aggregate_id, type, payload, published)`. Commit albo cofa oba. Osobny proces (polling albo Debezium czytający WAL) publikuje nieopublikowane wiersze i oznacza je po sukcesie. Publikacja może iść dwa razy, więc konsument jest idempotentny po `event_id` albo po kluczu biznesowym.

To jest ten sam pomysł co idempotencja: skutek uboczny wychodzi z miejsca, które ma twarde ograniczenie, a nie z „udało się, to wyślę”.

### Fan-out, policzony

Użytkownik ma 200 obserwujących. Post → 200 wstawek do ich timeline’ów. 1000 postów/s od zwykłych ludzi to 200k zapisów/s do timeline’ów. Dużo, ale da się partycjonować po `user_id` czytelnika.

Celebryta ma 10 mln obserwujących. Jeden post to 10 mln zapisów. Przy 5 takich postach na minutę worker nie nadąża, a większość tych ludzi i tak nie otworzy aplikacji, zanim post zniknie z okna. Dlatego hybrid: zwykli są rozpychani przy zapisie, celebryci są dokładani przy odczycie (weź swoje 800 gotowych + ostatnie posty 50 celebrytów, scal po czasie).

Liczba, od której przełączasz, jest decyzją. Powiedz próg na głos, np. „powyżej 10k obserwujących nie fan-outuję przy zapisie”.

### Paginacja, której bronisz

`OFFSET 100000` każe bazie przeczytać i wyrzucić 100k wierszy, żeby oddać 20. Koszt rośnie z numerem strony.

Kursor: klient dostaje sort key ostatniego wiersza, następne zapytanie brzmi „daj 20 rekordów ściśle za tym kluczem”. Indeks na `(created_at, id)` schodzi w pobliże kursora i czyta 20 wierszy. Koszt strony jest stały.

Klucz sortowania musi być unikalny i stabilny. Sam `created_at` remisuje i gubi albo dubluje wiersze na granicy strony. Dokładasz `id`. Snowflake jako `id` jest z grubsza posortowany w czasie, więc często wystarcza sam.

OFFSET zostawiasz przy panelu admina na stronie 3. Nie na feedzie.

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

**301 vs 302, do końca.** 301 znaczy „to miejsce jest na stałe”. Przeglądarka i proxy wolno zapamiętają `code → long_url` i kolejne kliknięcia nie przyjdą do Ciebie. Statystyki zaniżone, podmiana celu linku nie działa. 302 (albo 307) znaczy „na razie skocz tam”. Każdy klik może przejść przez Ciebie. Koszt: więcej QPS. Przy 10k QPS to jest powód cache, nie powód 301.

**Kolizja kodu.** Hash URL-a do base62: dwa różne URL-e mogą dać ten sam kod. Sprawdzasz unique, przy konflikcie dokładsz sól i liczysz jeszcze raz. Licznik globalny (Snowflake, albo zakresy z Redisa) kolizji nie ma, za to kody są zgadywalne, jeśli są monotoniczne — dlatego nie wystawiasz surowego autoincrement.

Po swoim designie, nie przed: [Bitly](https://www.hellointerview.com/learn/system-design/problem-breakdowns/bitly).

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

**Dlaczego fixed window kłamie.** Okno 60 s, limit 100. Użytkownik wysyła 100 requestów w sekundzie 59 i 100 w sekundzie 61. Żadne okno nie widzi 200, a w 2 sekundy poszło 200. Sliding window trzyma znaczniki czasu albo przybliża je wagą dwóch sąsiednich okien. Token bucket trzyma liczbę żetonów i czas ostatniego dopełnienia: burst do pojemności wiadra, potem stałe tempo. Na rozmowie token bucket + Lua w Redisie jest odpowiedzią domyślną, fixed window tylko jako „na start, znam burst na granicy”.

**Lua, bo dwa polecenia to wyścig.** `INCR` i osobno `EXPIRE` potrafią zostać przerwane: proces padnie po INCR, klucz bez TTL żyje wiecznie i użytkownik jest zablokowany na zawsze. Skrypt w Redisie wykonuje się atomowo na jednym wątku serwera.

Redis padł. Fail-open: puszczasz ruch, chronisz dostępność, ryzykujesz że padnie to, co limiter chronił. Fail-closed: zwracasz 429, chronisz dół, sam stajesz się awarią. Dla logowania i płatności częściej fail-closed. Dla odczytu publicznego feedu częściej fail-open z lokalnym, ciasnym limitem w procesie jako druga linia.

Po swoim designie: [distributed rate limiter](https://www.hellointerview.com/learn/system-design/problem-breakdowns/distributed-rate-limiter). Redisowe struktury pod to: [Redis deep dive](https://www.hellointerview.com/learn/system-design/deep-dives/redis).

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

**Co jest prawdą.** Wiersz w `messages` jest prawdą. WebSocket jest powiadomieniem. Jak push zginie, klient przy ponownym połączeniu woła historię `seq > last_seen` i nadrabia. Dlatego exactly-once na kablu jest zbędne: serwer zapisuje raz (unique `message_id`), push może pójść dwa razy, klient deduplikuje po `message_id`.

**Sekwencja w konwersacji.** Jeden writer na konwersację (transakcja w bazie z `max(seq)+1` albo jedna partycja Kafki po `conversation_id`) daje lukę bez dziur. Dwa równoległe inserty bez tego dają dwa razy seq=5 albo dziurę. Unread = `max(seq) - last_read_seq`. To działa tylko, gdy seq jest gęsty i monotoniczny w tej konwersacji. Globalna kolejność między czatami nikomu nie jest potrzebna.

**Grupa 100 vs kanał 10k.** 100 pushy na wiadomość jest tanie. 10k pushy na każdą wiadomość kanału zamienia jednego piszącego w burzę. Wtedy członków nie budzisz pojedynczo: trzymają swój last_seq i dociągają przy otwarciu, a online dostaje sygnał „kanał X ma nowość”, nie kopię treści.

Po swoim designie: [WhatsApp](https://www.hellointerview.com/learn/system-design/problem-breakdowns/whatsapp).

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

**Dlaczego kopia w timeline kłamie.** Fan-out zapisuje `post_id` albo od razu treść. Kopia treści jest szybka w odczycie i zgniła przy edycji oraz usunięciu: 10 mln timeline’ów ma stary tekst. Wskaźnik `post_id` plus odczyt treści z cache postów jest wolniejszy o jeden hop i da się unieważnić w jednym miejscu. Na rozmowie bierz wskaźnik, chyba że p99 odczytu nie wytrzymuje.

**Swój post znika.** Worker fan-outu jeszcze nie wpisał Cię do Twojego timeline’u, a już odświeżasz. Read-your-writes: przy zapisie od razu wstawiasz post do timeline’u autora synchronicznie, resztę obserwatorów obsługuje worker. Albo pierwszy odczyt po zapisie idzie ścieżką, która dokleja własne posty z tabeli `posts`, nie z timeline’u.

Rachunek progu celebryty jest wyżej, w sekcji fan-out. Tu tylko decyzja: próg mówisz liczbą.

Po swoim designie: [news feed](https://www.hellointerview.com/learn/system-design/problem-breakdowns/fb-news-feed).

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

**Dlaczego SELECT, potem UPDATE, sprzedaje dwa razy.** Oba requesty czytają `status=free`. Oba wchodzą w UPDATE. Bez warunku w UPDATE drugi nadpisuje pierwszego. Baza nie widzi „intencji”, widzi dwa zapisy.

Poprawny UPDATE niesie warunek: `WHERE id=:id AND status='free'`. Silnik bierze lock wiersza, zmienia co najwyżej jeden raz, drugi dostaje 0 wierszy i wie, że przegrał. To jest pesymistyczne na czas tego jednego statementu i nie wymaga trzymania transakcji przez 10 minut płatności.

Hold na 10 minut jest osobnym stanem, nie otwartą transakcją. Otwarta transakcja przez czas wpisywania karty trzyma połączenie z puli i lock. Przy 5k osobach pula siada, zanim skończy się koncert. `held` + `hold_until` pozwala commitnąć od razu. Worker co minutę robi `UPDATE ... SET status='free' WHERE status='held' AND hold_until < now()`.

Webhook płatności przyjdzie drugi raz. Warunek `WHERE booking_id=:id AND status='held'` przy confirm jest idempotentny: drugi raz zmienia 0 wierszy i zwraca sukces.

Cache mapy sal kłamie w trakcie skoku. Mapa może być cache’owana na sekundy. Sam hold zawsze idzie do wiersza, nigdy do kopii z cache.

Po swoim designie: [Ticketmaster](https://www.hellointerview.com/learn/system-design/problem-breakdowns/ticketmaster).

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
