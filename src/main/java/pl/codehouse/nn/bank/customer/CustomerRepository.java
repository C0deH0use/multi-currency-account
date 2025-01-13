package pl.codehouse.nn.bank.customer;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface CustomerRepository extends ReactiveCrudRepository<Customer, Long> {

}
