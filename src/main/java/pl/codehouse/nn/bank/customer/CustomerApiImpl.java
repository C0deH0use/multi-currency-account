package pl.codehouse.nn.bank.customer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pl.codehouse.nn.bank.ExecutionResult;
import reactor.core.publisher.Mono;

@Service
record CustomerApiImpl(
        CreateCustomerCommand createCustomerCommand
) implements CustomerApi {
    private static final Logger log = LoggerFactory.getLogger(ExecutionResult.class);

    @Override
    public Mono<CustomerDto> createAccount(CreateCustomerRequest request) {
        return createCustomerCommand.execute(request)
                .map(ExecutionResult::handle);
    }
}
