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

**Ćwicz:** Longest Substring Without Repeating, Minimum Window Substring (trudniejszy, ale częsty), Max Consecutive Ones III.

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

---

## 11. Trees (BST + binary tree)

Tylko 4 rzeczy:

- inorder / pre / post (rekurencja + iteracja stackiem)
- max depth, level order (BFS)
- validate BST (min/max bounds, nie tylko lewy < node < prawy lokalnie)
- LCA

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
