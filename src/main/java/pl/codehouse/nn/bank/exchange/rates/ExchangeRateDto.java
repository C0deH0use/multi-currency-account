package pl.codehouse.nn.bank.exchange.rates;

import java.math.BigDecimal;
import pl.codehouse.nn.bank.account.Currency;

/**
 * Data Transfer Object (DTO) representing an exchange rate.
 * This record encapsulates the currency and its corresponding exchange rate.
 */
public record ExchangeRateDto(
        Currency currency,
        BigDecimal rate
) {
}
