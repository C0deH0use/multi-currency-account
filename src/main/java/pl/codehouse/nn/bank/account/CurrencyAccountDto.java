package pl.codehouse.nn.bank.account;

import java.math.BigDecimal;

/**
 * Data Transfer Object (DTO) representing a currency account.
 * This record encapsulates the details of a single currency account for a customer.
 */
public record CurrencyAccountDto(
        Currency currency,
        BigDecimal amount,
        boolean isMainAccount
) {
}
