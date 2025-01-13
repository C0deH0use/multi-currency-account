package pl.codehouse.nn.bank.customer;

import org.springframework.stereotype.Service;
import pl.codehouse.nn.bank.ExecutionResult;
import pl.codehouse.nn.bank.account.AccountService;
import reactor.core.publisher.Mono;

@Service
record CustomerApiImpl(
        CustomerRepository repository,
        AccountService accountService,
        CreateCustomerCommand createCustomerCommand
) implements CustomerApi {

    @Override
    public Mono<CustomerDto> fetchCustomer(long customerId) {
        return repository.findById(customerId)
                .zipWith(accountService.findAccountsFor(customerId), CustomerDto::from);
    }

    @Override
    public Mono<CustomerDto> createAccount(CreateCustomerRequest request) {
        return createCustomerCommand.execute(request)
                .map(ExecutionResult::handle);
    }
}
