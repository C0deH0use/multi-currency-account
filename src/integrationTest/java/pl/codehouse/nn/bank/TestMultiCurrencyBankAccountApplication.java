package pl.codehouse.nn.bank;

import org.springframework.boot.SpringApplication;

public class TestMultiCurrencyBankAccountApplication {

    public static void main(String[] args) {
        SpringApplication.from(MultiCurrencyBankAccountApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }

}
