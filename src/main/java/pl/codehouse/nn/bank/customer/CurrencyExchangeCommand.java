package pl.codehouse.nn.bank.customer;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pl.codehouse.nn.bank.Command;
import pl.codehouse.nn.bank.ExecutionResult;
import pl.codehouse.nn.bank.account.AccountService;
import pl.codehouse.nn.bank.account.Currency;
import pl.codehouse.nn.bank.account.CurrencyAccountDto;
import pl.codehouse.nn.bank.account.CurrencyAccountNotFoundException;
import pl.codehouse.nn.bank.exchange.rates.ExchangeRateDto;
import pl.codehouse.nn.bank.exchange.rates.ExchangeRatesService;
import reactor.core.publisher.Mono;

@Component
record CurrencyExchangeCommand(
        CustomerRepository repository,
        AccountService accountService,
        ExchangeRatesService exchangeRatesService
)  implements Command<CustomerAwareContext<ExchangeRequest>, CustomerDto> {
    private static final Logger log = LoggerFactory.getLogger(CurrencyExchangeCommand.class);

    @Override
    public Mono<ExecutionResult<CustomerDto>> execute(CustomerAwareContext<ExchangeRequest> context) {
        long customerId = context.customerId();
        ExchangeRequest exchangeRequest = context.request();
        log.info("Customer {} requested to exchange {} {} to {}.",
                 customerId, exchangeRequest.fromCurrency(), exchangeRequest.amount(), exchangeRequest.toCurrency());

        return repository.findById(customerId)
                .switchIfEmpty(Mono.error(new CustomerNotFoundException(customerId)))
                .zipWith(accountService.findAccountsFor(customerId))
                .map(customerAccountsTuple -> customerAccountsTuple.getT2().stream()
                        .filter(account -> exchangeRequest.fromCurrency() == account.currency())
                        .findFirst()
                        .orElseThrow(() -> new CurrencyAccountNotFoundException(customerId, exchangeRequest.fromCurrency()))
                )
                .filter(account -> validateNeededBalance(account, exchangeRequest.amount()))
                .flatMap(account -> switch (exchangeRequest.toCurrency()) {
                    case PLN: yield exchangeRatesService.fetchCurrentRatesFor(exchangeRequest.fromCurrency());
                    case USD:
                    case EUR: yield exchangeRatesService.fetchCurrentRatesFor(exchangeRequest.toCurrency());
                })
                .map(exchangeRates -> calculateTheExchangeAmounts(exchangeRequest, exchangeRates))
                .flatMap(exchangeAmountValues -> Mono.when(
                        accountService.updateAmountFor(customerId, exchangeRequest.toCurrency(), exchangeAmountValues.to.amount),
                        accountService.updateAmountFor(customerId, exchangeRequest.fromCurrency(), exchangeAmountValues.from.amount))
                        .doOnSuccess((v) -> log.info("Updated all Currency Accounts..."))
                        .thenReturn(true)
                )
                .flatMap(x -> repository.findById(customerId).zipWith(accountService.findAccountsFor(customerId)))
                .map(accountBalanceTuple -> CustomerDto.from(accountBalanceTuple.getT1(), accountBalanceTuple.getT2()))
                .map(ExecutionResult::success)
                .onErrorResume(error -> {
                    log.error("Error during Exchange Currency command: {}", error.getMessage(), error);
                    ExecutionResult<CustomerDto> failureResult = ExecutionResult.failure(
                            new RuntimeException("Error during Exchange Currency command Reason: " + error.getMessage(), error)
                    );
                    return Mono.just(failureResult);
                });
    }

    private ExchangeAmountValues calculateTheExchangeAmounts(ExchangeRequest request, ExchangeRateDto exchangeRequest) {
        ExchangeAmount from = new ExchangeAmount(request.fromCurrency(), request.amount().negate());
        ExchangeAmount to;
        BigDecimal exchangeToCurrencyAmount = switch (request.fromCurrency()) {
            case PLN: yield request.amount().divide(exchangeRequest.rate(), 2, RoundingMode.HALF_DOWN);
            case USD:
            case EUR: yield request.amount().multiply(exchangeRequest.rate())
                    .setScale(2, RoundingMode.HALF_UP);
        };
        to = new ExchangeAmount(request.toCurrency(), exchangeToCurrencyAmount);

        log.info("Exchange Amount values for {} =>> {}", from, to);
        return new ExchangeAmountValues(from, to);
    }

    private boolean validateNeededBalance(CurrencyAccountDto account, BigDecimal amount) {
        return account.amount().compareTo(amount) >= 0;
    }

    private record ExchangeAmountValues(
            ExchangeAmount from,
            ExchangeAmount to
    ) {
    }

    private record ExchangeAmount(Currency currency, BigDecimal amount) {
    }
}
