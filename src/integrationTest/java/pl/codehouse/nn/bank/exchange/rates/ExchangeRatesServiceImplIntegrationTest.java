package pl.codehouse.nn.bank.exchange.rates;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import pl.codehouse.nn.bank.TestcontainersConfiguration;
import pl.codehouse.nn.bank.account.Currency;
import reactor.test.StepVerifier;

@SpringBootTest
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class ExchangeRatesServiceImplIntegrationTest {

    @Autowired
    private ExchangeRatesService exchangeRatesService;

    @BeforeEach
    void setUp(@Autowired Flyway flyway) {
        flyway.clean();
        flyway.migrate();
        WireMock.reset();
    }

    @Test
    void fetchCurrentRatesForUSD() {
        // When
        var result = exchangeRatesService.fetchCurrentRatesFor(Currency.USD);

        // Then
        StepVerifier.create(result)
                .assertNext(dto -> assertThat(dto)
                        .hasFieldOrPropertyWithValue("currency", Currency.USD)
                        .satisfies(exchange -> assertThat(exchange.rate()).isEqualTo("3.9488"))
                )
                .verifyComplete();
    }
}
