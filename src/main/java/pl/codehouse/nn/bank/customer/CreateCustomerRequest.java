package pl.codehouse.nn.bank.customer;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import pl.codehouse.nn.bank.account.Currency;

/**
 * Represents a request to create a new customer account.
 * This record encapsulates all the necessary information to create a new customer
 * with their initial account details.
 */
public record CreateCustomerRequest(
        @JsonProperty("firstName") @NotBlank String firstName,
        @JsonProperty("lastName") @NotBlank String lastName,
        @JsonProperty("mainAccountBalance") @NotNull @DecimalMin("0.00") BigDecimal mainAccountBalance,
        @JsonProperty("mainAccountCurrency") @Nullable Currency mainAccountCurrency,
        @JsonProperty("additionalCurrencies") @NotNull List<Currency> additionalCurrencies
) {
}
