package pl.codehouse.nn.bank.account;

import java.math.BigDecimal;
import java.util.List;
import reactor.core.publisher.Mono;

/**
 * Service interface for managing currency accounts in the multi-currency bank account system.
 * This interface defines operations related to creating, retrieving, and managing currency accounts for customers.
 */
public interface AccountService {

    /**
     * Retrieves all currency accounts associated with a specific customer.
     *
     * @param customerId The unique identifier of the customer whose accounts are to be retrieved.
     * @return A {@link Mono} that emits a List of {@link CurrencyAccountDto} representing all currency accounts
     *         owned by the specified customer. If the customer has no accounts, an empty list is emitted.
     */
    Mono<List<CurrencyAccountDto>> findAccountsFor(long customerId);

    /**
     * Creates new currency accounts for a customer based on the provided request.
     * This method handles the creation of multiple currency accounts as specified in the request,
     * including setting up the main account and any additional currency accounts.
     *
     * @param request The {@link NewAccountsRequest} containing details for creating new accounts,
     *                including the customer ID, main account currency and balance, and any additional currencies.
     * @return A {@link Mono} that emits a List of {@link CurrencyAccountDto} representing the newly created accounts.
     *         The list includes all accounts created, with their respective currencies and balances.
     * @throws IllegalArgumentException if the request contains invalid data, such as negative balances or unsupported currencies.
     */
    Mono<List<CurrencyAccountDto>> createNewAccounts(NewAccountsRequest request);

    Mono<CurrencyAccountDto> updateAmountFor(long customerId, Currency currency, BigDecimal amount);
}
