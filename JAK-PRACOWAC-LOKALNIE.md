# Gdzie to odpalać

Tak, otwórz **to repo** w IDE (Cursor albo IntelliJ).

Na rozmowie IDE nie będzie. Karat Studio jest w przeglądarce. Tu ćwiczysz, tam tylko piszesz.

```bash
cd coding
chmod +x run.sh
./run.sh W1
```

Potrzebny JDK 11 albo nowszy. AdoptOpenJDK 8 wywali się na `javac: invalid flag: --release`. Na Macu:

```bash
brew install --cask temurin@21
/usr/libexec/java_home -v 21
```

`./run.sh` sam wybierze Temurin 21, nawet gdy `java` w PATH nadal jest 1.8. Żeby terminal też widział 21, w `~/.zshrc` zostaw tylko:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
```

i usuń starą ścieżkę AdoptOpenJDK 8. Potem `./run.sh --jdk` powinno pokazać 21.

Start od `README.md`, potem `karat/01-platforma.md`. Nie od P01.

Źródło jest na GitHubie: `mj4worski/interview-prep`. Branch z tym screenem: `cursor/karat-interview-rebuild-57c3`.

## Co jest w zestawie

| Plik | Kiedy |
|------|--------|
| `README.md`, `00-PLAN.md` | Dziś. Kolejność pod Karat |
| `karat/` | Dziś i jutro. Platforma, design 20 min, protokół kodu, ściąga |
| `coding/` W1, K1–K3 | Dziś. Bug, parse, agregacja |
| `09-materialy.md` | Po screenie. Mapa 14 dni i linki |
| `01`–`08` | Po screenie. Design, Java, Spring, SQL, Kafka, AWS, K8s |
| `coding/` P01–P07 | Po screenie, jeśli wpadnie runda algorytmiczna |
