package pl.codehouse.nn.bank.account;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
class DummyAccountServiceImpl implements AccountService {
    @Override
    public Mono<List<CurrencyAccountDto>> findAccountsFor(long customerId) {
        return Mono.just(new ArrayList<>());
    }

    @Override
    public Mono<List<CurrencyAccountDto>> createNewAccounts(NewAccountsRequest request) {
        return Mono.just(new ArrayList<>());
    }
}
