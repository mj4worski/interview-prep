# Ściąga na rano

Nic nowego. 15 minut. Potem zamknij.

## Kolejność

1. Intro, krótko. Java, zanim wkleją zadanie.
2. Design, 20 minut. Cztery zdania: failure, user impact, first change + tradeoff, jak poznasz, że działa.
3. Kod, 30 minut. A musi być zielone, zanim będzie B. Skończone A+B bije niedokończone trzy części.

## Design, ściągnij do jednej linijki

- Padł cudzy serwis na ścieżce: timeout, circuit breaker, drobne kwoty osobno, duże fail closed.
- Saldo się cofa: zapis na primary, odczyt z repliki albo cache. Read-your-writes dla nadawcy, reszta może być chwilę w tyle.
- PAN w logu: przestań to logować. Szyfrowanie nic nie daje, skoro aplikacja to wypisuje.
- Dwa node’y gorące: round robin nie widzi ciężaru. Batch zdejmij z API na kolejkę. Nie sharduj po wielkim kliencie.
- Podwójny albo zgubiony SMS: wiersz outbox w tej samej transakcji co przelew. Klucz idempotencji. Przelewu nie cofasz, bo mail nie wyszedł.
- Gruby strumień w Postgresie przelewów: osobny zapis, partycja na dzień, 90 dni na gorąco, lata w object storage. Pilnuj lagu ingestu.

## Kod, zanim zaczniesz pisać

```text
Let me restate the failing case.
My hypothesis is ___. I'll change that one thing and rerun.
```

- Tylko status ze specu wchodzi do sumy. Reszta jest w danych po to, żeby Cię złapać.
- Pieniądze: `long` w groszach, cięcie po kropce, dokładnie dwie cyfry. Zero jest legalne.
- `equals` i `hashCode` po tym samym polu. Najpierw hash, potem equals. Sam `equals` nie naprawia `HashSet`.
- Na końcu: `O(n)` na przejście, `O(n)` na mapę, `O(n log n)` jak sortujesz klucze.

## JDK

| Potrzebujesz | Piszesz |
|---|---|
| gdzie jest kropka | `s.indexOf('.')` |
| drugi raz od tego miejsca | `s.indexOf('.', from)` |
| kawałek tekstu | `s.substring(begin, end)` |
| podział linii | `line.split(",", -1)` albo `line.split("\\|", -1)` |
| spacje przy polu | `s.trim()` |
| pusto | `s.isBlank()` |
| cyfra | `Character.isDigit(c)` |
| liczba | `Long.parseLong(s)`, `Integer.parseInt(s)` |
| zły format liczby | łapiesz `NumberFormatException`, rzucasz `IllegalArgumentException` |
| status | `"SETTLED".equals(status)` |
| zliczanie | `map.merge(key, 1, Integer::sum)` |
| wiersz na konto | `map.computeIfAbsent(account, k -> new long[3])` |
| klucz z dwóch pól | `userId + "\0" + template` |
| rate | `(double) errors / hits` |
| klucze do sortowania | `new ArrayList<>(map.keySet())`, potem `Collections.sort(ids)` |

## Zdanie, gdy zostaje minuta

```text
Part B is green. Part C groups by account, filters SETTLED, sorts the keys. Time O(n log n), space O(n). I'll keep coding that.
```

## Organizacyjnie

Chrome, słuchawki, kamera, prąd, cisza, 10 minut wcześniej. Link do testu audio jest w mailu.

Dokumentacja Javy: wolno, powiedz na głos. Gotowe rozwiązanie z internetu albo z AI: nie.

Jak screen się rozsypie technicznie i support milczy: `support@karat.io`. Redo, jeśli w ogóle, w 24 godziny. Oba nagrania idą do klienta.
