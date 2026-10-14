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

Importy, o które łatwo się potknąć:

| Potrzebujesz | Piszesz |
|---|---|
| mapa | `import java.util.HashMap;` i `import java.util.Map;` |
| lista | `import java.util.ArrayList;` i `import java.util.List;` |
| sortowanie listy | `import java.util.Collections;` |
| zbiór | `import java.util.HashSet;` i `import java.util.Set;` |
| unikalne posortowane | `import java.util.TreeSet;` |
| kolejka albo stos | `import java.util.ArrayDeque;` i `import java.util.Deque;` |
| kopiec | `import java.util.PriorityQueue;` i `import java.util.Comparator;` |
| equals, gdy pole może być null | `import java.util.Objects;` |

Tekst:

| Potrzebujesz | Piszesz |
|---|---|
| znak na pozycji | `s.charAt(i)` |
| długość | `s.length()` |
| gdzie jest kropka | `s.indexOf('.')` |
| szukaj dalej od pozycji | `s.indexOf('.', from)` |
| ostatnie wystąpienie | `s.lastIndexOf('.')` |
| od indeksu do końca | `s.substring(begin)` |
| kawałek, znak `end` już nie wchodzi | `s.substring(begin, end)` |
| cyfry po kropce | `s.substring(dot + 1)` |
| CSV, puste pole na końcu zostaje | `line.split(",", -1)` |
| podział po pipe | `line.split("\\|", -1)` |
| przyciąć jedno pole | `s.trim()` |
| przyciąć każde pole | `for (int i = 0; i < p.length; i++) p[i] = p[i].trim();` |
| pusto albo same spacje | `s.isBlank()` |
| zaczyna się / kończy | `s.startsWith("2024")`, `s.endsWith("Z")` |
| status | `"SETTLED".equals(s)` |
| bez względu na wielkość liter | `s.equalsIgnoreCase("usd")` |
| kolejność, też czas ISO-8601 | `a.compareTo(b)` |
| późniejszy czas | `a.compareTo(b) > 0` |
| wielkie litery | `s.toUpperCase()` |
| dokładnie 3 wielkie litery | `s.matches("[A-Z]{3}")` |
| cyfra | `Character.isDigit(c)` |
| litera | `Character.isLetter(c)` |
| skleić kolumny | `String.join(",", parts)` |

Liczby:

| Potrzebujesz | Piszesz |
|---|---|
| int albo long z tekstu | `Integer.parseInt(s)`, `Long.parseLong(s)` |
| zły format liczby | `catch (NumberFormatException e)` i `throw new IllegalArgumentException("amount")` |
| grosze | `Long.parseLong(whole) * 100L + Long.parseLong(frac)` |
| większa albo mniejsza | `Math.max(a, b)`, `Math.min(a, b)` |
| porównanie do sortowania | `Integer.compare(a, b)`, `Long.compare(a, b)`, `Double.compare(a, b)` |
| rate | `(double) errors / hits` |
| hash z long | `Long.hashCode(amount)` |

Mapa, zbiór, lista:

| Potrzebujesz | Piszesz |
|---|---|
| nowa mapa | `Map<String, Integer> map = new HashMap<>();` |
| brak klucza | `map.get(key)` zwraca `null` |
| wartość albo zero | `map.getOrDefault(key, 0)` |
| czy klucz jest | `map.containsKey(key)` |
| wstawić | `map.put(key, value)` |
| zliczyć | `map.merge(key, 1, Integer::sum)` |
| dodać kwotę | `map.merge(key, cents, Long::sum)` |
| wiersz na konto, raz | `long[] row = map.computeIfAbsent(account, k -> new long[3]);` |
| wstawić tylko gdy nie ma | `map.putIfAbsent(key, value)` |
| przejść pary | `for (Map.Entry<String, long[]> e : map.entrySet())` |
| klucz i wartość pary | `e.getKey()`, `e.getValue()` |
| klucz z dwóch pól | `userId + "\0" + template` |
| zbiór | `Set<String> seen = new HashSet<>();` potem `seen.add(id)` i `seen.contains(id)` |
| unikalne i od razu posortowane | `Set<String> ids = new TreeSet<>();` potem `new ArrayList<>(ids)` |
| lista | `new ArrayList<>()`, `add`, `get(i)`, `size()` |

Sortowanie:

| Potrzebujesz | Piszesz |
|---|---|
| klucze rosnąco | `List<String> ids = new ArrayList<>(map.keySet());` potem `Collections.sort(ids);` |
| własna kolejność | `rows.sort((a, b) -> { ... return 0; });` |
| rate malejąco, remis po nazwie | `int c = Double.compare(b.rate, a.rate); if (c != 0) return c; return a.key.compareTo(b.key);` |
| pole, potem drugie pole | `list.sort(Comparator.comparing((Row r) -> r.name).thenComparing(r -> r.id));` |

Kolejka, stos, top-k. Na tym screenie rzadziej niż mapa, ale rubryka ma struktury danych:

| Potrzebujesz | Piszesz |
|---|---|
| kolejka | `Deque<String> q = new ArrayDeque<>();` |
| włóż na koniec, zdejmij z przodu | `q.addLast(x)`, `q.pollFirst()` |
| podejrzyj bez zdejmowania | `q.peekFirst()`, `q.peekLast()` |
| stos | `q.addLast(x)`, `q.pollLast()` |
| najmniejszy na wierzchu | `PriorityQueue<Integer> min = new PriorityQueue<>();` potem `min.add(x)` i `min.poll()` |
| największy na wierzchu | `new PriorityQueue<>(Comparator.reverseOrder())` |

`equals` i `hashCode`:

| Potrzebujesz | Piszesz |
|---|---|
| ten sam obiekt | `if (this == o) return true;` |
| zły typ | `if (!(o instanceof Payment)) return false;` |
| pole, które może być null | `Objects.equals(id, other.id)` |
| hash jednego stringa | `return id.hashCode();` |
| hash z kilku pól | `return Objects.hash(id, currency);` |

Pułapki, które na tym kodzie już wyszły:

| Potrzebujesz | Piszesz |
|---|---|
| cyfry po kropce | `substring(dot + 1)`. `substring(dot)` zostawia kropkę, `".50"` ma długość 3 |
| druga kropka | `indexOf('.', from)`. Pierwszy argument to znak, nie pozycja |
| puste pole na końcu CSV | `split(",", -1)` |
| porównanie stringów | `"SETTLED".equals(status)` |
| rate z dwóch int | `(double) a / b`. Samo `a / b` jest zerem |
| `HashSet` po `equals` | `hashCode` patrzy na to samo pole |
| `get` bez klucza | sprawdź `null` albo użyj `getOrDefault` |
| pieniądze | `long` w groszach. Nie `double` |
| usuwanie w trakcie pętli | zbierz osobno. `remove` w for-each rzuca `ConcurrentModificationException` |
| przyciąć kolumny | pętla i `trim`. Stream da się napisać, na zegarze pętla jest krótsza |

## Zdanie, gdy zostaje minuta

```text
Part B is green. Part C groups by account, filters SETTLED, sorts the keys. Time O(n log n), space O(n). I'll keep coding that.
```

## Organizacyjnie

Chrome, słuchawki, kamera, prąd, cisza, 10 minut wcześniej. Link do testu audio jest w mailu.

Dokumentacja Javy: wolno, powiedz na głos. Gotowe rozwiązanie z internetu albo z AI: nie.

Jak screen się rozsypie technicznie i support milczy: `support@karat.io`. Redo, jeśli w ogóle, w 24 godziny. Oba nagrania idą do klienta.
