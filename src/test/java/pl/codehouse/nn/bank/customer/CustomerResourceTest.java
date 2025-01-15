package pl.codehouse.nn.bank.customer;

import static io.restassured.module.webtestclient.RestAssuredWebTestClient.given;
import static io.restassured.module.webtestclient.RestAssuredWebTestClient.webTestClient;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import pl.codehouse.nn.bank.ExecutionResult;
import pl.codehouse.nn.bank.account.Currency;
import pl.codehouse.nn.bank.account.CurrencyAccountDto;
import reactor.core.publisher.Mono;


@WebFluxTest(CustomerResource.class)
class CustomerResourceTest {
private final static Logger log = LoggerFactory.getLogger(CustomerResourceTest.class);

    @Autowired
    private WebTestClient webClient;

    @MockitoBean
    private CreateCustomerCommand createCustomerCommand;

    @MockitoBean
    private CurrencyExchangeCommand exchangeCommand;

    @MockitoBean
    private CustomerApi customerApi;

    @BeforeEach
    void setUp() {
        webTestClient(webClient);
    }

    @Test
    @DisplayName("should return 200 with customer details when valid customer ID is provided")
    void should_Return200WithCustomerDetails_When_ValidCustomerIdProvided() {
        // given
        long customerId = 100L;
        List<CurrencyAccountDto> accountBalance = List.of(
                new CurrencyAccountDto(Currency.PLN, new BigDecimal("199.99"), true),
                new CurrencyAccountDto(Currency.USD, BigDecimal.ZERO, false)
        );
        CustomerDto customerDto = new CustomerDto(customerId, "Peter", "Pan", accountBalance);

        given(customerApi.fetchCustomer(customerId)).willReturn(Mono.just(customerDto));

        // when & then
        given()
                .contentType(APPLICATION_JSON_VALUE)

                .when()
                .get("/customers/{customerId}", String.valueOf(customerId))

                .then()
                .log().ifValidationFails()
                .status(HttpStatus.OK)
                .body("accountId", equalTo(100))
                .body("firstName", equalTo("Peter"))
                .body("lastName", equalTo("Pan"))
                .body("accountBalance", hasSize(2))
                .body("accountBalance[0].amount", equalTo(199.99f))
                .body("accountBalance[0].currency", equalTo("PLN"))
                .body("accountBalance[0].isMainAccount", equalTo(true))
                .body("accountBalance[1].amount", equalTo(0))
                .body("accountBalance[1].currency", equalTo("USD"))
                .body("accountBalance[1].isMainAccount", equalTo(false));
    }

    @Test
    @DisplayName("should return 404 Not Found when customer ID does not exist")
    void should_Return404NotFound_When_CustomerIdDoesNotExist() {
        // given
        long customerId = 999;
        given(customerApi.fetchCustomer(customerId)).willReturn(Mono.empty());

        // when & then
        given()
                .contentType(APPLICATION_JSON_VALUE)

                .when()
                .get("/customers/{customerId}", String.valueOf(customerId))

                .then()
                .log().ifValidationFails()
                .status(HttpStatus.NOT_FOUND)
                .body("title", equalTo("Not Found"))
                .body("status", equalTo(404))
                .body("detail", equalTo("Customer with id 999 not found."));
    }

