package pl.codehouse.nn.bank.account;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface CurrencyAccountRepository extends ReactiveCrudRepository<CurrencyAccount, Integer> {

}
