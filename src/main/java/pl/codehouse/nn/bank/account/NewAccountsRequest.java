package pl.codehouse.nn.bank.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * Represents a request to create new accounts for a customer.
 * This record encapsulates the necessary information to create one or more currency accounts
 * for a specific customer, including the main account and any additional currency accounts.
 */
public record NewAccountsRequest(
        @JsonProperty("customerId") long customerId,
        @JsonProperty("mainAccountBalance") @NotNull @DecimalMin("0.00") BigDecimal mainAccountBalance,
        @JsonProperty("mainAccountCurrency") @Nullable Currency mainAccountCurrency,
        @JsonProperty("additionalCurrencies") @NotNull List<Currency> additionalCurrencies
) {
}
