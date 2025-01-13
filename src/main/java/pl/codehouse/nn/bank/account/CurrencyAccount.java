package pl.codehouse.nn.bank.account;

import java.math.BigDecimal;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("currency_accounts")
record CurrencyAccount(
        @Id
        CurrencyAccountPk id,
        BigDecimal amount,
        boolean isMainAccount
) {

    CurrencyAccountDto toDto() {
        return new CurrencyAccountDto(this.id.currency(), this.amount(), this.isMainAccount());
    }
}
