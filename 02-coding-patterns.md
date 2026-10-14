# Nie na screen Karat

Kod na tej rozmowie to bug, parse i agregacja: `coding/` W1, K1, K2, K3 oraz `karat/03-kod-30.md`. Wzorce poniżej są na późniejszą rundę algorytmiczną, jeśli w ogóle przyjdzie.

# Coding — 12 wzorców, nie 200 zadań

Na HackerRank / LeetCode w 40 minut wygrywa ten, kto rozpoznaje wzorzec w 2 minuty i komunikuje.

Język: Java. Pisz jak na rozmowie: jasne nazwy, brzegi, złożoność na końcu.

## Protokół 40 minut (ważniejsze niż wzorzec)

1. **60–90 s — powtórz zadanie swoimi słowami.** "Dostaję tablicę n, mam zwrócić…"
2. **Przykład ręcznie.** Weź input z treści i jeden edge (empty, 1 element, negatywy, duplikaty).
3. **Constraints.** n = 10^5? Wtedy O(n²) nie przejdzie. n = 100? Przejdzie.
4. **Brute force na głos.** Nawet jeśli go nie kodujesz. "Mogę sprawdzić wszystkie pary w O(n²)."
5. **Cel.** "Chcę O(n) time, O(n) memory" albo "O(n log n)".
6. **Kod.** Najpierw happy path, potem brzegi.
7. **Dry-run** na przykładzie.
8. **Złożoność.** Time / space. Wąskie miejsca.

Jeśli utkniesz 5 minut — wróć do brute force i optymalizuj jeden bottleneck.

## Złożoność, zanim wybierzesz wzorzec

Big-O mówi, jak rośnie praca, gdy rośnie `n`. Na rozmowie liczy się próg, od którego rozwiązanie pada na teście, nie akademicka definicja.

Przybliżenie: 1 sekunda to rząd `10^8` prostych operacji. Stąd:

| n | O(n) | O(n log n) | O(n²) | Co z tego wynika |
|---|------|------------|-------|------------------|
| 100 | śmiech | śmiech | śmiech | brute force jest OK |
| 10^4 | OK | OK | na styk | n² bywa akceptowane |
| 10^5 | OK | OK | za wolno | szukasz liniowego albo n log n |
| 10^6 | OK | na styk | martwe | jedno przejście, stała pamięć jeśli się da |

`log` przy binsearch i kopcu jest mały: `log2(10^5) ≈ 17`. Dlatego `n log n` przy sortowaniu i `n log k` przy kopcu rozmiaru k przechodzą tam, gdzie `n²` nie.

Pamięć ma ten sam próg. Mapa na `n` elementów przy `n = 10^5` jest normalna. Mapa na wszystkie pary już nie.

### Jak rozpoznać wzorzec w 2 minuty

Pytania, które zadajesz sobie po przeczytaniu treści:

1. Czy odpowiedź zależy od „czy już to widziałem” albo od pary do targetu? HashMap / Set.
2. Czy dane są posortowane, albo wolno je posortować, i szukasz pary z dwóch końców? Dwa wskaźniki.
3. Czy to podciąg ciągły (substring, subarray bez dziur) z warunkiem „dopóki ok”? Okno. Wskaźnik lewy tylko dogania, nigdy nie cofa prawego.
4. Czy suma zakresu albo „ile podtablic sumuje się do k”? Suma prefiksowa. Różnica dwóch prefiksów to suma między nimi.
5. Czy „następny większy” albo zagnieżdżone nawiasy? Stos. Często monotoniczny: trzymasz indeksy w kolejności wartości.
6. Czy „najmniejsze X takie, że da się”? Jeśli `can(X)` jest monotoniczne (jak da się przy 10, da się przy 11), binsearch po odpowiedzi.
7. Czy top-k, mediana na bieżąco, scalanie k list? Kopiec. Kopiec min rozmiaru k trzyma k największych.
8. Czy siatka, wyspa, najkrótsza droga w nieważonym grafie? BFS. Najkrótsza w nieważonym grafie jest z BFS, nie z DFS.
9. Czy zależności „A przed B” i pytanie o cykl? Sortowanie topologiczne, algorytm Kahna.
10. Czy nakładające się odcinki? Sort po starcie, potem jedno przejście.

