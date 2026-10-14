# Java + Spring — pytania, które wychodzą naprawdę

Odpowiadaj w 45–90 sekund: definicja → dlaczego to boli w produkcji → przykład.

Pod spodem jest mechanizm do tych pytań, które padają prawie zawsze. Linki i limity: `09-materialy.md`. Na dzień 3 blok B siadaj do Behlera o `@Transactional`, nie do całego spisu Springa.

## Java core (must)

### equals / hashCode

Kontrakt: równe obiekty → ten sam hash. Jeśli psujesz `hashCode`, `HashMap`/`HashSet` Cię zgubi.

- `equals` zwrotny, symetryczny, przechodni, spójny.
- Pola użyte w `equals` muszą być w `hashCode`.
- Nie używaj mutowalnych pól jako klucza w mapie.
- Preferuj `record` / Guava / `Objects.hash`.

Pułapka: `Id` encji JPA. equals po ID zanim encja ma ID = ból w Set. Często equals po business key albo w ogóle nie wkładasz encji do HashSet przed persistem.

### Co HashMap robi z kontraktem

`get` liczy hash, wybiera kubełek, w kubełku idzie po wpisach i woła `equals`. Dwa obiekty równe `equals` muszą trafić do tego samego kubełka, inaczej `get` ich nie znajdzie. Stąd: równe obiekty → ten sam `hashCode`. Odwrotność nie obowiązuje. Ten sam hash może mieć dwa różne obiekty, wtedy `equals` je rozróżnia. To jest kolizja, nie błąd.

Złamanie, które widać na rozmowie: `equals` patrzy na `id` i `name`, `hashCode` tylko na `id`. Wstawiasz `("a", id=1)`, mutujesz nazwę na `"b"` w kopii, która `equals` mówi że równa, a hash już inny. Mapa nie znajduje klucza. Drugie złamanie: klucz mutowalny. Włożyłeś go, zmieniłeś pole użyte w hashu, wpis zostaje w starym kubełku.

Encja JPA przed `persist` ma `id == null`. Dwie takie encje są równe, jeśli equals jest po id. `HashSet` traktuje je jako jeden element. Po zapisie dostają różne id i zbiór, który już je „ma”, jest niespójny z equals. Dlatego albo equals po kluczu biznesowym, który istnieje od konstrukcji, albo nie wkładasz encji do hasha przed persistem.

`record` dostaje equals i hashCode ze składowych. Dopóki składowe są niemutowalne, klucz w mapie jest bezpieczny.

