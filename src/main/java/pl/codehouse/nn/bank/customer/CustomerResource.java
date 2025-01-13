package pl.codehouse.nn.bank.customer;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
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
    private final CreateCustomerCommand createCustomerCommand;

    CustomerResource(CreateCustomerCommand createCustomerCommand) {
        this.createCustomerCommand = createCustomerCommand;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    Mono<CustomerDto> createCustomer(@RequestBody @Valid CreateCustomerRequest request) {
        return createCustomerCommand.execute(request)
                .map(ExecutionResult::handle);
    }
}
