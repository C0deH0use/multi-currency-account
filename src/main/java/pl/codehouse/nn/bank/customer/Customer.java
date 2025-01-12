package pl.codehouse.nn.bank.customer;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("customers")
record Customer(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column("id")
    long accountId,
    @Column("first_name")
    String firstName,
    @Column("last_name")
    String lastName
) {

    private static final int NEW_ACCOUNT_ID = 0;

    static Customer createNew(String firstName, String lastName) {
        return new Customer(NEW_ACCOUNT_ID, firstName, lastName);
    }
}
