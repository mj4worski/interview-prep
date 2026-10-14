# System design — 20 minut, nie 45

Na Karacie nie prowadzisz sesji „wymagania, QPS, API, schemat, deep dive”. Dostajesz **krótki scenariusz**. Masz około czterech minut, włącznie z dopytaniem. Cztery, czasem pięć takich pytań.

Rama z `01-system-design.md` jest na późniejszą rundę z zespołem. Tu zaszkodzi: zjesz połowę czasu na DAU, zanim powiesz, co jest zepsute.

## Cztery zdania, zawsze w tej kolejności

Mów po angielsku.

```text
The failure is ___.
The user impact is ___.
The first change is ___. The tradeoff is ___.
I'd know it worked when ___.
```

Jak proszą „to zaprojektuj”, dopisz tylko: gdzie leży stan i co jest synchroniczne, a co idzie obok commitu. Potem przestań. Nie rysuj dwunastu pudełek i nie zaczynaj od Kafki albo Kubernetesa.

Jak mówią „anything else?”, dokładaj drugi konkretny risk, nie głębszy opis tego samego. W relacjach z tej części ludzie dostają cztery albo pięć wklejonych promptów, a interviewer zbiera listę, nie jeden rysunek.

Dwa braki, których nie ma w scenariuszach niżej, a wracają w relacjach:

- Logujesz tylko wysłane notyfikacje. Nieudanych nie ma w logu, więc nie da się ich wygrepać. Lista id, które miały pójść, minus lista id, które doszły, to są te nieudane. Od teraz zapis próby powstaje przed wysyłką, razem z porażką.
- Licznik, który dziś liczysz skanem dużej tabeli relacji (znajomi, polubienia). Trzymaj go przy rekordzie i aktualizuj przy zapisie. Cache na ten licznik, nie na całe skanowanie.

Liczby podawaj tylko gdy oni podali liczby, albo jeden rząd wielkości, który zmienia decyzję („to nie wejdzie w ten sam Postgres”). Nie udawaj kalkulatora.

Najpierw powiedz swoją wersję na głos. Wzorzec pod scenariuszem czytaj dopiero potem. Jak Twoja wersja ma te cztery zdania i konkretną zmianę, jest dobra, nawet gdy słownictwo jest inne.

Trening jutro: scenariusze 1, 2, 4 i 5. Trójkę i szóstkę, jeśli zostanie czas. GCB4 słucha mocniej 1, 2 i 3. GCB5 słucha mocniej 4, 5 i 6. Zrób wszystkie cztery z planu, nie tylko „swoją” rubrykę.

---

## 1. SPOF na ścieżce przelewu (GCB4)

Przelew woła synchronicznie jeden on-prem serwis fraudowy, zanim wolno mu odpowiedzieć. We wtorek ten serwis leżał 12 minut i nie przeszedł żaden przelew. Co jest nie tak i co zmieniasz?

Wzorzec:

```text
The failure is a single dependency on the critical path. Its outage is our outage, and we don't operate it.

The user can't move money, including payments that are obviously low risk.

The first change is a short timeout, around 100 to 150 milliseconds, plus a circuit breaker so a dead dependency stops occupying threads. Low-value transfers fall back to a local rule or settle and get reviewed async. High-value transfers fail closed.

The tradeoff is that during an outage some fraud gets through, capped by the amount limit. If the business refuses any fallback, the real fix is a second instance in another zone. Until that exists we shed load instead of hanging the API.

I'd know it worked when a dark fraud service no longer saturates the thread pool, and the count of fallback settlements stays inside the cap we agreed.
```

Dopytanie „a jak nie wolno przepuścić nic bez fraudu?”: timeout, breaker, druga instancja. Bez drugiej instancji i tak nie masz dostępności, tylko wolniejsze padanie.

---

## 2. Saldo, które cofa się po odświeżeniu (GCB4 i GCB5)

UI mówi, że przelew się udał. Odbiorca czasem widzi nowe saldo, czasem stare, przez pół minuty. O co chodzi?

Wzorzec:

```text
The failure is a read that isn't looking at the write. The debit is committed on the primary, and the balance is served from a replica or a cache that lags.

The user sees a successful transfer and then a balance that goes backwards. They retry. That's how you get a double submit.

I wouldn't make every read strongly consistent. The invariant is read-your-writes for the sender. The recipient can be a few seconds behind, and the UI should say so.

The first change is to return the new balance from the POST, and to read the sender's account from the primary for a few seconds after their own write. Invalidate that account's cache key on write. The recipient path stays on the replica.

The tradeoff is extra read traffic on the primary, bounded to the writer and a short window. Waiting for replica catch-up before we respond would make every transfer as slow as the worst replication lag.

I'd watch replica lag and how often a client observes a balance that moved backwards.
```

---

## 3. PAN w logach i CSV na wspólnym buckecie (GCB4)

Log aplikacji zawiera pełny numer karty i identyfikator. Nocny job kładzie CSV klientów na bucket, z którego czyta analityka i zewnętrzny vendor. Jakie jest ryzyko i co zmieniasz?

Wzorzec:

