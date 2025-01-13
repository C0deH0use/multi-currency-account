package pl.codehouse.nn.bank;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

@Configuration
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
