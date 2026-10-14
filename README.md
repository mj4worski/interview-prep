# Karat — Backend Engineer (GCB4 / GCB5)

To jest screen na **karat.com**, nie rozmowa system-design na 45 minut i nie LeetCode. Stary plan 14 dni (Kafka, AWS, Kubernetes, P01–P07) zostaje w repo na etap **po** tym screenie. Przed Karatem go nie rób.

Zakładam Goldman Sachs Polska: w przeglądarce była zakładka `KARAT_POLAND_GS`. Rubryka jest ważniejsza niż logo. Jak to inna firma, i tak trenujemy to, co jest na screenach.

## Czy odpalać to repo w IDE?

Tak. Otwórz **ten katalog** w Cursorze albo IntelliJ i pracuj w terminalu w `coding/`.

Na prawdziwej rozmowie swojego IDE nie będzie. Kod piszesz w przeglądarce, w **Karat Studio**. Dlatego ostatni trening (K3) zrób bez podpowiedzi z Copilota.

Nie zaczynaj od `P01`. Start:

```bash
cd coding
./run.sh W1
```

## Co jest na screenach

Godzina z interviewerem Karat (nie z Goldmanem). Z tego **50 minut** jest oceniane. Intro jest poza tym czasem.

| | System design | Kod: Develop and Update Backend Code |
|---|---|---|
| Czas | 20 min | 30 min |
| GCB4 | 50%, max 300. Słabości, SPOF, fault tolerance, skala, spójność, bezpieczeństwo, dane, integracje | 50%, max 300, **próg 60** |
| GCB5 | 40%, max 240. Skala i duży ruch, modele spójności, load balancing, wąskie gardła, storage, notyfikacje | 60%, max 360, **próg 72** |

Umiejętności kodu są **te same** na obu poziomach:

- struktury danych i obiektówka
- metody do parsowania i agregacji, plus debug
- bugi w gotowym kodzie

Suma punktów to 600. Próg 60 albo 72 to 20% sekcji kodu: poniżej tego screen odpada, ale sam próg nie wystarcza, żeby przejść. Na dole ekranu są progi rekomendacji (further review / next round / tech touch). Ze screena nie da się uczciwie odczytać procentów, więc nie celuj w „60 punktów”. Celuj w zielone testy na dwóch pełnych częściach i domkniętą trzecią.

Spring, Hibernate i Kafka **nie są** na tej rubryce. Kod to zwykła Java: klasy, `List`, `Map`, parsowanie linii, bug w metodzie, która już jest. Framework może paść później, na rozmowie z zespołem. Tu wystarczy, że w designie powiesz „zapis i wysyłka nie mogą być w jednym try/catch po commicie”.

## Kolejność, jak masz mało czasu

Jedna sesja to około 2 godziny. Mów na głos. Po angielsku, jeśli rekruter nie powiedział, że rozmowa jest po polsku. Karat zwykle prowadzi po angielsku.

1. **Dziś — kod.** Przeczytaj `karat/01-platforma.md` i `karat/03-kod-30.md` (razem ~20 min). Potem `./run.sh W1`, potem K1 na zegarze 30 min. Jak zostanie siła: K2.
2. **Jutro — design i mock.** `karat/02-design-20.md`: 4 scenariusze na głos, 20 minut. Potem K3 na 30 minut, bez podpowiedzi i bez `coding/solutions`.
3. **Rano w dzień rozmowy.** Tylko `karat/05-sciaga.md` i ewentualnie samo W1, żeby ręce pamiętały składnię. Nic nowego.

Jak masz tylko jeden wieczór: W1 + K1 + dwa scenariusze (SPOF i notyfikacje) + ściąga. K2 rano, jeśli zdążysz. K3 tylko gdy K1 jest zielone.

## Mapa plików

| Plik | Po co |
|---|---|
| `karat/01-platforma.md` | Jak wygląda rozmowa, co wolno, redo |
| `karat/02-design-20.md` | 20 minut scenariuszy, nie whiteboard |
| `karat/03-kod-30.md` | Protokół kodowania i zdania po angielsku |
| `karat/04-java-minimum.md` | Java, którą trzeba napisać bez szukania |
| `karat/05-sciaga.md` | Jedyna kartka w dzień rozmowy |
| `coding/` | W1, K1, K2, K3. Rozwiązania w `coding/solutions/`, dopiero po zegarze |
| `01`–`08`, `P01`–`P07` | Etap po Karacie. Nie dziś |

Jak utkniesz, wklej kod i output `./run.sh`. Recenzja jest jak na rozmowie: co interviewer by przerwał, co jest bugiem, czy zdążysz na następną część.
