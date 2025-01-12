package pl.codehouse.nn.bank.account;

import java.util.List;
import reactor.core.publisher.Mono;

/**
 * Service interface for managing currency accounts.
 * This interface defines operations related to creating and managing currency accounts for customers.
 */
public interface AccountService {
    /**
     * Creates new currency accounts for a customer based on the provided request.
     *
     * @param request The {@link NewAccountsRequest} containing details for creating new accounts.
     * @return A {@link Mono} that emits a List of {@link CurrencyAccountDto} representing the newly created accounts.
     */
    Mono<List<CurrencyAccountDto>> createNewAccounts(NewAccountsRequest request);
}
