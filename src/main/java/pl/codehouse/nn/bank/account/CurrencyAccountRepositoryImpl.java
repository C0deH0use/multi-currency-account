package pl.codehouse.nn.bank.account;

import io.r2dbc.spi.Readable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
class CurrencyAccountRepositoryImpl implements CurrencyAccountRepository {
    private static final String INSERT_SQL_TEMPLATE = """
            INSERT INTO currency_accounts(customer_id, currency, amount, is_main_account) 
            VALUES (:customerId, :currency, :amount, :isMainAccount) RETURNING customer_id, currency, amount, is_main_account;
            """;

    private static final String updateSql = """
            UPDATE currency_accounts
            SET amount = :updatedAmount
            WHERE customer_id = :customerId AND currency = :currency
            RETURNING customer_id, currency, amount, is_main_account
            """;
    private static final Logger log = LoggerFactory.getLogger(CurrencyAccountRepositoryImpl.class);
    private final R2dbcEntityTemplate entityTemplate;

    CurrencyAccountRepositoryImpl(R2dbcEntityTemplate entityTemplate) {
        this.entityTemplate = entityTemplate;
    }

    @Override
    public Flux<CurrencyAccount> findByCustomerId(long customerId) {
        return entityTemplate.select(CurrencyAccount.class)
                .matching(Query.query(Criteria.where("customer_id").is(customerId)))
                .all();
    }

    @Override
    public Mono<CurrencyAccount> findById(CurrencyAccountPk id) {
        return entityTemplate.select(CurrencyAccount.class)
                .matching(Query.query(
                        Criteria.where("customer_id").is(id.customerId())
                                .and("currency").is(id.currency().name())
                ))
                .first();
    }

    @Override
    public Flux<CurrencyAccount> saveAll(List<CurrencyAccount> accounts) {
        return Flux.fromIterable(accounts)
                .flatMap(account -> entityTemplate.getDatabaseClient().sql(INSERT_SQL_TEMPLATE)
                        .bind("customerId", account.id().customerId())
                        .bind("currency", account.id().currency().name())
                        .bind("amount", account.amount())
                        .bind("isMainAccount", account.isMainAccount())
                        .map(CurrencyAccountRepositoryImpl::mapToCurrencyAccount)
                        .one()
                );
    }

    @Override
    public Mono<CurrencyAccountDto> updateAmountBy(CurrencyAccountPk id, BigDecimal updatedAmount) {
        log.info("Updating Customer CurrencyAccount ({}) with updatedAmount => {}.", id, updatedAmount);
        return entityTemplate.getDatabaseClient().sql(updateSql)
                .bind("customerId", id.customerId())
                .bind("currency", id.currency().name())
                .bind("updatedAmount", updatedAmount)
                .map(CurrencyAccountRepositoryImpl::mapToCurrencyAccount)
                .one()
                .map(CurrencyAccount::toDto);
    }

    private static CurrencyAccount mapToCurrencyAccount(Readable readable) {
        Long customerId = readable.get("customer_id", Long.class);
        Currency currency = Currency.valueOf(readable.get("currency", String.class));
        BigDecimal amount = readable.get("amount", BigDecimal.class);
        Boolean isMainAccount = readable.get("is_main_account", Boolean.class);
        return new CurrencyAccount(new CurrencyAccountPk(customerId, currency), amount, isMainAccount);
    }
}
