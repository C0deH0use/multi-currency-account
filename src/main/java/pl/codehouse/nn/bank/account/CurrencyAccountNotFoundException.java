package pl.codehouse.nn.bank.account;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Exception thrown when a currency account is not found for a given customer and currency.
 * This exception is mapped to an HTTP 404 Not Found response.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class CurrencyAccountNotFoundException extends ResponseStatusException {

    /**
     * Constructs a new CurrencyAccountNotFoundException with the specified customer ID and currency.
     *
     * @param customerId The ID of the customer for whom the account was not found.
     * @param currency The currency of the account that was not found.
     */
    public CurrencyAccountNotFoundException(long customerId, Currency currency) {
        super(HttpStatus.NOT_FOUND, "Customer with id %s does not have account with following currency %s".formatted(customerId, currency));
    }
}
