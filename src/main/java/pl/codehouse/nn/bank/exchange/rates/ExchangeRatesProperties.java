package pl.codehouse.nn.bank.exchange.rates;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for exchange rates.
 * This record holds the configuration values for the exchange rates service.
 */
@ConfigurationProperties("app.exchange-rates")
public record ExchangeRatesProperties(
        String baseUrl,
        String sourceTable
) {

}
