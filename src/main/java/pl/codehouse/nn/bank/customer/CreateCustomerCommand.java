package pl.codehouse.nn.bank.customer;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import pl.codehouse.nn.bank.Command;
import pl.codehouse.nn.bank.ExecutionResult;
import pl.codehouse.nn.bank.account.AccountService;
import pl.codehouse.nn.bank.account.Currency;
import pl.codehouse.nn.bank.account.CurrencyAccountDto;
import pl.codehouse.nn.bank.account.NewAccountsRequest;
import reactor.core.publisher.Mono;

/**
 * Command for creating a new customer account with associated currency accounts.
 * This class implements the Command pattern to encapsulate the logic for customer creation.
 */
@Component
class CreateCustomerCommand implements Command<CreateCustomerRequest, CustomerDto> {

    private final Logger log = LoggerFactory.getLogger(CreateCustomerCommand.class);
    private final CustomerRepository customerRepository;
    private final AccountService accountService;

    /**
     * Constructs a new CreateCustomerCommand with the necessary dependencies.
     *
     * @param customerRepository Repository for customer data operations.
     * @param accountService Service for managing currency accounts.
     */
    CreateCustomerCommand(CustomerRepository customerRepository, AccountService accountService) {
        this.customerRepository = customerRepository;
        this.accountService = accountService;
    }

    /**
     * Executes the command to create a new customer account.
     * This method performs the following steps:
     * 1. Creates a new customer entity.
     * 2. Saves the customer to the repository.
     * 3. Creates initial currency accounts for the customer.
     * 4. Combines the customer data with the created accounts.
     * 5. Handles any errors that occur during the process.
     *
     * @param request The {@link CreateCustomerRequest} containing the details for creating a new customer account.
     * @return A {@link Mono} that emits an {@link ExecutionResult} containing the created {@link CustomerDto}.
     *         The result will be a success if the customer is created successfully, or a failure with an error message otherwise.
     */
    @Override
    public Mono<ExecutionResult<CustomerDto>> execute(CreateCustomerRequest request) {
        log.info("Creating new customer with data: {}", request);
        var newAccount = Customer.createNew(request.firstName(), request.lastName());

        return customerRepository.save(newAccount)
                .flatMap(entity -> Mono.zip(Mono.just(entity), createInitialDeposit(entity.accountId(), request)))
                .map(accountBalanceTuple -> CustomerDto.from(accountBalanceTuple.getT1(), accountBalanceTuple.getT2()))
                .doOnNext(accountDto -> log.debug("New customer created =>> {}", accountDto))
                .map(ExecutionResult::success)
                .onErrorResume(error -> {
                    log.error("Error during Customer creation Reason: {}", error.getMessage(), error);
                    ExecutionResult<CustomerDto> failureResult = ExecutionResult.failure(
                            new RuntimeException("Error during Customer creation Reason: " + error.getMessage(), error)
                    );
                    return Mono.just(failureResult);
                });
    }

    private Mono<List<CurrencyAccountDto>> createInitialDeposit(long accountId, CreateCustomerRequest request) {
        List<Currency> additionalCurrencies = createListOfAdditionalCurrencies(request);
        Currency startingCurrency = Optional.ofNullable(request.mainAccountCurrency()).orElse(Currency.PLN);
        NewAccountsRequest newAccountsRequest = new NewAccountsRequest(
                accountId,
                request.mainAccountBalance(),
                startingCurrency,
                additionalCurrencies
        );
        log.info("Creating a new currency accounts for customer {}, => {}", accountId, newAccountsRequest);
        return accountService.createNewAccounts(newAccountsRequest);
    }

    private static List<Currency> createListOfAdditionalCurrencies(CreateCustomerRequest request) {
        if(CollectionUtils.isEmpty(request.additionalCurrencies())) {
            return switch (request.mainAccountCurrency()) {
                case PLN:
                    yield List.of(Currency.USD, Currency.EUR);
                case USD:
                    yield List.of(Currency.PLN, Currency.EUR);
                case EUR:
                    yield List.of(Currency.PLN, Currency.USD);
                case null:
                    yield List.of(Currency.PLN, Currency.USD, Currency.EUR);
            };
        }

        return request.additionalCurrencies();
    }
}
