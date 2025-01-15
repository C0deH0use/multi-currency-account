package pl.codehouse.nn.bank.customer;

/**
 * A record that represents a context aware of a customer, containing both the customer ID and a generic request.
 * This class is useful for operations that need to be performed in the context of a specific customer.
 *
 * @param <T> The type of the request associated with this context.
 */
public record CustomerAwareContext<T>(
        long customerId,
        T request
) {
}
