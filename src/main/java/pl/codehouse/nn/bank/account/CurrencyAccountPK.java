package pl.codehouse.nn.bank.account;

import org.springframework.data.relational.core.mapping.Column;

record CurrencyAccountPK(
        @Column("customer_id") long customerId,
        @Column("currency") Currency currency
) {
}
