# Jak działa rozmowa na Karat

Karat nie jest firmą, do której aplikujesz. Goldman (albo inny klient) zleca im pierwszy techniczny screen. Rozmawiasz z **Interview Engineer**: obcy senior, który przeprowadza takie rozmowy zawodowo. Nie jest z zespołu, do którego idziesz. Nie sprzedajesz mu historii z CV. On ma rubrykę i trzyma czas.

Po rozmowie klient dostaje nagranie (obraz, głos, kod, playback) i ustrukturyzowany raport. Decyzję podejmuje rekruter, nie osoba z Karat. Wynik zwykle wraca w kilka dni roboczych.

## Przebieg tej konkretnej rubryki

Mail od Karat bywa ogólnikowy („godzina, intro, dyskusja, kod”). Twoje screeny są dokładniejsze i nimi się trzymaj:

1. **Intro, około 5 minut.** Poza 50 minutami z tabelki. Kim jesteś, jedna-dwie minuty. Nie opowiadaj kariery.
2. **System design, 20 minut.** Kilka krótkich scenariuszy, nie jeden system od zera. GCB4: „co tu jest słabe, gdzie jest SPOF, co z danymi i bezpieczeństwem”. GCB5: „jak to udźwignąć, gdzie jest wąskie gardło, jak wygląda zapis i notyfikacja”.
3. **Kod, 30 minut.** Gotowe klasy. Część A: padający przypadek, znajdź bug. Część B i C dochodzą po kolei: parsowanie linii, potem agregacja. **Nie ma części B, dopóki A nie przechodzi.** Tak Karat prowadzi zadania wieloczęściowe.

Na dole rubryki widać progi rekomendacji (further review / next round / tech touch). Kod ma twardy próg minimalny (60 na GCB4, 72 na GCB5). Design na screenie tego progu nie pokazuje. Najpierw kod.

„View sample questions” na tym ekranie, jeśli da się kliknąć, jest warte więcej niż scenariusze z tego repo. Wklej je tutaj, podmienimy trening.

## Karat Studio

Edytor w przeglądarce, zbliżony do VS Code. Chrome albo Firefox. Safari bywa kapryśny. Tablet odpada: kod i wideo naraz.

Nie stawiasz JDK u siebie. Oni odpają kod. Możesz odpalać testy wielokrotnie. Składnia, która się nie kompiluje, mocno boli, bo stoisz w miejscu.

Wolno sprawdzić **dokumentację** (sygnatura `Map.merge`, `split`, `Integer.parseInt`). Powiedz to na głos: „I'm looking up the Map API”. Nie wklejaj rozwiązania z Google, Stack Overflow ani z Chata. Polityka Karat traktuje generowanie rozwiązania przez AI jak ściąganie. Nagranie ogląda klient. Na tym screenie bezpiecznie jest zostać przy dokumentacji Javy.

Big-O zapytają, przy wyborze struktury albo na końcu części. Optymalność jest druga. Pierwsze jest: **działa i jest skończone**.

Język wybierasz Ty. Bierz Javę. Python jest krótszy w klasycznym LeetCode, ale ta rubryka to gotowy kod, parsowanie i obiekty. Przepinanie się na język, w którym nie czytasz cudzej klasy, jest stratą.

Nie wiadomo, którą wersję JDK ma ich edytor. Pisz jak w Javie 11: zwykłe klasy, pętle, `List`, `Map`. Bez records, bez pattern matchingu, bez kombinowania streamami, chyba że stream jest krótszy od pętli i umiesz go napisać za pierwszym razem.

## Czego tu nie będzie

- Whiteboard „zaprojektuj Twittera” na 45 minut.
- Grafy, DP, trudne medium z LeetCode. Karat wprost mówi, że sprawdza kodowanie, nie ezoteryczne algorytmy.
- Grilla ze Springa. Jak padnie jedno pytanie z Javy („czym jest `equals` i `hashCode`”), masz to w W1. Nie ucz się dziś `@Transactional`.

Jest osobny produkt, Karat NextGen: duży wieloplikowy projekt i asystent AI przez ~50 minut. **Twoje screeny to nie jest NextGen.** Jak w mailu jest „AI assistant” albo „multi-file codebase”, zatrzymaj się i napisz — to inna rozmowa.

## Redo, przesunięcie, awaria

Jak screen pójdzie wyraźnie poniżej Ciebie (rozsypany setup, panika, nie weszła jedna część), **redo zgłaszasz w 24 godziny**. Nowy interviewer, nowe pytania. Do klienta idą oba raporty. Karat radzi im brać wyższy wynik, ale pierwsze nagranie i tak mają. To nie jest plan „pierwszy raz na pół gwizdka”. Redo ma sens, gdy wiesz, co poszło nie tak i drugi raz zrobisz to ciaśniej.

Przesunąć termin można dwa razy łącznie z redo. Lepiej ≥ 24 h wcześniej.

Jak padnie audio albo wytną Was wcześniej, support Karat umawia powtórkę. Jak milczą: `support@karat.io`.

## Jak to wygląda punktowo

GCB4 waży design i kod po równo. Design jest diagnostyczny: znajdź słabość.

GCB5 waży kod 60% i ma wyższy próg kodu. Design jest projektowy: skala, spójność, balancer, storage, notyfikacje.

Nie wiemy, którą rubrykę przypięli do Twojego zaproszenia. Kod trenujemy tak samo. W designie umiesz obie wersje: „co tu pęknie” i „jak bym to rozdzielił”. Jak rekruter nie napisał poziomu, przygotuj się pod wyższy próg kodu, czyli GCB5.
