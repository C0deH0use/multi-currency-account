package pl.codehouse.nn.bank.customer;

import java.util.List;
import pl.codehouse.nn.bank.account.CurrencyAccountDto;

/**
 * Data Transfer Object (DTO) representing a customer with their account details.
 * This record encapsulates customer information and their associated currency accounts.
 */
public record CustomerDto(
        long accountId,
        String firstName,
        String lastName,
        List<CurrencyAccountDto> accountBalance
) {

    /**
     * Creates a CustomerDto from a Customer entity and a list of CurrencyAccountDto objects.
     *
     * @param entity The Customer entity to convert.
     * @param currencyAccounts A list of CurrencyAccountDto objects representing the customer's currency accounts.
     * @return A new CustomerDto instance.
     */
    static CustomerDto from(Customer entity, List<CurrencyAccountDto> currencyAccounts) {
        return new CustomerDto(entity.accountId(), entity.firstName(), entity.lastName(), currencyAccounts);
    }
}
