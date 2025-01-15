package pl.codehouse.nn.bank.account;

import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

interface CurrencyAccountRepository {

    Mono<List<CurrencyAccount>> findById(CurrencyAccountPk id);

    Flux<CurrencyAccount> saveAll(List<CurrencyAccount> accounts);
}
