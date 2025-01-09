package pl.codehouse.nn.bank;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class MultiCurrencyBankAccountApplicationTest {

    @Test
    @DisplayName("should start the application context")
    void should_startApplicationContext() {
    }

}