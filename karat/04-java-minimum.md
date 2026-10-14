# Java, którą piszesz bez szukania

Tabela do druku jest w `karat/05-sciaga.md`, sekcja JDK. Tu zostają dłuższe przykłady.

Tylko tyle. Reszta biblioteki Javy nie wchodzi w 30 minut. Przy W1 możesz tu zerkać. Od K2 pisz z głowy.

## Map i zliczanie

```java
Map<String, Integer> freq = new HashMap<>();
freq.merge(word, 1, Integer::sum);

Map<String, Long> sums = new HashMap<>();
sums.merge(key, amount, Long::sum);

long[] cell = byAccount.computeIfAbsent(account, k -> new long[3]);
cell[0] += amount;
```

Brak klucza: `get` zwraca `null`. Nie wołaj metody na tym wyniku. Albo `computeIfAbsent`, albo `getOrDefault`.

## Klucz z dwóch pól

```java
String key = userId + "\0" + template;
```

`userId + template` myli `"ab"+"c"` z `"a"+"bc"`.

## Grosze

```java
static long toCents(String amount) {
    int dot = amount.indexOf('.');
    if (dot <= 0 || amount.indexOf('.', dot + 1) != -1) {
        throw new IllegalArgumentException("amount");
    }
    String whole = amount.substring(0, dot);
    String frac = amount.substring(dot + 1);
    if (frac.length() != 2 || !allDigits(whole) || !allDigits(frac)) {
        throw new IllegalArgumentException("amount");
    }
    return Long.parseLong(whole) * 100L + Long.parseLong(frac);
}

static boolean allDigits(String s) {
    if (s.isEmpty()) return false;
    for (int i = 0; i < s.length(); i++) {
        if (!Character.isDigit(s.charAt(i))) return false;
    }
    return true;
}
```

`0.00` jest poprawne i daje `0`. `12.5` i `12.555` są złe. Nie używaj `double`.

## Linia CSV i liczba

```java
String[] p = line.split(",", -1); // -1 zostawia puste pole na końcu
if (p.length != 6) throw new IllegalArgumentException("columns");
for (int i = 0; i < p.length; i++) p[i] = p[i].trim();

int attempt;
try {
    attempt = Integer.parseInt(p[5]);
} catch (NumberFormatException e) {
    throw new IllegalArgumentException("attempt");
}
```

`split("\\|", -1)` gdy separatorem jest `|`.

## equals i hashCode

Identyczność płatności to `id`, nie kwota. Oba muszą patrzeć na to samo. Inaczej `HashSet` kłamie: `contains` liczy hash, potem `equals`.

```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Payment)) return false;
    return id.equals(((Payment) o).id);
}

@Override
public int hashCode() {
    return id.hashCode();
}
```

Porównanie statusu: `"SETTLED".equals(t.status)`, nigdy `t.status == "SETTLED"`.

## Sortowanie kluczy

```java
List<String> ids = new ArrayList<>(map.keySet());
Collections.sort(ids);
```

## Rate

```java
double rate = (double) errors / hits;
```

`errors / hits` przy dwóch `int` jest zerem, dopóki błędów nie będzie co najmniej tyle, ile hitów.

## Złożoność, jedno zdanie

```text
One pass over the transfers, so time is O(n). The map keeps one row per account, so space is O(n). The sort of account ids makes time O(n log n) if I include it.
```

## Czego nie pisać na zegarze

- `Stream`, `groupingBy`, `TreeMap` „bo ładniej”, jeśli pętla już działa.
- `record`, `sealed`, pattern matching. Ich JDK może być starsze, a Ty zużyjesz minuty na składnię.
- Nowa abstrakcja, fabryka, enum statusów, jeśli w zadaniu status jest `String`.
- `LinkedList` i `Stack`. Lista to `ArrayList`. Kolejka, gdy naprawdę trzeba, to `ArrayDeque`.
