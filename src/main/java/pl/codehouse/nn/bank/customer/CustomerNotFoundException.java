package pl.codehouse.nn.bank.customer;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Exception thrown when a customer is not found in the system.
 * This exception is used to indicate that a requested customer does not exist,
 * typically when trying to fetch or operate on a customer using an invalid or non-existent ID.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class CustomerNotFoundException extends ResponseStatusException {

    /**
     * Constructs a new CustomerNotFoundException with the specified customer ID.
     *
     * @param customerId The id of the customer that was not found.
     */
    public CustomerNotFoundException(long customerId) {
        super(HttpStatus.NOT_FOUND, "Customer with id %s not found.".formatted(customerId));
    }
}
