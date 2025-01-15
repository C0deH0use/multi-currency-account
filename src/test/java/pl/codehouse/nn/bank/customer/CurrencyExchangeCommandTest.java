package pl.codehouse.nn.bank.customer;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.codehouse.nn.bank.ExecutionResult;
import pl.codehouse.nn.bank.account.AccountService;
import pl.codehouse.nn.bank.account.Currency;
import pl.codehouse.nn.bank.account.CurrencyAccountDto;
import pl.codehouse.nn.bank.account.CurrencyAccountNotFoundException;
import pl.codehouse.nn.bank.exchange.rates.ExchangeRateDto;
import pl.codehouse.nn.bank.exchange.rates.ExchangeRatesService;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class CurrencyExchangeCommandTest {
    private static final BigDecimal EUR_RATE = new BigDecimal("4.2737");
    private static final BigDecimal USD_RATE = new BigDecimal("4.1658");
    private final long customerId = 1000L;
    private final Customer mockCustomer = new Customer(customerId, "Bob", "Johnson");

    @Mock
    private AccountService accountService;

    @Mock
    private CustomerRepository repository;

    @Mock
    private ExchangeRatesService exchangeRatesService;

    @InjectMocks
    private CurrencyExchangeCommand sut;

    @BeforeEach
    void setUp() {
        var customerAccounts = List.of(
                new CurrencyAccountDto(Currency.PLN, BigDecimal.TEN, true),
                new CurrencyAccountDto(Currency.EUR, BigDecimal.TEN, false),
                new CurrencyAccountDto(Currency.USD, BigDecimal.TEN, false)
        );
        lenient().when(repository.findById(customerId)).thenReturn(Mono.just(mockCustomer));
        lenient().when(accountService.findAccountsFor(customerId)).thenReturn(Mono.just(customerAccounts));

        lenient().when(accountService.updateAmountFor(anyLong(), any(), any())).thenReturn(Mono.just(customerAccounts.getFirst()));
        lenient().when(exchangeRatesService.fetchCurrentRatesFor(Currency.EUR)).thenReturn(Mono.just(new ExchangeRateDto(Currency.EUR, EUR_RATE)));
        lenient().when(exchangeRatesService.fetchCurrentRatesFor(Currency.USD)).thenReturn(Mono.just(new ExchangeRateDto(Currency.USD, USD_RATE)));
    }

    @MethodSource("toPlnCurrencyExchangeMethodSource")
    @DisplayName("exchange foreign currency to PLN")
    @ParameterizedTest(name = "should add {2} to PLN currency account when exchanging {1} from {0} currency")
    void should_addPlnCurrencyAccount_When_exchangingFromGivenCurrency(Currency fromCurrency, BigDecimal exchangeToAmount, BigDecimal expectedPlnAmountChange) {
        // given
        var request = new CustomerAwareContext<ExchangeRequest>(customerId, new ExchangeRequest(fromCurrency, Currency.PLN, exchangeToAmount));

        // when
        Mono<ExecutionResult<CustomerDto>> resultMono = sut.execute(request);

        // then
        StepVerifier.create(resultMono)
                .assertNext(consumer -> assertThat(consumer.isSuccess()).isTrue())
                .verifyComplete();

        // and
        then(accountService).should(times(1)).updateAmountFor(customerId, fromCurrency, exchangeToAmount.negate());
        then(accountService).should(times(1)).updateAmountFor(customerId, Currency.PLN, expectedPlnAmountChange);
    }

    @Test
    @DisplayName("should not update PLN currency account when not enough amount for exchanging from USD currency")
    void shouldNotUpdateCurrencyAccount_When_NotEnoughAmountForExchangeFromUSDCurrency() {
        // given
        var request = new CustomerAwareContext<ExchangeRequest>(customerId, new ExchangeRequest(Currency.USD, Currency.PLN, BigDecimal.valueOf(3.49)));

        // when
        Mono<CustomerDto> resultMono = sut.execute(request).map(ExecutionResult::handle);

        // then
        StepVerifier.create(resultMono)
                .expectErrorSatisfies(error -> assertThat(error)
                        .isInstanceOf(RuntimeException.class)
                        .hasRootCauseMessage("Not enough amount for exchanging from USD currency")
                );
    }

    @MethodSource("fromPlnCurrencyExchangeMethodSource")
    @DisplayName("exchange PLN to foreign currency")
    @ParameterizedTest(name = "should add {2} to {0} currency account when exchanging {1} PLN to {0}")
    void should_addToForeignCurrencyAccount_When_exchangingFromPlnCurrency(Currency toCurrency, BigDecimal exchangeToAmount, BigDecimal expectedAmountChange) {
        // given
        var request = new CustomerAwareContext<ExchangeRequest>(customerId, new ExchangeRequest(Currency.PLN, toCurrency, exchangeToAmount));

        // when
        Mono<ExecutionResult<CustomerDto>> resultMono = sut.execute(request);

        // then
        StepVerifier.create(resultMono)
                .assertNext(consumer -> assertThat(consumer.isSuccess()).isTrue())
                .verifyComplete();


        // and
        then(accountService).should(times(1)).updateAmountFor(customerId, Currency.PLN, exchangeToAmount.negate());
        then(accountService).should(times(1)).updateAmountFor(customerId, toCurrency, expectedAmountChange);
    }


    @Test
    @DisplayName("should throw not found customer exception when customer id is not known")
    void should_throwNotFoundCustomer_When_CustomerIdIsUnknown() {
        // given
        given(repository.findById(customerId)).willReturn(Mono.empty());
        var request = new CustomerAwareContext<ExchangeRequest>(customerId, new ExchangeRequest(Currency.USD, Currency.PLN, BigDecimal.valueOf(3.49)));

        // when
        Mono<CustomerDto> resultMono = sut.execute(request).map(ExecutionResult::handle);

        // then
        StepVerifier.create(resultMono)
                .expectErrorSatisfies(error -> assertThat(error)
                        .isInstanceOf(CustomerNotFoundException.class)
                        .hasRootCauseMessage("Customer with id %s not found.".formatted(customerId))
                );
    }


    @Test
    @DisplayName("should throw currency account not found exception when customer does not have currency account of given type")
    void should_currencyAccountNotFound_When_CustomerDoesNotHaveCurrencyOfGivenType() {
        // given
        var customerAccounts = List.of(
                new CurrencyAccountDto(Currency.PLN, BigDecimal.TEN, true),
                new CurrencyAccountDto(Currency.EUR, BigDecimal.TEN, false)
        );
        given(accountService.findAccountsFor(customerId)).willReturn(Mono.just(customerAccounts));

        var request = new CustomerAwareContext<ExchangeRequest>(customerId, new ExchangeRequest(Currency.PLN, Currency.USD, BigDecimal.valueOf(3.49)));

        // when
        Mono<CustomerDto> resultMono = sut.execute(request).map(ExecutionResult::handle);

        // then
        StepVerifier.create(resultMono)
                .expectErrorSatisfies(error -> assertThat(error)
                        .isInstanceOf(CurrencyAccountNotFoundException.class)
                        .hasRootCauseMessage("Customer with id %s does not have account with following currency %s".formatted(customerId, Currency.USD))
                );
    }


    public static Stream<Arguments> toPlnCurrencyExchangeMethodSource() {
        return Stream.of(
                Arguments.of(Currency.EUR, BigDecimal.TEN, new BigDecimal("42.74")),
                Arguments.of(Currency.EUR, new BigDecimal("3.99"), new BigDecimal("17.05"))
        );
    }


    public static Stream<Arguments> fromPlnCurrencyExchangeMethodSource() {
        return Stream.of(
                Arguments.of(Currency.EUR, BigDecimal.TEN, new BigDecimal("2.34")),
                Arguments.of(Currency.USD, new BigDecimal("3.99"), new BigDecimal("0.96"))
        );
    }
}