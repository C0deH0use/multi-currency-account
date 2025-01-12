package pl.codehouse.nn.bank.customer;

import reactor.core.publisher.Mono;

/**
 * Defines the API for customer-related operations.
 * This interface provides methods for managing customer accounts.
 */
public interface CustomerApi {

    /**
     * Creates a new customer account based on the provided request.
     *
     * @param request The {@link CreateCustomerRequest} containing the details for creating a new customer account.
     * @return A {@link Mono} that emits a {@link CustomerDto} representing the newly created customer account.
     */
    Mono<CustomerDto> createAccount(CreateCustomerRequest request);
}