Dopiero potem piszesz kod. Wzorzec wybrany w ciemno daje 30 minut w złej złożoności.

Materiały do tej sekcji: [NeetCode roadmap](https://neetcode.io/roadmap) jako spis, [VisuAlgo](https://visualgo.net/en) do hasha, kopca i BFS. Limity i zakaz rozwiązań przed czasem: `09-materialy.md`.

## Co umieć pisać bez myślenia

```java
Map<Integer, Integer> freq = new HashMap<>();
freq.merge(x, 1, Integer::sum);

Deque<Integer> q = new ArrayDeque<>(); // stack i queue
q.addLast(x); q.pollFirst(); q.peekLast();

PriorityQueue<Integer> min = new PriorityQueue<>();
PriorityQueue<Integer> max = new PriorityQueue<>(Collections.reverseOrder());

int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
```

Nie używaj `Stack` (legacy). Nie używaj `LinkedList` jako listy — tylko jako deque jeśli musisz.

---

## 1. HashMap / Set — "czy widziałem"

**Kiedy:** para sumująca do targetu, duplikaty, anagramy, two-sum, subarray ze sumą.

**Two Sum**

```java
public int[] twoSum(int[] nums, int target) {
    Map<Integer, Integer> idx = new HashMap<>();
    for (int i = 0; i < nums.length; i++) {
        Integer j = idx.get(target - nums[i]);
        if (j != null) return new int[]{j, i};
        idx.put(nums[i], i);
    }
    throw new IllegalArgumentException("no pair");
}
```

O(n) / O(n). Powiedz: "nie sortuję, bo muszę zwrócić indeksy".

**Ćwicz:** Two Sum, Contains Duplicate, Group Anagrams, Longest Consecutive Sequence.

### Mechanizm

HashMap zamienia „znajdź w tym, co już minąłem” z pętli O(n) na odczyt O(1) średnio. Płacisz pamięcią O(n).

Niezmiennik Two Sum: po dojściu do indeksu `i` mapa trzyma wartości z `0..i-1` i ich indeksy. Szukasz `target - nums[i]` zanim wstawisz `nums[i]`, inaczej liczba sparuje się sama ze sobą.

Przejście `nums = [2,7,11,15]`, `target = 9`:

| i | liczba | szukam | mapa przed wstawieniem | wynik |
|---|--------|--------|------------------------|-------|
| 0 | 2 | 7 | {} | brak, wstaw 2→0 |
| 1 | 7 | 2 | {2:0} | jest, zwróć [0,1] |

`get` idzie tak: `hashCode` wybiera kubełek, w kubełku `equals` wybiera wpis. Dlatego kontrakt equals/hashCode jest tu widoczny, nie teoretyczny. Zły `hashCode` (stała) zamienia mapę w jedną listę i Two Sum staje się O(n²) przy ładnym kodzie.

Java 8 przy długiej kolizji i kluczach `Comparable` zamienia listę w kubełku na drzewo czerwono-czarne. Na rozmowie wystarczy: „średnio O(1), pesymistycznie gorzej, w praktyce kubełek nie jest gołą listą”.

Pułapki: zwracasz wartości zamiast indeksów; sortujesz i gubisz oryginalne indeksy; `Integer` porównujesz przez `==` (cache −128..127). `merge` do zliczania jest atomowe w ramach jednego wątku i czytelniejsze niż `get` + `put`.

Treść zadań: [Two Sum](https://leetcode.com/problems/two-sum/). Wizualizacja kubełków: [VisuAlgo Hashtable](https://visualgo.net/en/hashtable). Film z rozwiązaniem dopiero po swoim kodzie.

---

## 2. Two pointers

**Kiedy:** posortowana tablica, para, palindrom, container with most water, usuwanie inplace.

```java
public int removeDuplicates(int[] a) {
    if (a.length == 0) return 0;
    int w = 1;
    for (int r = 1; r < a.length; r++) {
        if (a[r] != a[w - 1]) a[w++] = a[r];
    }
    return w;
}
```

**Ćwicz:** Two Sum II (sorted), 3Sum (uwaga na duplikaty), Valid Palindrome, Trapping Rain Water (jeśli idzie dobrze).

### Mechanizm

Dwa wskaźniki działają, gdy da się udowodnić, że jednego końca nie warto już ruszać.

Na posortowanej tablicy suma pary: lewy `L`, prawy `R`. Suma za mała — większe liczby są tylko w prawo, więc `L++`. Suma za duża — `R--`. Każdy krok odrzuca jedną liczbę na zawsze, stąd O(n) po sortowaniu O(n log n).

To jest inny problem niż Two Sum z indeksami. Tam sortowanie niszczy pozycje, więc hash. Tu indeksy nie są potrzebne albo tablica już jest posortowana.

`removeDuplicates` trzyma wskaźnik zapisu `w` (pierwsze wolne miejsce w „czystym” prefiksie) i odczytu `r`. Wszystko przed `w` jest już bez duplikatów. `r` tylko czyta.

3Sum: sort, dla każdego `i` dwa wskaźniki na reszcie. Duplikaty pomijasz, gdy `nums[i] == nums[i-1]` po posortowaniu, inaczej ta sama trójka wpada kilka razy. To jest główny bug, nie sam algorytm.

Treść: [3Sum](https://leetcode.com/problems/3sum/), [Two Sum II](https://leetcode.com/problems/two-sum-ii-input-array-is-sorted/).

---

## 3. Sliding window

**Kiedy:** najdłuższy/najkrótszy podciąg spełniający warunek, "co najwyżej K", anagram w stringu.

Szablon:

```java
public int longestOnes(int[] nums, int k) {
    int left = 0, zeros = 0, best = 0;
    for (int right = 0; right < nums.length; right++) {
        if (nums[right] == 0) zeros++;
        while (zeros > k) {
            if (nums[left] == 0) zeros--;
            left++;
        }
        best = Math.max(best, right - left + 1);
    }
    return best;
}
```

Reguła: `right` zawsze idzie do przodu. `left` dogania, gdy warunek pęka.

### Mechanizm

Okno to dwa wskaźniki na **ciągłym** fragmencie plus stan tego fragmentu (liczba zer, licznik znaków, suma). Nie skaczesz. Każdy element wchodzi raz prawą krawędzią i wychodzi co najwyżej raz lewą, więc całość jest O(n), mimo `while` w środku.

Stan musi dać się zaktualizować w O(1) przy dołożeniu i zdjęciu elementu. Gdy aktualizacja stanu jest droga, okno przestaje być liniowe i zwykle trzeba innego wzorca.

Dwa warianty, myl je świadomie:

- **najdłuższe** okno spełniające warunek (`while` zdejmuje, aż znowu OK, potem `max`)
- **najkrótsze** okno spełniające warunek (rozszerzasz, aż OK, potem kurczysz i liczysz `min`)

Longest substring bez powtórzeń jest wariantem pierwszym. Stan to „ostatni indeks znaku” albo licznik w oknie. Gdy `s[right]` już jest w oknie, `left` wskakuje za poprzednie wystąpienie. Pułapka na `abba` i `dvdf`: `left` wolno tylko zwiększać. `Math.max(left, last+1)`, nigdy cofanie w lewo, bo okno by się rozjechało i złożoność też.

Minimum window jest trudniejsze: liczysz, ile unikalnych wymaganych znaków już masz, i kurczysz, dopóki okno nadal jest poprawne.

**Ćwicz:** Longest Substring Without Repeating, Minimum Window Substring (trudniejszy, ale częsty), Max Consecutive Ones III.

Treść: [Longest Substring](https://leetcode.com/problems/longest-substring-without-repeating-characters/), [Minimum Window](https://leetcode.com/problems/minimum-window-substring/). Rozwiązania po swoim P01.

---

## 4. Prefix sum

**Kiedy:** suma zakresu, subarray sum = k, różnica prefiksów.

```java
public int subarraySum(int[] nums, int k) {
    Map<Integer, Integer> seen = new HashMap<>();
    seen.put(0, 1);
    int sum = 0, ans = 0;
    for (int x : nums) {
        sum += x;
        ans += seen.getOrDefault(sum - k, 0);
        seen.merge(sum, 1, Integer::sum);
    }
    return ans;
}
```

Powiedz: `prefix[j] - prefix[i] = k` ⇒ szukam ile razy widziałem `sum - k`.

### Mechanizm

Suma od `i+1` do `j` włącznie to `prefix[j] - prefix[i]`. Szukasz par, dla których ta różnica wynosi `k`, czyli wcześniejszy prefiks równy `sum - k`.

Mapa trzyma, ile razy dana suma prefiksowa już wystąpiła. `seen.put(0, 1)` przed pętlą jest obowiązkowe: podtablica od początku tablicy ma „wcześniejszy prefiks” 0, którego nie było w pętli.

Przejście `nums = [1, 2, 3]`, `k = 3`:

| x | sum | sum−k | ile dopisuję | mapa po |
|---|-----|-------|--------------|---------|
| 1 | 1 | −2 | 0 | {0:1, 1:1} |
| 2 | 3 | 0 | 1 (całość 1+2) | {0:1, 1:1, 3:1} |
| 3 | 6 | 3 | 1 (sama trójka) | {0:1, 1:1, 3:1, 6:1} |

Wynik 2. Podtablica `[1,2]` i `[3]`.

To nie jest okno. Okno trzyma jeden ciągły fragment „na teraz”. Prefiks zlicza **wszystkie** wcześniejsze końce, także rozłączne. Przy samych dodatnich liczbach okno często wystarcza i jest prostsze. Przy ujemnych i zerach okno „kurcz aż suma spadnie” jest fałszywe, bo zdjęcie elementu może sumę zwiększyć. Wtedy zostaje prefiks.

Pułapka: `int` przy dużych sumach. Na rozmowie zapytaj o zakres albo bierz `long`.

Treść: [Subarray Sum Equals K](https://leetcode.com/problems/subarray-sum-equals-k/).

---

## 5. Stack (monotonic)

**Kiedy:** next greater, valid parentheses, daily temperatures, histogram.

```java
public int[] dailyTemperatures(int[] t) {
    int n = t.length;
    int[] ans = new int[n];
    Deque<Integer> st = new ArrayDeque<>(); // indeksy, malejące wartości
    for (int i = 0; i < n; i++) {
        while (!st.isEmpty() && t[i] > t[st.peek()]) {
            int j = st.pop();
            ans[j] = i - j;
        }
        st.push(i);
    }
    return ans;
}
```

**Ćwicz:** Valid Parentheses, Daily Temperatures, Largest Rectangle in Histogram (hard, tylko jeśli day 4+).

### Mechanizm

Zwykły stos pamięta rzeczy do zamknięcia później: nawias otwarty, wywołanie, ścieżka rekurencji. Zdejmujesz, gdy przychodzi para.

Stos monotoniczny pamięta **kandydatów, którzy jeszcze nie dostali odpowiedzi**, w kolejności wartości. W daily temperatures na stosie leżą indeksy temperatur malejąco od dołu (albo rosnąco — trzymaj jedną konwencję i powiedz ją). Gdy przychodzi cieplejszy dzień, zamyka wszystkich zimniejszych z wierzchu, bo dla nich to pierwszy cieplejszy. Chłodniejsi pod spodem czekają dalej.

Dlaczego O(n): każdy indeks wchodzi na stos raz i schodzi raz. `while` w środku nie robi z tego n².

Pułapki: trzymasz temperatury zamiast indeksów i nie umiesz policzyć odległości; używasz `Stack` (synchronizowany, stary) zamiast `ArrayDeque`; mylisz, który koniec deque jest wierzchołkiem. W `ArrayDeque` wierzchołek stosu to `push` / `pop` / `peek` od frontu. `addLast`/`pollFirst` to kolejka. Nie mieszaj w jednym zadaniu bez komentarza.

Treść: [Daily Temperatures](https://leetcode.com/problems/daily-temperatures/). Wizualizacja: przejdź `[73,74,75,71,69,72,76,73]` ręcznie, zanim odpalisz testy P03.

---

## 6. Binary search (w tym na odpowiedzi)

**Kiedy:** posortowane, albo "minimalne X takie, że warunek".

Szukanie lewej granicy:

```java
public int lowerBound(int[] a, int target) {
    int lo = 0, hi = a.length; // exclusive
    while (lo < hi) {
        int mid = lo + (hi - lo) / 2;
        if (a[mid] >= target) hi = mid;
        else lo = mid + 1;
    }
    return lo;
}
```

Search on answer (Koko eating bananas, ship packages):

```java
boolean can(int capacity) { /* ... */ }

int lo = min, hi = max;
while (lo < hi) {
    int mid = lo + (hi - lo) / 2;
    if (can(mid)) hi = mid;
    else lo = mid + 1;
}
return lo;
```

Na głos: "funkcja jest monotoniczna, więc mogę binsearchować próg".

### Mechanizm

Binsearch wymaga uporządkowania. Na tablicy: lewa połowa jest w całości za mała albo prawa w całości za duża, więc jedną połowę wyrzucasz.

`lo + (hi - lo) / 2` zamiast `(lo+hi)/2`, bo suma dwóch dużych `int` się przekręca.

Wariant z `while (lo < hi)` i `hi` wyłącznym szuka granicy, nie „czy jest”. Po pętli `lo == hi` i to jest pierwszy indeks, na którym warunek zaczął być prawdziwy. Jeśli wstawisz `hi = mid - 1` w tym wariancie, gubisz granicę. Jeśli trzymasz `while (lo <= hi)` i szukasz dokładnej wartości, inna konwencja — nie mieszaj ich w jednym kodzie.

Search on answer: wcale nie szukasz w tablicy. Szukasz najmniejszej liczby z zakresu `[min, max]`, dla której predykat `can` jest true. Predykat musi być monotoniczny. Przykład: minimalna ładowność statku. Jak dowozisz paczki przy capacity 10, dowozisz też przy 11. Jak nie dowozisz przy 7, nie dowozisz przy 6. Dlatego połowa zakresu odpada tak samo jak w tablicy.

Najpierw powiedz na głos dziedzinę (`lo` = max paczka, `hi` = suma) i co oznacza `can`. Potem kod. Odwrócony predykat (szukasz największego X takiego, że jeszcze się da) zmienia, którą stronę przesuwasz. Zapisz to zdaniem, zanim ruszysz `lo`/`hi`.

---

## 7. Heap / Top-K

**Kiedy:** k największych, merge k list, running median, scheduling.

```java
public int[] topKFrequent(int[] nums, int k) {
    Map<Integer, Integer> f = new HashMap<>();
    for (int x : nums) f.merge(x, 1, Integer::sum);

    PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
    for (var e : f.entrySet()) {
        pq.offer(new int[]{e.getKey(), e.getValue()});
        if (pq.size() > k) pq.poll();
    }
    return pq.stream().mapToInt(a -> a[0]).toArray();
}
```

Min-heap rozmiaru k = O(n log k). Powiedz to. Quickselect jest O(n) średnio — bonus, nie musisz kodować.

### Mechanizm

`PriorityQueue` w Javie to kopiec binarny. Domyślnie min: na wierzchu najmniejszy, `poll` zdejmuje najmniejszego w O(log n).

Top-k największych: trzymasz kopiec **min** rozmiaru k. Wierzchołek to najsłabszy z aktualnej elity. Nowy element lepszy od wierzchołka wyrzuca wierzchołek i wchodzi. Gorsi odpadają w O(log k). Po całym przejściu w kopcu zostało k najlepszych. Pełne sortowanie byłoby O(n log n) i robi robotę, której nie proszą.

Odwrócenie jest częstym bugiem: kopiec max rozmiaru k trzyma k **najmniejszych**, bo wyrzucasz największego z dołu. Powiedz na głos „min-heap, wierzchołek = najgorszy z topki”, zanim napiszesz komparator.

`PriorityQueue` nie jest współbieżna. Nie ma wydajnego „daj mi i-ty” — tylko wierzchołek. Gdy potrzebujesz obu końców (mediana na bieżąco), dwa kopce: max na mniejszej połowie, min na większej.

Wizualizacja: [VisuAlgo heap](https://visualgo.net/en/heap). Treść: [Top K Frequent](https://leetcode.com/problems/top-k-frequent-elements/).

---

## 8. BFS / DFS / graf

**Kiedy:** siatka, shortest path w nieważonym, islands, course schedule, clone graph.

BFS siatka (shortest path):

```java
public int shortestPathBinaryMatrix(int[][] g) {
    int n = g.length;
    if (g[0][0] == 1 || g[n-1][n-1] == 1) return -1;
    int[][] dirs = {{-1,-1},{-1,0},{-1,1},{0,-1},{0,1},{1,-1},{1,0},{1,1}};
    ArrayDeque<int[]> q = new ArrayDeque<>();
    q.add(new int[]{0, 0, 1});
    g[0][0] = 1; // visited in-place
    while (!q.isEmpty()) {
        int[] cur = q.poll();
        if (cur[0] == n - 1 && cur[1] == n - 1) return cur[2];
        for (int[] d : dirs) {
            int r = cur[0] + d[0], c = cur[1] + d[1];
            if (r >= 0 && r < n && c >= 0 && c < n && g[r][c] == 0) {
                g[r][c] = 1;
                q.add(new int[]{r, c, cur[2] + 1});
            }
        }
    }
    return -1;
}
```

Topological (Course Schedule) — Kahn:

```java
// indegree[], adj List<Integer>[]
// queue wszystkich z indegree 0
// zdejmuj, zmniejszaj sąsiadów
// jeśli processed < n → cykl
```

**Ćwicz:** Number of Islands, Course Schedule, Rotting Oranges, Clone Graph.

### Mechanizm

Graf na rozmowie to albo siatka (`int[][]` plus 4 kierunki), albo lista sąsiedztwa `List<Integer>[]`.

BFS idzie warstwami. Pierwszy raz, gdy dochodzisz do komórki, doszedłeś najkrótszą drogą w grafie nieważonym (każda krawędź kosztuje 1). Dlatego shortest path na siatce 0/1 bez wag to BFS, a DFS da jakąś drogę, nie najkrótszą. Odwiedzenie zaznaczasz **przy wkładaniu do kolejki**, nie przy zdejmowaniu. Inaczej ten sam sąsiad wpada wiele razy i złożoność się sypie.

DFS schodzi w głąb. Dobry do „oznacz całą wyspę”, składowych, cykli w malowaniu kolorami. Rekurencja na siatce 500×500 bywa głęboka — na rozmowie rekurencja jest OK, dopóki nie pytają o stos. Iteracyjny DFS to ten sam `ArrayDeque`, tylko `push`/`pop` zamiast kolejki.

Kahn (course schedule): krawędź `a → b` znaczy „a przed b”. `indegree[b]` to liczba rzeczy, które muszą się stać przed b. Kolejka startuje od węzłów z indegree 0 (nikt ich nie blokuje). Zdejmujesz węzeł, zmniejszasz sąsiadów, sąsiad z zerem wpada do kolejki. Jak przetworzysz mniej niż n węzłów, został cykl: reszta czeka na siebie.

Pułapka: budujesz krawędź odwrotnie do treści („kurs A wymaga B” to krawędź B → A, nie A → B). Narysuj dwa węzły, zanim napiszesz pętlę.

Wizualizacja: [VisuAlgo DFS/BFS](https://visualgo.net/en/dfsbfs). Treść: [Number of Islands](https://leetcode.com/problems/number-of-islands/), [Course Schedule](https://leetcode.com/problems/course-schedule/).

---

## 9. Intervals

**Kiedy:** merge, overlapping meetings, insert interval.

```java
public int[][] merge(int[][] intervals) {
    Arrays.sort(intervals, Comparator.comparingInt(a -> a[0]));
    List<int[]> out = new ArrayList<>();
    int[] cur = intervals[0];
    for (int i = 1; i < intervals.length; i++) {
        if (intervals[i][0] <= cur[1]) {
            cur[1] = Math.max(cur[1], intervals[i][1]);
        } else {
            out.add(cur);
            cur = intervals[i];
        }
    }
    out.add(cur);
    return out.toArray(new int[0][]);
}
```

Meeting rooms: sort start, heap po end. "Ile równoległych" = max heap size.

### Mechanizm

Po sortowaniu po początku odcinki są w kolejności, w jakiej „się zaczynają”. Jeden przebieg wystarcza, bo kandydat do scalenia z bieżącym jest tylko ten, który zaczyna się zanim bieżący się skończył. Późniejszy, który startuje po `cur[1]`, nie nachodzi i wszystko pomiędzy już rozpatrzyłeś.

Warunek nachodzenia: `next.start <= cur.end`. Przy zamkniętych końcach `[1,2]` i `[2,3]` nachodzą. Treść czasem mówi „dotykanie się liczy jako merge”, czasem nie. Powtórz to zdaniem, zanim napiszesz `<=`.

Scalanie bierze `max` końców, nie koniec nowszego. `[1,10]` + `[2,3]` zostaje `[1,10]`. Bug „`cur[1] = next[1]`” obcina długi odcinek.

Meeting rooms (ile sal): sort startów, kopiec min czasów końca trwających spotkań. Nowe spotkanie wyrzuca z kopca wszystkie, które już się skończyły (`end <= start`). Rozmiar kopca po dołożeniu to liczba sal w tej chwili. Maksimum z tych rozmiarów to odpowiedź.

Treść: [Merge Intervals](https://leetcode.com/problems/merge-intervals/).

---

## 10. Linked list

**Kiedy:** reverse, cycle, middle, merge two sorted.

```java
public ListNode reverse(ListNode head) {
    ListNode prev = null;
    while (head != null) {
        ListNode next = head.next;
        head.next = prev;
        prev = head;
        head = next;
    }
    return prev;
}
```

Floyd: `slow/fast`. Jeśli spotkanie — jest cykl. Middle: slow/fast, fast idzie 2×.

### Mechanizm

Lista na rozmowie to węzeł `val + next`. Nie masz indeksu, więc „idź wstecz” albo „środek” robisz wskaźnikami, nie pętlą po `i`.

Odwracanie: trzy referencje `prev`, `head`, `next`. Najpierw zapamiętujesz `next`, potem przepinasz `head.next` na `prev`, potem oba wskaźniki idą do przodu. Kolejność jest całą treścią zadania. Jedno przestawienie gubi ogon i zostaje cykl albo `null`.

Floyd: `slow` krok 1, `fast` krok 2. W cyklu szybszy dogania wolniejszego, bo różnica prędkości to 1. Brak spotkania (`fast` albo `fast.next` jest null) znaczy brak cyklu. Środek listy: ten sam układ, koniec gdy `fast` dojdzie do końca, `slow` stoi w połowie. Przy parzystej długości umów się, który środek zwracasz, i powiedz to.

Pułapka: dummy node przy usuwaniu głowy. Łatwiej mieć sztuczną głowę niż osobny przypadek `head` do skasowania.

---

## 11. Trees (BST + binary tree)

Tylko 4 rzeczy:

- inorder / pre / post (rekurencja + iteracja stackiem)
- max depth, level order (BFS)
- validate BST (min/max bounds, nie tylko lewy < node < prawy lokalnie)
- LCA

### Mechanizm

Inorder w BST daje wartości posortowane. To jest powód, dla którego walidacja i „k-ty najmniejszy” żyją na inorderze. Preorder: węzeł, lewe, prawe. Postorder: dzieci, potem węzeł (usuń drzewo, policz wysokość od dołu).

Level order to BFS z kolejką. Rozmiar kolejki na starcie warstwy mówi, ile węzłów należy do tej warstwy. Bez tego snapshotu mieszasz poziomy.

`isValidBST` z samych porównań „lewy < node i prawy > node” jest fałszywe. Lewe poddrzewo prawej gałęzi może złamać ograniczenie dziadka i lokalny test tego nie widzi. Niesiesz zakres `(lo, hi)` w dół. Na lewo `hi` staje się wartością węzła, na prawo `lo`.

`long` na granicach, bo `Integer.MIN_VALUE` jako węzeł przy `int` granicach bywa równy limitowi i test się wywraca. Start: `lo = Long.MIN_VALUE`, `hi = Long.MAX_VALUE`.

LCA w drzewie binarnym (niekoniecznie BST): jak lewe poddrzewo i prawe oba zwracają nie-null, bieżący węzeł jest LCA. Jak tylko jedna strona coś zwraca, oddajesz to wyżej. W BST da się zejść w jedną stronę, porównując z wartościami, ale wersja ogólna jest tą, którą warto umieć napisać.

Wizualizacja: [VisuAlgo BST](https://visualgo.net/en/bst).

```java
public boolean isValidBST(TreeNode n, long lo, long hi) {
    if (n == null) return true;
    if (n.val <= lo || n.val >= hi) return false;
    return isValidBST(n.left, lo, n.val) && isValidBST(n.right, n.val, hi);
}
```

---

## 12. DP — tylko klasyki, jeśli zostaje czas

Na express: **1D DP** i tyle.

- Climbing stairs / House robber (stan: brać / nie brać)
- Coin change (unbounded knapsack)
- LIS (n log n bonus)

```java
public int coinChange(int[] coins, int amount) {
    int[] dp = new int[amount + 1];
    Arrays.fill(dp, amount + 1);
    dp[0] = 0;
    for (int a = 1; a <= amount; a++) {
        for (int c : coins) {
            if (c <= a) dp[a] = Math.min(dp[a], dp[a - c] + 1);
        }
    }
    return dp[amount] > amount ? -1 : dp[amount];
}
```

Hard DP odpuść. Lepiej 2 clean medium niż pół harda.

### Mechanizm

DP to brute force z pamięcią. Najpierw rekurencja „odpowiedź dla mniejszego problemu”, potem tablica, żeby nie liczyć tego samego tysiąc razy.

Coin change (najmniejsza liczba monet): `dp[a]` = najmniejsza liczba monet, która składa się na kwotę `a`. Przejście: `dp[a] = min(dp[a - coin] + 1)` po monetach, które mieszczą się w `a`. `dp[0] = 0`, bo zero monet składa się na zero. Wypełnienie `amount + 1` jest wartownikiem „nie da się” — każda prawdziwa odpowiedź jest ≤ amount (same monety 1).

Kolejność pętli ma znaczenie i warto ją powiedzieć. Zewnętrzna po kwocie, wewnętrzna po monetach: każda moneta może wejść wiele razy (unbounded). Zewnętrzna po monetach i wewnętrzna po kwocie w górę to ten sam unbounded knapsack, inny porządek. Kwota w dół przy zewnętrznej pętli monet to 0/1: moneta wchodzi raz. Na rozmowie nazwij, który wariant piszesz.

House robber: `dp[i] = max(dp[i-1], dp[i-2] + nums[i])`. Albo biorę dom i nie biorę poprzedniego, albo omijam dom. Dwie zmienne zamiast tablicy, gdy stan zależy tylko od dwóch poprzednich.

Nie zaczynaj kodu od tablicy. Najpierw zdanie: „stan, przejście, baza, kolejność”. Jak tego nie ma, DP jest zgadywaniem indeksów.

---

## Typowe pułapki na HackerRank (Java)

- `Scanner` jest wolny — na dużym n użyj `BufferedReader` jeśli platforma pozwala.
- overflow: `int` vs `long` przy sumach i `lo + (hi-lo)/2`.
- `Integer` cache vs `==` — porównuj `equals` albo `int`.
- mutowalność tablicy wejściowej — zapytaj, czy wolno psuć in-place.
- sort custom objects: `Comparator.comparingInt`.
- timeout = zwykle O(n²) przy n=1e5.

## Mini-zestaw na 3 dni (rób na czas)

Dzień 1: Two Sum, Longest Substring Without Repeating, Daily Temperatures, Number of Islands
Dzień 2: 3Sum, Top K Frequent, Course Schedule, Merge Intervals
Dzień 3: Binary Search (Koko / Search in Rotated), Subarray Sum Equals K, LRU Cache, Coin Change

LRU Cache wychodzi jako "zakoduj klasę". Musisz umieć `LinkedHashMap` albo HashMap + doubly linked list.

```java
class LRUCache extends LinkedHashMap<Integer, Integer> {
    private final int cap;
    LRUCache(int cap) { super(cap, 0.75f, true); this.cap = cap; }
    public Integer get(int k) { return super.getOrDefault(k, -1); }
    public void put(int k, int v) { super.put(k, v); }
    protected boolean removeEldestEntry(Map.Entry<Integer, Integer> e) {
        return size() > cap;
    }
}
```

Na rozmowie powiedz, że znasz `accessOrder=true`, ale umiesz też ręczną listę.

## Jak ja Cię będę oceniał na mocku

- Cisza > 30 s bez narracji = minus
- Od razu kod bez przykładu = minus
- Działa, ale bez brzegów = mid
- Działa + złożoność + 2 testy brzegowe + czytelne nazwy = senior