    @Test
    @DisplayName("should return 400 Bad Request when customer ID is invalid")
    void should_Return400BadRequest_When_CustomerIdIsInvalid() {
        // when & then
        given()
                .contentType(APPLICATION_JSON_VALUE)

                .when()
                .get("/customers/invalid")

                .then()
                .log().ifValidationFails()
                .status(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("should return 200 with created customer when valid request passed")
    void should_Return200WithCreatedCustomer_When_ValidRequestPassed() {
        // given
        var request = Map.of(
                "firstName", "Peter",
                "lastName", "Pan",
                "mainAccountBalance", "199.99",
                "mainAccountCurrency", "PLN",
                "additionalCurrencies", List.of("USD")
        );

        List<CurrencyAccountDto> accountBalance = List.of(
                new CurrencyAccountDto(Currency.PLN, new BigDecimal("199.99"), true),
                new CurrencyAccountDto(Currency.USD, BigDecimal.ZERO, false)
        );
        CustomerDto customerDto = new CustomerDto(100L, "Peter", "Pan", accountBalance);

        given(createCustomerCommand.execute(any())).willReturn(Mono.just(ExecutionResult.success(customerDto)));

        // when & then
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(request)

                .when()
                .post("/customers")

                .then()
                .log().ifValidationFails()
                .status(CREATED)
                .body("accountId", equalTo(100))
                .body("firstName", equalTo("Peter"))
                .body("lastName", equalTo("Pan"))
                .body("accountBalance", hasSize(2))
                .body("accountBalance[0].amount", equalTo(199.99f))
                .body("accountBalance[0].currency", equalTo("PLN"))
                .body("accountBalance[0].isMainAccount", equalTo(true))
                .body("accountBalance[1].amount", equalTo(0))
                .body("accountBalance[1].currency", equalTo("USD"))
                .body("accountBalance[1].isMainAccount", equalTo(false))
        ;
    }

    @Test
    @DisplayName("should return 400 Validation Failed when first name and last name are missing")
    void should_Return400ValidationFailed_When_FirstNameAndLastNameAreMissing() {
        // given
        var request = Map.of(
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
                .status(HttpStatus.BAD_REQUEST)
                .body("type", equalTo("about:blank"))
                .body("title", equalTo("Validation Failed"))
                .body("status", equalTo(400))
                .body("errors", aMapWithSize(2))
                .body("errors", hasEntry("lastName", "NotBlank"))
                .body("errors", hasEntry("firstName", "NotBlank"))
        ;
    }

    @Test
    @DisplayName("should return 400 Validation Failed when main account balance is incorrect")
    void should_Return400ValidationFailed_When_MainAccountBalanceIsIncorrect() {
        // given
        var request = Map.of(
                "firstName", "Peter",
                "lastName", "Pan",
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
                .status(HttpStatus.BAD_REQUEST)
                .body("type", equalTo("about:blank"))
                .body("title", equalTo("Validation Failed"))
                .body("status", equalTo(400))
                .body("errors", aMapWithSize(1))
                .body("errors", hasEntry("mainAccountBalance", "NotNull"))
        ;
    }

    @Test
    @DisplayName("should return 400 Validation Failed when additionalCurrencies is missing")
    void should_Return400ValidationFailed_When_AdditionalCurrenciesIsMissing() {
        // given
        var request = Map.of(
                "firstName", "Peter",
                "lastName", "Pan",
                "mainAccountBalance", "199.99",
                "mainAccountCurrency", "PLN"
        );

        // when & then
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(request)

                .when()
                .post("/customers")

                .then()
                .log().ifValidationFails()
                .status(HttpStatus.BAD_REQUEST)
                .body("type", equalTo("about:blank"))
                .body("title", equalTo("Validation Failed"))
                .body("status", equalTo(400))
                .body("errors", aMapWithSize(1))
                .body("errors", hasEntry("additionalCurrencies", "NotNull"))
        ;
    }


    @Test
    @DisplayName("should return 400 Validation Failed when additionalCurrencies is null")
    void should_Return400ValidationFailed_When_AdditionalCurrenciesIsNull() {
        // given
        var request = Map.of(
                "firstName", "Peter",
                "lastName", "Pan",
                "mainAccountBalance", "199.99",
                "mainAccountCurrency", "PLN",
                "additionalCurrencies", "null"
        );

        // when & then
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(request)

                .when()
                .post("/customers")

                .then()
                .log().ifValidationFails()
                .status(HttpStatus.BAD_REQUEST)
                .body("type", equalTo("about:blank"))
                .body("title", equalTo("Bad Request"))
                .body("status", equalTo(400))
                .body("detail", equalTo("Failed to read HTTP message"))
        ;
    }


}