Tekst z kontraktowymi przykładami: [equals i hashCode](https://www.baeldung.com/java-equals-hashcode-contract).

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

### Co z tego wynika w kodzie

Wątek nie ma obowiązku widzieć zapisu innego wątku „od razu”. Bez relacji happens-before kompilator i CPU wolno trzymać wartość w rejestrze. Pętla `while (running)` na zwykłym `boolean running` może kręcić się w nieskończoność, mimo że inny wątek ustawił `running = false`. `volatile` na tym polu wstawia relację: zapis volatile dzieje się przed kolejnym odczytem volatile tego pola.

`volatile` nie skleja kilku operacji. `volatile int i; i++` to odczyt, dodanie, zapis. Dwa wątki gubią zwiększenia. `AtomicInteger.incrementAndGet` robi to jedną operacją CAS: „jeśli wciąż widzę starą wartość, podmień na nową, inaczej powtórz”. Przy liczniku, w który wszyscy walą, CAS się tłucze i `LongAdder` rozkłada licznik na kilka komórek, sumowanych przy odczycie.

`synchronized` daje i wzajemne wykluczenie, i happens-before: wszystko zapisane przed zwolnieniem monitora jest widoczne po jego zajęciu. To jest powód, dla którego „wystarczy volatile” pada przy strukturze z kilku pól. Lista „dodaj węzeł” pod volatile samego referera i tak potrafi być widziana w kawałku.

Krótko, bez całej książki o JMM: [Jenkov — Java Memory Model](https://jenkov.com/tutorials/java-concurrency/java-memory-model.html), sekcje o volatile i o blokadzie.

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

### Co się dzieje przy wywołaniu

Kontener nie owija Twojego obiektu magią w środku metody. Przy starcie tworzy proxy: albo JDK proxy (interfejs), albo podklasę CGLIB. Do innych beanów wstrzykuje proxy, nie goły obiekt.

Wywołanie z zewnątrz:

1. Wchodzi w proxy.
2. `TransactionInterceptor` woła `PlatformTransactionManager.getTransaction`.
3. Menadżer bierze połączenie z puli, wyłącza auto-commit, ewentualnie dołącza do transakcji, która już wisi na tym wątku (`REQUIRED`).
4. Woła Twoją metodę.
5. Brak wyjątku: commit. Wyjątek z listy rollback: rollback. Potem połączenie wraca do puli.

`this.metoda()` w tej samej klasie idzie po referencji `this`, która jest celem, nie proxy. Interceptor nie startuje. Dlatego prywatna metoda i wywołanie wewnętrzne nie dostaną transakcji, nawet z adnotacją. `@Transactional` na metodzie niepublicznej też nie, bo proxy nie umie jej przechwycić z zewnątrz.

Domyślna reguła rollbacku: `RuntimeException` i `Error` cofają, checked exception commituje. To zaskakuje przy `IOException` z klienta HTTP w środku metody, która zdążyła zrobić `UPDATE`. `rollbackFor = Exception.class` na granicy use-case jest świadomym wyborem, powiedz go.

`REQUIRES_NEW` zawiesza bieżącą transakcję i otwiera drugą, na drugim połączeniu. Wewnętrzny commit jest widoczny, nawet gdy zewnętrzny potem się wywali. Używasz tego do „zapisz audyt niezależnie od biznesu”. Nie używasz tego, żeby ominąć lock, który sam trzymasz na pierwszym połączeniu: drugie połączenie będzie na ten lock czekać, aż pierwsze skończy, a pierwsze czeka na drugie. To jest deadlock z propagacji, nie z bazy „samej z siebie”.

Połączenie jest zajęte od wejścia w metodę do commitu. HTTP w środku, na 2 sekundy, przy puli 20 i 50 równoległych requestach, kończy się kolejką po połączenie, podczas gdy baza nic nie liczy. Wzorzec: transakcja na zapisie, wołanie sieci poza nią, idempotentny zapis wyniku.

Na dzień 3: [Marco Behler, @Transactional](https://www.marcobehler.com/guides/spring-transaction-management-transactional-in-depth). Ściąga atrybutów: [dokumentacja](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html).

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

### N+1 jako ślad SQL

`orderRepository.findAll()` robi `SELECT * FROM orders`. W pętli `order.getCustomer().getName()` przy `FetchType.LAZY` odpala osobny `SELECT * FROM customers WHERE id=?` na każde zamówienie. 200 zamówień = 201 zapytań. Persistence context (L1) w tej samej transakcji deduplikuje po id encji, więc ten sam klient nie poleci dwa razy. Stu różnych klientów poleci sto razy.

`JOIN FETCH` w JPQL dokleja klienta w tym samym SELECT. `@EntityGraph` robi to samo bez ręcznego joinu. `@BatchSize` albo `hibernate.default_batch_fetch_size` zbija dociągnięcia do `WHERE id IN (...)`. Najpierw włącz log SQL (datasource-proxy, p6spy), potem wybieraj. `show-sql` na produkcji zostaw wyłączone.

LAZY poza sesją rzuca `LazyInitializationException`, bo nie ma już połączenia, którym dałoby się dociągnąć. OSIV (`spring.jpa.open-in-view=true`, domyślnie w Boot) trzyma sesję przez cały request HTTP, więc widok albo serializer Jacksona dociąga leniwie i wygląda, jakby problemu nie było. Koszt: połączenie wisi od kontrolera do końca zapisu odpowiedzi, a N+1 chowa się w serializacji. Na API wyłączasz OSIV i oddajesz DTO złożone w transakcji.

[N+1](https://vladmihalcea.com/n-plus-1-query-problem/), [open-in-view](https://vladmihalcea.com/the-open-session-in-view-anti-pattern/).

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
