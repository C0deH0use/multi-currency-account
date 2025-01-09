# multi-currency-account
This is a multi-currency bank account Java service

## Task
Celem zadania jest przygotowanie aplikacji serwującej API REST, która pozwoli na założenie konta oraz wymianę waluty w parze PLN<->USD.

#### Założenia funkcjonalne:
- Aplikacja posiada REST API pozwalające na założenie konta walutowego.
- Przy zakładaniu konta wymagane jest podanie początkowego salda konta w PLN.
- Aplikacja przy zakładaniu konta wymaga od użytkownika podania imienia i nazwiska.
- Aplikacja przy zakładaniu konta generuje identyfikator konta, który powinien być używany przy wywoływaniu dalszych metod API.
- Aplikacja powinna udostępnić REST API do wymiany pieniędzy w parze PLN<->USD (czyli PLN na USD oraz USD na PLN), a aktualny kurs wymiany pobrać z publicznego API NBP (http://api.nbp.pl/).
- Aplikacja powinna udostępnić REST API do pobrania danych o koncie i jego aktualnego stanu w PLN i USD.

#### Założenia niefunkcjonalne:
- Aplikacja musi zostać wykonana w Javie.
- Aplikacja może być wykonana w dowolnym frameworku.
- Aplikacja powinna zachowywać dane po restarcie.
- Kod źródłowy aplikacji powinien zostać udostępniony na wybranym portalu do hostowania kodu (np. Gitlab, Github, Bitbucket).
- Aplikacja musi być budowana przy pomocy narzędzia do budowania aplikacji (np. Maven, Gradle).
- Wymagane jest README z instrukcją pozwalającą uruchomić aplikację.