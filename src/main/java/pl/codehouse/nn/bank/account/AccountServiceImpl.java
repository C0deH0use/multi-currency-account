package pl.codehouse.nn.bank.account;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
record AccountServiceImpl(
        CurrencyAccountRepository repository
) implements AccountService {
    private static final Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);

    @Override
    public Mono<List<CurrencyAccountDto>> findAccountsFor(long customerId) {
        return Mono.just(new ArrayList<>());
    }

    @Override
    public Mono<List<CurrencyAccountDto>> createNewAccounts(NewAccountsRequest request) {
        log.info("Creating new currency accounts: {}.", request);
        ArrayList<CurrencyAccount> accounts = new ArrayList<>();
        accounts.add(new CurrencyAccount(new CurrencyAccountPk(request.customerId(), request.mainAccountCurrency()), request.mainAccountBalance(), true));
        List<CurrencyAccount> additionalCurrencyAccounts = request.additionalCurrencies()
                .stream()
                .map(currency -> new CurrencyAccount(new CurrencyAccountPk(request.customerId(), currency), BigDecimal.ZERO, false))
                .toList();
        accounts.addAll(additionalCurrencyAccounts);
        return repository.saveAll(accounts)
                .mapNotNull(CurrencyAccount::toDto)
                .collectList();
    }
}