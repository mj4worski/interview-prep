# Jak trzymać to lokalnie i pracować razem

Ta sesja Cloud Agent **nie ma repozytorium**. Pliki żyją na VM agenta i **mogą zniknąć** po restarcie środowiska. Dlatego kopia u Ciebie na dysku jest obowiązkowa.

## 1. Pobierz paczkę (najszybsze)

W katalogu nadrzędnym jest `interview-prep.zip` (albo `interview-prep/` obok).

**Cursor Desktop / Agent na komputerze**

1. Rozpakuj zip do np. `~/interview-prep`
2. Cursor: **File → Open Folder** → ten katalog
3. Nowy czat w tym folderze (lokalny Agent, nie Cloud bez repo). Wtedy obaj widzimy te same pliki, Twoje rozwiązania zostają na dysku.

**Tylko telefon / przeglądarka**

1. Wejdź w tę sesję: https://cursor.com/agents/bc-01a0e269-a55e-7fcf-b570-071a80bdd919
2. Pobierz `interview-prep.zip` z plików workspace (Downloads / Files)
3. Wrzuć na komputer albo do prywatnego repo

## 2. Lepsze na 14 dni: prywatne repo GitHub

Bez tego każda nowa sesja Cloud Agent startuje z pustym dyskiem.

```bash
cd ~/interview-prep
git init
git add .
git commit -m "Interview prep kit"
# stwórz puste prywatne repo na GitHubie, potem:
git remote add origin git@github.com:<TY>/interview-prep.git
git branch -M main
git push -u origin main
```

Potem nowe Cloud Agents odpalaj **z tego repo**. Wtedy kod P01–P07 i notatki przeżywają.

Nie wrzucaj tu CV z danymi osobowymi ani sekretów z pracy.

## 3. Coding lokalnie (Java 17+)

```bash
cd interview-prep/coding
chmod +x run.sh
./run.sh P01
```

Edytujesz `src/interview/p01/LongestSubstring.java`, odpalasz testy, wrzucasz mi kod w czacie.

## 4. Co jest w zestawie

| Plik | Treść |
|------|--------|
| `README.md` | plan 14 dni |
| `01-system-design.md` | rama + 5 systemów |
| `02-coding-patterns.md` | 12 wzorców |
| `03-java-spring.md` | grill Java/Spring |
| `04-databases.md` | SQL, indeksy, isolation |
| `05-behavioral.md` | STAR + exit story |
| `06-cheat-sheet.md` | 20 min przed |
| `07-kafka-aws-k8s.md` | stack z ogłoszenia |
| `08-java17-21.md` | records, VT |
| `coding/` | P01–P07 + testy |
