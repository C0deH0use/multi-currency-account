package pl.codehouse.nn.bank.account;

import java.math.BigDecimal;
import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

interface CurrencyAccountRepository {

    Flux<CurrencyAccount> findByCustomerId(long customerId);

    Mono<CurrencyAccount> findById(CurrencyAccountPk id);

    Flux<CurrencyAccount> saveAll(List<CurrencyAccount> accounts);

    Mono<CurrencyAccountDto> updateAmountBy(CurrencyAccountPk id, BigDecimal updatedAmount);

}
