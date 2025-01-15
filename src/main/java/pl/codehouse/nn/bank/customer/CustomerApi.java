package pl.codehouse.nn.bank.customer;

import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

/**
 * Defines the API for customer-related operations in the multi-currency bank account system.
 * This interface provides methods for managing customer accounts, including fetching customer information
 * and creating new customer accounts.
 */
public interface CustomerApi {

    /**
     * Fetches customer information based on the provided customer ID.
     *
     * @param customerId The unique identifier of the customer to fetch.
     * @return A {@link Mono} that emits a {@link CustomerDto} containing the customer's information.
     *      If the customer is not found, the Mono will complete empty.
     */
    Mono<CustomerDto> fetchCustomer(long customerId);

    /**
     * Creates a new customer account based on the provided request.
     * This method handles the creation of a new customer record along with associated currency accounts.
     *
     * @param request The {@link CreateCustomerRequest} containing the details for creating a new customer account,
     *                including personal information and initial account settings.
     * @return A {@link Mono} that emits a {@link CustomerDto} representing the newly created customer account,
     *      including all initialized currency accounts.
     * @throws IllegalArgumentException if the request contains invalid data.
     */
    Mono<CustomerDto> createAccount(@Valid CreateCustomerRequest request);

    Mono<CustomerDto> exchange(@Valid ExchangeRequest request);
}
