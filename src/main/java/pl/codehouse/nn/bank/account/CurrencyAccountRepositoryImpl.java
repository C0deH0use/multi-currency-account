package pl.codehouse.nn.bank.account;

import io.r2dbc.spi.Readable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
class CurrencyAccountRepositoryImpl implements CurrencyAccountRepository {
    private static final String INSERT_SQL_TEMPLATE = """
            INSERT INTO currency_accounts(customer_id, currency, amount, is_main_account) 
            VALUES (:customerId, :currency, :amount, :isMainAccount) RETURNING customer_id, currency, amount, is_main_account;
            """;
    private final R2dbcEntityTemplate entityTemplate;

    CurrencyAccountRepositoryImpl(R2dbcEntityTemplate entityTemplate) {
        this.entityTemplate = entityTemplate;
    }

    @Override
    public Mono<List<CurrencyAccount>> findById(CurrencyAccountPk id) {
        return null;
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

    private static CurrencyAccount mapToCurrencyAccount(Readable readable) {
        Long customerId = readable.get("customer_id", Long.class);
        Currency currency = Currency.valueOf(readable.get("currency", String.class));
        BigDecimal amount = readable.get("amount", BigDecimal.class);
        Boolean isMainAccount = readable.get("is_main_account", Boolean.class);
        return new CurrencyAccount(new CurrencyAccountPk(customerId, currency), amount, isMainAccount);
    }
}
