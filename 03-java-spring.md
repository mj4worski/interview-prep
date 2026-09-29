# Java + Spring — pytania, które wychodzą naprawdę

Odpowiadaj w 45–90 sekund: definicja → dlaczego to boli w produkcji → przykład.

## Java core (must)

### equals / hashCode

Kontrakt: równe obiekty → ten sam hash. Jeśli psujesz `hashCode`, `HashMap`/`HashSet` Cię zgubi.

- `equals` zwrotny, symetryczny, przechodni, spójny.
- Pola użyte w `equals` muszą być w `hashCode`.
- Nie używaj mutowalnych pól jako klucza w mapie.
- Preferuj `record` / Guava / `Objects.hash`.

Pułapka: `Id` encji JPA. equals po ID zanim encja ma ID = ból w Set. Często equals po business key albo w ogóle nie wkładasz encji do HashSet przed persistem.

### Collections — co wybrać

| Potrzeba | Struktura |
|----------|-----------|
| k→v, brak kolejności | HashMap |
| k→v, kolejność wstawiania | LinkedHashMap |
| k→v, posortowane klucze | TreeMap |
| współbieżny k→v | ConcurrentHashMap (nie Hashtable) |
| unikalne, kolejność | LinkedHashSet |
| kolejka priorytetów | PriorityQueue (nie thread-safe) |
| deque / stack | ArrayDeque |

`HashMap` Java 8+: bucket jako drzewo przy kolizjach (czerwono-czarne), nie tylko lista.

`ConcurrentHashMap`:
- nie blokuje całej mapy (segmenty / CASowe bin-y)
- `size()` jest estymatą w trakcie mutacji
- iteracja weakly consistent — bez CME
- `compute` / `computeIfAbsent` atomowe na klucz
- nie dawaj `null` key/value

### String, intern, immutability

String jest niemutowalny → bezpieczny jako klucz, thread-safe. `+` w pętli = wiele kopii → `StringBuilder`.
`intern()` prawie nigdy nie ruszaj na rozmowie poza "pula stringów, ryzyko metaspace".

### Generics

Type erasure. `List<String>` i `List<Integer>` to w runtime `List`.
`? extends T` — producer (get), `? super T` — consumer (put). PECS.
Nie zrobisz `new T[]`.

### Optional

Tylko jako return. Nie jako field, nie jako param, nie `Optional.get()` bez sprawdzenia.
`orElse(getX())` zawsze wywoła `getX()` — użyj `orElseGet`.

### Streams

Nie świętość. Czytelna pętla > stream z 8 side-effectami.
`parallelStream()` tylko gdy CPU-bound, dużo danych, brak shared mutable. I tak na interview: "nie domyślnie".

Checked exceptions w streamach bolą — opakowujesz w unchecked. To zapach.

---

## Concurrency (must, poziom senior intuicja)

### Thread vs Executor

Nie twórz `new Thread` per request. Pula:

```java
ExecutorService pool = new ThreadPoolExecutor(
    core, max, 60, SECONDS, new LinkedBlockingQueue<>(1000),
    new ThreadFactoryBuilder().setNameFormat("w-%d").build(),
    new ThreadPoolExecutor.CallerRunsPolicy());
```

Powiedz: bounded queue + rejection policy. Nieograniczona `LinkedBlockingQueue` + default `Executors.newFixedThreadPool` = OOM przy spike.

### synchronized vs Lock vs concurrent collections

- `synchronized` — proste monitor, reentrant, nie interruptible wait łatwo
- `ReentrantLock` — tryLock, fairness, condition
- wyżej: nie dziel stanu. Immutability, message passing, ConcurrentHashMap, LongAdder

### volatile vs Atomic

`volatile` = widoczność, nie atomowość `++`.
`AtomicInteger` = CAS. `AtomicReference`.
`LongAdder` lepszy przy dużej kontencji na liczniku.

### happens-before (intuicja)

Zapis przed unlock jest widoczny po lock. To samo: volatile write → read, start/join wątku, completion Future.

### Deadlock

4 warunki: mutual exclusion, hold&wait, no preemption, circular wait.
Fix: stała kolejność locków, timeout, mniej locków, lock-free strukturę.

### ThreadLocal

Dane per wątek (user id, MDC). W puli **musisz czyścić** (`try/finally remove`), inaczej wyciek / wyciek danych między requestami. W virtual threads (21) ThreadLocal jest droższy — mention tylko jeśli pytają.

---

## JVM / GC (wystarczy 90 sekund)

- Heap: young (eden + survivors) + old. Metaspace poza heap (klasy).
- Minor GC częsty i tani. Full GC boli.
- Dziś default często G1. ZGC/Shenandoah gdy duże heapy i niski pause.
- `OutOfMemoryError: heap` ≠ metaspace ≠ GC overhead limit
- Narzędzia: `jcmd`, `jfr`, heap dump + Eclipse MAT / VisualVM
- Escape analysis / TLAB — bonus, nie musisz

Na pytanie "jak debugowałeś latencję": JFR + alloc pressure + lock contention + GC pauses + pool saturation. Nie "zwiększyłem heap".

