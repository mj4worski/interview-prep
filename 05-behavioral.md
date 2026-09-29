# Behavioral — sprzedaż 6 lat w jednej firmie

Po długim stażu boją się dwóch rzeczy: że utknąłeś i że nie umiesz opowiadać o impakcie. Rozbroisz to historiami, nie sloganami.

## Format STAR (90 sekund)

- **S** (1 zdanie) — kontekst, skala, kto cierpiał
- **T** (1 zdanie) — Twoja odpowiedzialność, nie zespołu ogółem
- **A** (40 s) — 2–3 decyzje, które TY podjąłeś
- **R** (20 s) — liczba, zanim/po, co się nauczyłeś

Zakazane: "my jako zespół zrobiliśmy microservices".
Wymagane: "ja zaproponowałem X, przekonałem Y, wynik Z".

## 6 historii, które musisz mieć na kartce

Wypełnij własne fakty. Poniżej szablony — podmień nawiasy.

### 1. Impact / ownership (najważniejsza)

"W [systemie] p99 [endpointu] skoczyło do [X ms] przy [Y RPS]. Wziąłem [pomiar: JFR / EXPLAIN / logi]. Znalazłem [N+1 / brak indeksu / lock]. Zrobiłem [konkret]. p99 spadło do [Z], [koszt / incidentów / konwersja]."

### 2. Konflikt techniczny

"Tech lead chciał [duży rewrite]. Ja argumentowałem [mniejszy krok], bo [ryzyko / czas]. Zrobiłem spike / metryki. Uzgodniliśmy [kompromis]. Dzięki temu [dostarczyliśmy w terminie / uniknęliśmy outage]."

### 3. Awaria / on-call

"Alert [Y]. Najpierw [mitigacja: rollback / feature flag / scale]. Potem przyczyna [X]. Fix [Y]. Follow-up: [runbook, test, dashboard]. MTTD/MTTR: [liczba jeśli masz]."

### 4. Mentoring / leadership bez tytułu

"Junior / zespół grzązł w [X]. Wprowadziłem [RFC, pairing, checklistę review, ADRs]. Efekt: [czas onboardingu, mniej revertów, kto awansował]."

### 5. Porażka

Wybierz prawdziwą. Senior bez porażki wygląda na słabego.
"Złe założenie [X]. Skutek [Y]. Ja powiedziałem o tym [wcześnie / za późno]. Naprawa [Z]. Od wtedy [proces]."

Nie: "jestem perfekcjonistą". Tak: zła estymata, zły index, za szeroki scope, produkcyjny bug po Twoim review.

### 6. Dlaczego odchodzisz po 6 latach

Struktura:

1. Co było dobre (szacunek, nie plucie)
2. Co się skończyło (wzrost, skala, wpływ, technologia)
3. Czego szukasz konkretnie u nich (domena, skala, ownership)
4. Most: "chcę znów być w miejscu, gdzie [X], a u Was widzę [Y z ogłoszenia / produktu]"

Przykład szkieletu:

"Zostałem 6 lat, bo [produkt / ludzie / skala] dawały realny impact. Ostatnie [N] miesięcy wzrost spłaszczał: [mniej zielonego pola / decyzje wyżej / stack zamrożony]. Szukam roli, gdzie [projektuję systemy X, prowadzę Y]. Wasz [produkt / problem skali] to dokładnie ten rodzaj problemu, przy którym chcę pracować."

Nie mów: "wypalenie", "słaby manager", "mało płacą" — nawet jeśli prawda. To wyjdzie jako ryzyko.

## Pytania, które padną prawie zawsze

**Opowiedz o sobie (2–3 min).**
Nie CV od 2013. 4 beaty:
1. Kim jesteś teraz (senior backend, Java, systemy X)
2. Najmocniejszy wątek z ostatnich 6 lat (1 produkt, 1 liczba)
3. Jak pracujesz (ownership, produkcja, mentoring)
4. Dlaczego ta rozmowa

**Jaka była Twoja rola vs title?**
Jeśli nie byłeś formalnym leadem: "lead techniczny feature'a / obszaru, nie line manager". Daj przykład decyzji, którą przepchnąłeś.

**Najtrudniejszy stakeholder**
Produkt chce wszystko na wczoraj. Ty: opcje A/B/C z kosztem ryzyka. Wybór ich, nie "powiedziałem nie".

**Pytanie o lukę / rdzę interview**
Bądź szczery i krótki: "6 lat deep work, live coding trenuję teraz. Produkcyjnie robię X codziennie." Potem pokaż mięsień na zadaniu.

## Pytania, które TY zadajesz (koniec rozmowy)

Wybierz 3, nie 10.

1. Jaki problem systemowy zespół musi rozwiązać w najbliższe 2 kwartały?
2. Jak wygląda on-call i kto naprawdę niesie p99?
3. Co odróżnia u Was seniora, który działa, od seniora, który tylko koduje taski?
4. Gdzie ostatni design review się spalił i dlaczego?
5. Jak mierzony jest impact inżyniera? (nie story pointy)

To pokazuje, że nie szukasz "fajnego stacku", tylko miejsca do roboty.

## Szybki audyt historii

Dla każdej z 6 opowieści dopisz na kartce:

- liczba (latency, $ , czas, incydenty, ludzie)
- Twoja decyzja (czasownik: zmierzyłem, zaproponowałem, zablokowałem, z mentorem)
- tradeoff, który świadomie wziąłeś

Jeśli nie ma liczby — oszacuj rząd wielkości. "kilka razy dziennie padało" > "było źle".
