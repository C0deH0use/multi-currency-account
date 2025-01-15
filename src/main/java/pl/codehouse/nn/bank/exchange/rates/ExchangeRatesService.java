package pl.codehouse.nn.bank.exchange.rates;

import pl.codehouse.nn.bank.account.Currency;
import reactor.core.publisher.Mono;

/**
 * Service interface for fetching exchange rates.
 * This service provides methods to retrieve current exchange rates for different currencies.
 */
public interface ExchangeRatesService {

    /**
     * Fetches the current exchange rate for a specified currency.
     *
     * @param currency The currency for which to fetch the exchange rate.
     * @return A Mono emitting an ExchangeRateDto containing the current exchange rate information.
     *         If the rate cannot be fetched, the Mono may complete empty or with an error.
     */
    Mono<ExchangeRateDto> fetchCurrentRatesFor(Currency currency);
}
