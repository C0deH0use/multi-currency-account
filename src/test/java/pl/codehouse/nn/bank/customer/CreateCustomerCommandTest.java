package pl.codehouse.nn.bank.customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.only;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.codehouse.nn.bank.ExecutionResult;
import pl.codehouse.nn.bank.account.AccountService;
import pl.codehouse.nn.bank.account.Currency;
import pl.codehouse.nn.bank.account.CurrencyAccountDto;
import pl.codehouse.nn.bank.account.NewAccountsRequest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class CreateCustomerCommandTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AccountService accountService;

    @Captor
    private ArgumentCaptor<Customer> accountArgumentCaptor;

    @Captor
    private ArgumentCaptor<NewAccountsRequest> newAccountsRequestArgumentCaptor;

    @InjectMocks
    private CreateCustomerCommand createCustomerCommand;

    @MethodSource("createCustomerTestCases")
    @DisplayName("Parameterized test for creating new customer accounts")
    @ParameterizedTest(name = "should create new customer with main currency account in given {1} currency and {0} balance when requested main currency {2} is provided")
    void should_createNewCustomerWithVariousBalancesAndCurrencies(BigDecimal mainAccountBalance, Currency mainAccountCurrency) {
        // given
        var additionalCurrencyAccounts = List.of(Currency.EUR);
        CreateCustomerRequest request = new CreateCustomerRequest(
                "John", "Doe", mainAccountBalance, mainAccountCurrency, additionalCurrencyAccounts
        );

        var defaultCurrencyBalanceMap = new HashMap<Currency, BigDecimal>() {{
            put(Currency.PLN, BigDecimal.ZERO);
            put(Currency.EUR, BigDecimal.ZERO);
            put(Currency.USD, BigDecimal.ZERO);
        }};
        defaultCurrencyBalanceMap.computeIfPresent(mainAccountCurrency, (currency, balance) -> mainAccountBalance);

        var expectedRequestedCurrencyAccounts = defaultCurrencyBalanceMap.entrySet()
                .stream()
                .map(entry -> new CurrencyAccountDto(entry.getKey(), entry.getValue(), !BigDecimal.ZERO.equals(entry.getValue())))
                .toList();

        given(customerRepository.save(any())).willReturn(Mono.just(new Customer(1000L, "John", "Doe")));
        given(accountService.createNewAccounts(any())).willReturn(Mono.just(expectedRequestedCurrencyAccounts));

        // when
        Mono<ExecutionResult<CustomerDto>> resultMono = createCustomerCommand.execute(request);

        // then
        StepVerifier.create(resultMono)
                .assertNext(result -> {
                    assertThat(result.isSuccess()).isTrue();
                    CustomerDto customerDto = result.handle();
                    assertThat(customerDto)
                            .hasFieldOrPropertyWithValue("accountId", 1000L)
                            .hasFieldOrPropertyWithValue("firstName", "John")
                            .hasFieldOrPropertyWithValue("lastName", "Doe");
                    assertThat(customerDto.accountBalance())
                            .hasSize(expectedRequestedCurrencyAccounts.size())
                            .containsExactlyInAnyOrderElementsOf(expectedRequestedCurrencyAccounts);
                })
                .verifyComplete();

        // and
        then(customerRepository).should(only()).save(accountArgumentCaptor.capture());
        assertThat(accountArgumentCaptor.getValue())
                .hasFieldOrPropertyWithValue("accountId", 0L)
                .hasFieldOrPropertyWithValue("firstName", "John")
                .hasFieldOrPropertyWithValue("lastName", "Doe");

        // and
        then(accountService).should(only()).createNewAccounts(newAccountsRequestArgumentCaptor.capture());
        assertThat(newAccountsRequestArgumentCaptor.getValue())
                .hasFieldOrPropertyWithValue("customerId", 1000L)
                .hasFieldOrPropertyWithValue("mainAccountBalance", mainAccountBalance)
                .hasFieldOrPropertyWithValue("mainAccountCurrency", mainAccountCurrency)
                .hasFieldOrPropertyWithValue("additionalCurrencies", additionalCurrencyAccounts);
    }

    @Test
    @DisplayName("should create new customer with default main currency account when no starter currency passed")
    void should_createNewCustomerWithDefaultMainCurrencyAccount_When_noStarterCurrencyPassed() {
        // given
        Currency starterCurrency = null;
        var expectedMainCurrencyAccount = Currency.PLN;
        List<Currency> expectedRequestedAdditionalCurrencies = List.of(Currency.EUR);

        CreateCustomerRequest request = new CreateCustomerRequest(
                "John", "Doe", BigDecimal.valueOf(1000), starterCurrency,  List.of(Currency.EUR)
        );

        List<CurrencyAccountDto> expectedCurrencyAccounts = List.of(
                new CurrencyAccountDto(Currency.PLN, BigDecimal.valueOf(1000), true),
                new CurrencyAccountDto(Currency.EUR, BigDecimal.ZERO, false)
        );
        given(customerRepository.save(any())).willAnswer(invocation -> {
            Customer customerEntity = invocation.getArgument(0);
            var account = new Customer(1000, customerEntity.firstName(), customerEntity.lastName());
            return Mono.just(account);
        });
        given(accountService.createNewAccounts(any())).willReturn(Mono.just(expectedCurrencyAccounts));

        // when
        Mono<ExecutionResult<CustomerDto>> resultMono = createCustomerCommand.execute(request);

        // then
        StepVerifier.create(resultMono)
                .assertNext(result -> {
                    assertThat(result.isSuccess()).isTrue();
                    CustomerDto customerDto = result.handle();
                    assertThat(customerDto)
                            .hasFieldOrPropertyWithValue("accountId", 1000L)
                            .hasFieldOrPropertyWithValue("firstName", "John")
                            .hasFieldOrPropertyWithValue("lastName", "Doe");
                    assertThat(customerDto.accountBalance())
                            .hasSize(2)
                            .satisfiesExactlyInAnyOrder(
                                    account -> assertThat(account)
                                            .hasFieldOrPropertyWithValue("currency", expectedMainCurrencyAccount)
                                            .hasFieldOrPropertyWithValue("amount", BigDecimal.valueOf(1000)),
                                    account -> assertThat(account)
                                            .hasFieldOrPropertyWithValue("currency", Currency.EUR)
                                            .hasFieldOrPropertyWithValue("amount", BigDecimal.ZERO)
                            );
                })
                .verifyComplete();

        // and
        then(customerRepository).should(only()).save(accountArgumentCaptor.capture());

        assertThat(accountArgumentCaptor.getValue())
                .hasFieldOrPropertyWithValue("accountId", 0L)
                .hasFieldOrPropertyWithValue("firstName", "John")
                .hasFieldOrPropertyWithValue("lastName", "Doe");

        // and
        then(accountService).should(only()).createNewAccounts(newAccountsRequestArgumentCaptor.capture());

        assertThat(newAccountsRequestArgumentCaptor.getValue())
                .hasFieldOrPropertyWithValue("customerId", 1000L)
                .hasFieldOrPropertyWithValue("mainAccountBalance", BigDecimal.valueOf(1000))
                .hasFieldOrPropertyWithValue("mainAccountCurrency", expectedMainCurrencyAccount)
                .hasFieldOrPropertyWithValue("additionalCurrencies", expectedRequestedAdditionalCurrencies);
    }

    @Test
    @DisplayName("should create new customer with default additional accounts when additional currencies passed")
    void should_createNewCustomerWithDefaultAdditionalCurrencies_When_noAdditionalCurrenciesPassed() {
        // given
        List<Currency> expectedRequestedAdditionalCurrencies = List.of(Currency.USD, Currency.EUR);

        CreateCustomerRequest request = new CreateCustomerRequest(
                "John", "Doe", BigDecimal.valueOf(1000), Currency.PLN, null
        );

        List<CurrencyAccountDto> expectedCurrencyAccounts = List.of(
                new CurrencyAccountDto(Currency.PLN, BigDecimal.valueOf(1000), true),
                new CurrencyAccountDto(Currency.EUR, BigDecimal.ZERO, false),
                new CurrencyAccountDto(Currency.USD, BigDecimal.ZERO, false)
        );
        given(customerRepository.save(any())).willAnswer(invocation -> {
            Customer customerEntity = invocation.getArgument(0);
            var account = new Customer(1000, customerEntity.firstName(), customerEntity.lastName());
            return Mono.just(account);
        });
        given(accountService.createNewAccounts(any())).willReturn(Mono.just(expectedCurrencyAccounts));

        // when
        Mono<ExecutionResult<CustomerDto>> resultMono = createCustomerCommand.execute(request);

        // then
        StepVerifier.create(resultMono)
                .assertNext(result -> {
                    assertThat(result.isSuccess()).isTrue();
                    CustomerDto customerDto = result.handle();
                    assertThat(customerDto)
                            .hasFieldOrPropertyWithValue("accountId", 1000L)
                            .hasFieldOrPropertyWithValue("firstName", "John")
                            .hasFieldOrPropertyWithValue("lastName", "Doe");
                    assertThat(customerDto.accountBalance())
                            .hasSize(3)
                            .satisfiesExactlyInAnyOrder(
                                    account -> assertThat(account)
                                            .hasFieldOrPropertyWithValue("currency", Currency.PLN)
                                            .hasFieldOrPropertyWithValue("amount", BigDecimal.valueOf(1000)),
                                    account -> assertThat(account)
                                            .hasFieldOrPropertyWithValue("currency", Currency.EUR)
                                            .hasFieldOrPropertyWithValue("amount", BigDecimal.ZERO),
                                    account -> assertThat(account)
                                            .hasFieldOrPropertyWithValue("currency", Currency.USD)
                                            .hasFieldOrPropertyWithValue("amount", BigDecimal.ZERO)
                            );
                })
                .verifyComplete();

        // and
        then(customerRepository).should(only()).save(accountArgumentCaptor.capture());

        assertThat(accountArgumentCaptor.getValue())
                .hasFieldOrPropertyWithValue("accountId", 0L)
                .hasFieldOrPropertyWithValue("firstName", "John")
                .hasFieldOrPropertyWithValue("lastName", "Doe");

        // and
        then(accountService).should(only()).createNewAccounts(newAccountsRequestArgumentCaptor.capture());

        assertThat(newAccountsRequestArgumentCaptor.getValue())
                .hasFieldOrPropertyWithValue("customerId", 1000L)
                .hasFieldOrPropertyWithValue("mainAccountBalance", BigDecimal.valueOf(1000))
                .hasFieldOrPropertyWithValue("mainAccountCurrency", Currency.PLN)
                .hasFieldOrPropertyWithValue("additionalCurrencies", expectedRequestedAdditionalCurrencies);
    }

    @Test
    @DisplayName("should create new account when specific additional currencies are provided")
    void should_createNewAccount_When_AddAdditionalCurrencies() {
        // given
        CreateCustomerRequest request = new CreateCustomerRequest(
                "Bob", "Johnson", BigDecimal.valueOf(2000), Currency.PLN, List.of(Currency.USD)
        );

        List<CurrencyAccountDto> expectedCurrencyAccounts = List.of(
                new CurrencyAccountDto(Currency.PLN, BigDecimal.valueOf(2000), true),
                new CurrencyAccountDto(Currency.USD, BigDecimal.ZERO, false)
        );
        given(customerRepository.save(any())).willReturn(Mono.just(new Customer(4000, "Bob", "Johnson")));
        given(accountService.createNewAccounts(any())).willReturn(Mono.just(expectedCurrencyAccounts));

        // when
        Mono<ExecutionResult<CustomerDto>> resultMono = createCustomerCommand.execute(request);

        // then
        StepVerifier.create(resultMono)
                .assertNext(result -> {
                    assertThat(result.isSuccess()).isTrue();
                    CustomerDto customerDto = result.handle();
                    assertThat(customerDto)
                            .hasFieldOrPropertyWithValue("accountId", 4000L)
                            .hasFieldOrPropertyWithValue("firstName", "Bob")
                            .hasFieldOrPropertyWithValue("lastName", "Johnson");
                    assertThat(customerDto.accountBalance())
                            .hasSize(2)
                            .satisfiesExactlyInAnyOrder(
                                    account -> assertThat(account)
                                            .hasFieldOrPropertyWithValue("currency", Currency.PLN)
                                            .hasFieldOrPropertyWithValue("amount", BigDecimal.valueOf(2000)),
                                    account -> assertThat(account)
                                            .hasFieldOrPropertyWithValue("currency", Currency.USD)
                                            .hasFieldOrPropertyWithValue("amount", BigDecimal.ZERO)
                            );
                })
                .verifyComplete();

        // and
        then(customerRepository).should(only()).save(accountArgumentCaptor.capture());
        assertThat(accountArgumentCaptor.getValue())
                .hasFieldOrPropertyWithValue("accountId", 0L)
                .hasFieldOrPropertyWithValue("firstName", "Bob")
                .hasFieldOrPropertyWithValue("lastName", "Johnson");

        // and
        then(accountService).should(only()).createNewAccounts(newAccountsRequestArgumentCaptor.capture());
        assertThat(newAccountsRequestArgumentCaptor.getValue())
                .hasFieldOrPropertyWithValue("customerId", 4000L)
                .hasFieldOrPropertyWithValue("mainAccountBalance", BigDecimal.valueOf(2000))
                .hasFieldOrPropertyWithValue("mainAccountCurrency", Currency.PLN)
                .hasFieldOrPropertyWithValue("additionalCurrencies", List.of(Currency.USD));
    }

    @Test
    @DisplayName("should handle error when customer repository fails during account creation with PLN balance and no additional currencies")
    void should_handleError_When_CustomerRepositoryFailsDuringAccountCreationWithPlnBalanceAndNoAdditionalCurrencies() {
        // given
        CreateCustomerRequest request = new CreateCustomerRequest(
                "Error", "User", BigDecimal.valueOf(100), Currency.PLN, null
        );
        given(customerRepository.save(any())).willReturn(Mono.error(new RuntimeException("Database error")));

        // when
        Mono<ExecutionResult<CustomerDto>> resultMono = createCustomerCommand.execute(request);

        // then
        StepVerifier.create(resultMono)
                .assertNext(result -> {
                    assertThat(result.isSuccess()).isFalse();
                    assertThat(result.exception())
                            .hasRootCauseInstanceOf(RuntimeException.class)
                            .hasRootCauseMessage("Database error");
                })
                .verifyComplete();

        // and
        then(customerRepository).should(only()).save(accountArgumentCaptor.capture());
        assertThat(accountArgumentCaptor.getValue())
                .hasFieldOrPropertyWithValue("accountId", 0L)
                .hasFieldOrPropertyWithValue("firstName", "Error")
                .hasFieldOrPropertyWithValue("lastName", "User");
    }

    private static Stream<Arguments> createCustomerTestCases() {
        return Stream.of(
                Arguments.of(BigDecimal.valueOf(1000), Currency.PLN),
                Arguments.of(BigDecimal.valueOf(500), Currency.USD)
        );
    }

}
