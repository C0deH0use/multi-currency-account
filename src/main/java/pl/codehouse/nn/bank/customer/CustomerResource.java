package pl.codehouse.nn.bank.customer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.codehouse.nn.bank.ExecutionResult;
import reactor.core.publisher.Mono;

@Validated
@RestController
@RequestMapping(value = "/customers", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
class CustomerResource {
    private final CustomerApi customerApi;
    private final CreateCustomerCommand createCustomerCommand;

    CustomerResource(CustomerApi customerApi, CreateCustomerCommand createCustomerCommand) {
        this.customerApi = customerApi;
        this.createCustomerCommand = createCustomerCommand;
    }

    @GetMapping("/{customerId}")
    Mono<CustomerDto> fetchGivenCustomer(@PathVariable @Valid @NotNull Long customerId) {
        return customerApi.fetchCustomer(customerId)
                .switchIfEmpty(Mono.error(new CustomerNotFoundException(customerId)));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    Mono<CustomerDto> createCustomer(@RequestBody @Valid CreateCustomerRequest request) {
        return createCustomerCommand.execute(request)
                .map(ExecutionResult::handle);
    }
}
