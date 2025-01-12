package pl.codehouse.nn.bank.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record NewAccountsRequest(
        @JsonProperty("customerId") long customerId,
        @JsonProperty("mainAccountBalance") @NotNull @Min(0) BigDecimal mainAccountBalance,
        @JsonProperty("mainAccountCurrency") @Nullable Currency mainAccountCurrency,
        @JsonProperty("additionalCurrencies") @Nullable List<Currency> additionalCurrencies
) {
}
