package pl.codehouse.nn.bank.customer;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import pl.codehouse.nn.bank.account.Currency;

/**
 * Represents a request for currency exchange.
 * This record encapsulates the details needed to perform a currency exchange operation.
 */
public record ExchangeRequest(
        @JsonProperty("fromCurrency") Currency fromCurrency,
        @JsonProperty("toCurrency") Currency toCurrency,
        @JsonProperty("amount") BigDecimal amount) {
}
