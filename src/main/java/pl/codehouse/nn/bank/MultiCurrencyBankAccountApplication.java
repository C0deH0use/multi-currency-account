package pl.codehouse.nn.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Main application class for the MultiCurrency Bank Account service.
 * This class serves as the entry point for the Spring Boot application.
 *
 * <p>It is annotated with:
 * <ul>
 *     <li>{@link SpringBootApplication} to enable auto-configuration and component scanning</li>
 *     <li>{@link EnableConfigurationProperties} to enable support for {@code @ConfigurationProperties}</li>
 *     <li>{@link ConfigurationPropertiesScan} to scan for {@code @ConfigurationProperties} classes</li>
 * </ul>
 * </p>
 */
@SpringBootApplication
@EnableConfigurationProperties
@ConfigurationPropertiesScan
public class MultiCurrencyBankAccountApplication {

    /**
     * The main method which serves as the entry point for the application.
     *
     * @param args command line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(MultiCurrencyBankAccountApplication.class, args);
    }

}
