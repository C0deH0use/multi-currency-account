# multi-currency-account
This is a multi-currency bank account Java service

## Task
The goal of this task is to prepare a REST API application that allows for creating an account and exchanging currency in the PLN<->USD pair.

#### Functional requirements:
- The application has a REST API that allows for creating a currency account.
- When creating an account, it is required to provide an initial account balance in PLN.
- The application requires the user to provide their first and last name when creating an account.
- The application generates an account identifier when creating an account, which should be used when calling further API methods.
- The application should provide a REST API for exchanging money in the PLN<->USD pair (i.e., PLN to USD and USD to PLN), and fetch the current exchange rate from the public NBP API (http://api.nbp.pl/).
- The application should provide a REST API to retrieve account data and its current balance in PLN and USD.

#### Non-functional requirements:
- The application must be written in Java.
- The application can be developed using any framework.
- The application should persist data after restart.
- The source code of the application should be made available on a chosen code hosting platform (e.g., Gitlab, Github, Bitbucket).
- The application must be built using a build tool (e.g., Maven, Gradle).
- A README with instructions on how to run the application is required.

## How to run the application

### Prerequisites
- Docker
- Docker Compose

### Steps to run the application

1. Clone the repository:
   ```
   git clone <repository-url>
   cd multi-currency-account
   ```

2. Build and start the application:
   ```
   make run
   ```
   This command will build the Docker image and start both the application and the database.

3. The application will be available at `http://localhost:8080`

### Testing the API

You can use the provided HTTP requests in the `http-tests/customers.http` file to test the API. Here's an example of creating a new customer:

```http
POST http://localhost:8080/customers
Content-Type: application/json

{
  "firstName": "Peter",
  "lastName": "Pan",
  "mainAccountBalance": "199.99",
  "mainAccountCurrency": "PLN",
  "additionalCurrencies": ["USD"]
}
```

You can use tools like cURL, Postman, or IDE plugins (e.g., REST Client for VS Code) to send these requests to the API.

### Additional commands

- To start only the database:
  ```
  make run-db
  ```

- To stop and remove all containers and volumes:
  ```
  make destroy
  ```