---

## Spring (must)

### IoC / DI / Bean

Kontener tworzy graf zależności. Constructor injection > field injection (testowalność, immutability, brak half-ready beanów).

Zakresy: `singleton` (default), `prototype`, `request`, `session`.
Pułapka: singleton z wstrzykniętym prototype — dostaniesz jedną instancję. `ObjectProvider` / `@Lookup`.

### Lifecycle

`constructor → @Autowired fields → @PostConstruct → afterPropertiesSet → ready`
Destroy: `@PreDestroy`.

Circular dependency: constructor injection jej nie ukryje (to plus). Setter/field + `@Lazy` maskuje problem. Na rozmowie: "lepiej rozbić design".

### AOP i proxy

`@Transactional`, `@Async`, `@Cacheable` działają przez proxy.

**Self-invocation:** `this.foo()` omija proxy → brak transakcji / cache. Fix: wywołaj przez innego beana, self-inject, albo `AopContext`.

Proxy: JDK (interfejs) vs CGLIB (klasa). `final` metody nie zostaną opakowane.

### @Transactional — to pytanie padnie

Powiedz w tej kolejności:

1. AOP proxy otwiera transakcję przed metodą publiczną, commit/rollback po.
2. Default rollback: **unchecked** (`RuntimeException`). Checked **nie** rollbackuje, chyba że `rollbackFor`.
3. `propagation`: `REQUIRED` (default, join), `REQUIRES_NEW` (zawiesza i otwiera nową — ostrożnie z lockami), `NOT_SUPPORTED`, `MANDATORY`.
4. `isolation` — patrz bazy.
5. `readOnly=true` — hint, w Hibernate może skipować dirty check.
6. Transakcja tylko na public method z zewnątrz beana.
7. Długa transakcja + HTTP call w środku = trzymasz połączenie z puli. Zło.

### Spring MVC / Boot

- `@RestController` = `@Controller` + `@ResponseBody`
- walidacja: `@Valid` + `MethodArgumentNotValidException` → `@ControllerAdvice`
- `ResponseEntity` gdy status jest częścią kontraktu
- filter / interceptor / ControllerAdvice — kolejność: filter → dispatcher → interceptor → controller → advice
- Boot: auto-config + starter + `application.yml`. Wyłączasz auto-config świadomie, nie magią

### Spring Data / JPA

- `EntityManager` persistence context = cache L1 w transakcji
- LAZY poza transakcją → `LazyInitializationException`
- N+1: `join fetch` / `@EntityGraph` / batch size. Najpierw loguj SQL (`show-sql` nie na prod, p6spy / datasource-proxy)
- `save()` na nowej encji vs detached — wiedz różnicę persist/merge
- OSIV (`open-in-view`) default w Boot = leniwe dociąganie w widoku. Na API często wyłączasz.

### Spring Security (wystarczy szkic)

Filter chain → Authentication → Authorization.
Stateless API: JWT / opaque token, bez sesji (`SessionCreationPolicy.STATELESS`).
Hasła: BCrypt, nie SHA. CSRF wyłączony przy stateless + nie-cookie, włączony przy cookie session.

### Testy

`@SpringBootTest` — ciężki, cały kontekst.
`@WebMvcTest` — slice kontrolera.
`@DataJpaTest` — slice JPA.
Mockito na unit. Testcontainers na repo / Kafka gdy mówisz o jakości.

---

## Pytania "grill" — krótkie odpowiedzi wzorcowe

**Czym różni się `==` od `equals`?**
`==` to tożsamość referencji (dla prymitywów wartość). `equals` równość logiczna. Stringi z literałów mogą dać `==` true przez pool — nie polegaj.

**Fail-fast vs weakly consistent iterator?**
`ArrayList` — CME przy strukturalnej zmianie. `ConcurrentHashMap` — widzi trochę stary, trochę nowy stan, nie wywala.

**Checked vs unchecked?**
Checked wymusza obsługę — dziś często owija się w runtime, bo checked nie przechodzi przez lambdy i psuje API. Na granicach systemu (IO) ma sens.

**Co robi `final`?**
Zmienna: referencja stała, obiekt może być mutowalny. Klasa: brak dziedziczenia. Metoda: brak override (i AOP CGLIB nie opakuje).

**Bean vs component vs service vs repository?**
`@Component` stereotype. `@Service`/`@Repository` to specjalizacje (repo tłumaczy persistence exceptions). Semantyka dla ludzi + AOP.

**Jak działa Spring Boot starter?**
`spring.factories` / `AutoConfiguration.imports` + `@ConditionalOn*`. Klasy auto-config wchodzą, gdy classpath i brak własnego beana.

**Jak zrobisz retry + idempotencję?**
Retry z backoff (Resilience4j) tylko na idempotentnych / świadomie. Idempotency-Key w tabeli unique. Timeout i circuit breaker osobno.

**Virtual threads (21)?**
Lekkie wątki na carrier. Super na IO-bound. Nie blokuj carrier synchronized na długo, uważaj na pooling. Nie przepisujesz wszystkiego na rozmowie — "pasuje do request-per-thread IO".