```text
These are two different failures.

The log line is a credential leak. Card numbers get copied into the log vendor, support laptops, and backups, and we can't really delete them. Encryption doesn't help if the same app decrypts them to print.

The bucket is access and minimization. One shared principal, no expiry, more columns than the vendor needs. The file also has no checksum, so a partial write looks like data.

The first change on logs is to stop writing PAN and the national id, log a token or last four, and scrub in the logger so printing the object can't regress it. Treat retained logs as exposed.

The first change on the file is a separate prefix per consumer, a role instead of a long-lived key, a column allow-list, and a manifest with a hash. The object expires.

The tradeoff is a slower investigation when support wants the full number. They go through a gated lookup, they don't grep production logs.

I'd know it worked when a new log event cannot contain a PAN, and the vendor's role cannot read the analytics prefix.
```

---

## 4. Piątek, dwa node’y na 100% (GCB5)

Osiem instancji za round robin. W piątek dwa node’y siedzą na 100% CPU, reszta stoi. Jeden klient payroll to około 40% ruchu. Gdzie patrzysz?

Wzorzec:

```text
The failure is that round robin balances connections, not work. The payroll burst is sticky: keep-alive, a handful of source NATs, or a gateway pinning that client. Two nodes do the whole file.

The user impact is on everyone else. Interactive traffic that landed on those two nodes times out, while six nodes are idle.

First I confirm it: CPU and in-flight requests per node, and the customer id on the hot ones. If it's pin, move to least-connections and make sure one long-lived connection isn't carrying the whole file.

The scale change is to get the batch off the interactive API. They submit a file, we ack, a worker pulls from a queue with a concurrency cap. Sharding by customer id would make this worse. The big customer becomes a hot partition.

The tradeoff is minutes of latency for a file they already send once a week. That's acceptable if they get a status.

I'd know it worked when CPU skew across the eight nodes drops during Friday, and the p99 of interactive calls stays flat through that window.
```

---

## 5. Mail i SMS po przelewie (GCB5)

Po commicie wysyłamy mail i SMS. Czasem dwa, czasem zero, zwykle przy deployu albo gdy provider odda 500. Jak to ustawiasz?

Wzorzec:

```text
The failure is sending inside the request, after the commit, with no record of the attempt. A crash after commit and before send loses the message. A timeout where the provider actually accepted it, plus our retry, duplicates it.

The user either hears nothing about a real payment, or gets two codes, or two "you were paid" mails and opens a ticket.

The first change is an outbox row in the same transaction as the transfer: id, transfer id, channel, template, status, attempts. A worker sends. We claim the row with a conditional update so two workers can't both own it. The provider call uses that id as the idempotency key.

This is at-least-once, so the template has to be safe to read twice. "Transfer X completed", not a fresh secret generated on each attempt. Attempts are capped, then the row goes to a dead state we alert on. We do not roll back the transfer because email failed.

The tradeoff is that the user can see success a few seconds before the SMS. Coupling them again is how the outage comes back.

I'd watch the age of the oldest pending outbox row, the send error rate, and duplicates if the provider reports them.
```

Jak dopytają „po co kolejka, nie wystarczy retry w kontrolerze?”: retry w kontrolerze nie przeżywa restartu procesu i nie ma klucza, po którym poznajesz, że pierwsza próba jednak doszła.

---

## 6. Duży strumień zdarzeń (GCB5)

Średnio 1000 zdarzeń autoryzacji na sekundę, peak około 5×, payload rzędu 1 KB. Śledztwo szuka ostatnich 90 dni po karcie albo po merchancie. Prawny wymóg to 7 lat. Co się stanie, jak włożymy to w ten sam Postgres co przelewy?

Najpierw rząd wielkości, na głos, wolno zaokrąglić:

```text
A day is about 10^5 seconds. One thousand events per second times 10^5 times 1 KB is on the order of 100 GB a day. Ninety days is about 10 TB raw, more with indexes. Seven years is on the order of a few hundred TB. Peak, five thousand a second, is an ingest problem, not a storage problem.
```

Potem decyzja:

```text
The failure is putting an append-only investigation log into the same primary as transfers. Inserts and ad-hoc lookups fight, index updates on card and merchant get hot, and deleting seven years of rows will vacuum forever. A bad query on this table becomes an incident for payments.

The first change is to split hot and cold, and to split this from the transfer database.

Hot store: ninety days, partitioned by day so retention is dropping a partition, not deleting rows. Two indexes only, because those are the real queries: card id plus time, and merchant id plus time. No open-ended SQL for investigators.

Ingest: append with a unique authorization id, so a retry doesn't double-count. At-least-once into the pipeline is fine if that key is unique.

Cold store: object storage, columnar files per day, seven years, cheap. A job moves a day after it is immutable. Those lookups are allowed to be slow.

The bottleneck I watch first is ingest lag, not query p99. If the sink is behind, an investigator looks at an incomplete day and doesn't know.

The tradeoff is a slow legal lookup. I would not keep seven years in the database to make that one query fast.
```

1000 × 100 000 × 1 KB = 100 GB/dzień. 90 dni ≈ 9 TB. 7 lat × ~100 GB ≈ 250 TB surowych. Mów „rząd wielkości”, nie trzy cyfry znaczące.

---

## Czego nie mówić

- „Dałbym to na Kafkę i Kubernetesa” jako pierwsze zdanie. Nazwij awarię.
- „Zrobimy strongly consistent wszędzie.” Powiedz, który odczyt musi widzieć który zapis.
- „Dodamy Redis.” Powiedz, jaki klucz i kiedy go unieważniasz.
- Lista dziesięciu komponentów bez jednej decyzji, z której rezygnujesz.
