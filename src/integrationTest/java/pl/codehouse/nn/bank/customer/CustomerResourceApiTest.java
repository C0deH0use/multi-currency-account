package pl.codehouse.nn.bank.customer;

import static io.restassured.module.webtestclient.RestAssuredWebTestClient.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToObject;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import io.restassured.module.webtestclient.RestAssuredWebTestClient;
import java.util.List;
import java.util.Map;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import pl.codehouse.nn.bank.TestcontainersConfiguration;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ExtendWith(SpringExtension.class)
@Import(TestcontainersConfiguration.class)
class CustomerResourceApiTest {

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp(
            @Autowired Flyway flyway
    ) {
        RestAssuredWebTestClient.webTestClient(webTestClient);

        flyway.clean();
        flyway.migrate();
    }

    @Test
    @DisplayName("should create new customer and open multi currency bank accounts when requested")
    void should_CreateNewCustomerAndOpenMultiCurrencyBankAccounts_WhenRequested() {
        // given
        var request = Map.of(
                "firstName", "Peter",
                "lastName", "Pan",
                "mainAccountBalance", "199.99",
                "mainAccountCurrency", "PLN",
                "additionalCurrencies", List.of("USD")
        );

        // when & then
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(request)

                .when()
                .post("/customers")

                .then()
                .log().ifValidationFails()
                .status(CREATED)
                .body("accountId", notNullValue(Integer.class))
                .body("firstName", equalTo("Peter"))
                .body("lastName", equalTo("Pan"))
                .body("accountBalance", empty())

// FIXME: Part of the task to create Bank Accounts #6 [Currency Account] - Create new Currency Account for a given customer.
//                .body("accountBalance", hasSize(2))
//                .body("accountBalance[0].amount", equalTo(199.99f))
//                .body("accountBalance[0].currency", equalTo("PLN"))
//                .body("accountBalance[0].isMainAccount", equalTo(true))
//                .body("accountBalance[1].amount", equalTo(0))
//                .body("accountBalance[1].currency", equalTo("USD"))
//                .body("accountBalance[1].isMainAccount", equalTo(false))

        ;
    }

    @Test
    @DisplayName("should fetch existing customer by id")
    void should_FetchExistingCustomer_WhenGivenValidId(@Autowired R2dbcEntityTemplate r2dbcEntityTemplate) {
        // given
        Customer existingCustomer = new Customer(0L, "John", "Doe");
        int existingCustomerId = (int) r2dbcEntityTemplate.insert(existingCustomer).block().accountId();

        given()
                .contentType(APPLICATION_JSON_VALUE)

                .when()
                .get("/customers/{customerId}", String.valueOf(existingCustomerId))

                .then()
                .log().ifValidationFails()
                .status(HttpStatus.OK)
                .body("accountId", is(existingCustomerId))
                .body("firstName", equalTo("John"))
                .body("lastName", equalTo("Doe"))
                .body("accountBalance", empty());
    }

    @Test
    @DisplayName("should return 404 when fetching non-existing customer")
    void should_Return404_WhenFetchingNonExistingCustomer() {
        // given
        given()
                .contentType(APPLICATION_JSON_VALUE)

                .when()
                .get("/customers/{customerId}", "999999")

                .then()
                .log().ifValidationFails()
                .status(HttpStatus.NOT_FOUND)
                .body("title", equalTo("Not Found"))
                .body("status", equalTo(404))
                .body("detail", equalTo("Customer with id 999999 not found."));
    }
}
