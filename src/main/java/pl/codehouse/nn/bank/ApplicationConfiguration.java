package pl.codehouse.nn.bank;

import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.reactive.function.client.WebClient;
import pl.codehouse.nn.bank.exchange.rates.ExchangeRatesProperties;

@Configuration
@EnableConfigurationProperties({ExchangeRatesProperties.class})
class ApplicationConfiguration {
    @Bean
    LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }

    @Bean
    MethodValidationPostProcessor validationPostProcessor(LocalValidatorFactoryBean validator) {
        MethodValidationPostProcessor processor = new MethodValidationPostProcessor();
        processor.setAdaptConstraintViolations(true);
        processor.setValidator(validator);
        return processor;
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

}
