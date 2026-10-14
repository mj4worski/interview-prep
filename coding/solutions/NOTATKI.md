# Co było nie tak

## W1

`Payment.equals` i `hashCode` patrzyły na kwotę. Identyczność to `id`. Samo `equals` nie wystarcza: `HashSet.contains` najpierw liczy hash. Dwie płatności o tym samym id i innej kwocie lądowały w innym kubełku i `contains` mówił „nie ma”.

`toCents` tnie po kropce i składa `long`. Żadnego `double`.

## K1A

`countsTowardBalance` puszczał `FAILED` razem z `SETTLED`. Pending był odrzucany, więc zielone testy pending Cię usypiają. Czerwony jest tylko failed.

## K1C

Konto, które wysyła do siebie, jest w mapie raz: inflow i outflow rosną, licznik transferów rośnie o 1, nie o 2. Klucz sortujesz alfabetycznie. Failed i pending nie tworzą wiersza.

## K2A

Mapa brała klucz `userId`. Spec każe brać parę `(userId, template)`. Użytkownik z dostarczonym WELCOME (wyższy attempt) gubił nieudane ALERT. Klucz: `userId + "\0" + template`.

## K2C

`SKIPPED` nie wchodzi ani do licznika, ani do mianownika. Kanał, który ma same SKIPPED, nie występuje w mapie. Kanał 0 dostarczonych i same FAILED ma rate `0.0` i w mapie jest. Dzielenie przez `double`.

## K3A

Warunek był `status > 500`, więc **500** wypadał. Poprawnie: `>= 500 && <= 599`. Część C woła tę metodę. Czerwone C przy dobrej agregacji często znaczy, że A nadal jest zepsute.

## K3C

`routeKey` to `service + " " + route`, nie sama ścieżka. Dwa serwisy z tym samym route to dwa wiersze. Próg jest domknięty (`>=`). Sort: rate malejąco, remis po kluczu rosnąco.
