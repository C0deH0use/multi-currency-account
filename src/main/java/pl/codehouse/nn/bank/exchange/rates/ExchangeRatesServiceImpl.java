package pl.codehouse.nn.bank.exchange.rates;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pl.codehouse.nn.bank.account.Currency;
import reactor.core.publisher.Mono;

@Service
record ExchangeRatesServiceImpl(
        @Qualifier("exchangeRatesWebClient") WebClient webClient,
        ExchangeRatesProperties properties) implements ExchangeRatesService {

    private static final String API_EXCHANGE_RATES_TABLE_CODE_URL = "/api/exchangerates/rates/{table}/{code}/";

    @Override
    public Mono<ExchangeRateDto> fetchCurrentRatesFor(Currency currency) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(API_EXCHANGE_RATES_TABLE_CODE_URL)
                        .build(properties.sourceTable(), currency.name()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ExchangeRateResponse>() {})
                .map(response -> new ExchangeRateDto(currency, BigDecimal.valueOf(response.rates().get(0).mid())))
                .onErrorResume(e -> Mono.error(new RuntimeException("Error fetching exchange rate for " + currency, e)));
    }

    private record ExchangeRateResponse(
            @JsonProperty("table") String table,
            @JsonProperty("currency") String currency,
            @JsonProperty("code") String code,
            @JsonProperty("rates") List<RateResponse> rates) {
    }

    private record RateResponse(
            @JsonProperty("no") String no,
            @JsonProperty("effectiveDate") LocalDate effectiveDate,
            @JsonProperty("mid") double mid) {
    }
}
