package pl.codehouse.nn.bank.account;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private CurrencyAccountRepository repository;

    @InjectMocks
    private AccountServiceImpl sut;

    @Test
    @DisplayName("should save new main account with proper balance")
    void should_SaveNewMainAccountWithProperBalance() {
        // given
        var customerId = 1000L;
        var mainAccountBalance = BigDecimal.valueOf(97.98);
        var mainAccountCurrency = Currency.PLN;

        NewAccountsRequest request = new NewAccountsRequest(customerId, mainAccountBalance, mainAccountCurrency, List.of());
        given(repository.saveAll(anyList())).willAnswer(params -> Flux.fromIterable(params.getArgument(0)));

        // when
        Mono<List<CurrencyAccountDto>> createdAccountsMono = sut.createNewAccounts(request);

        // then
        StepVerifier.create(createdAccountsMono)
                .assertNext(accounts -> {
                    assertThat(accounts.size()).isEqualTo(1);
                    assertThat(accounts.get(0))
                        .hasFieldOrPropertyWithValue("amount", mainAccountBalance)
                        .hasFieldOrPropertyWithValue("currency", mainAccountCurrency)
                        .hasFieldOrPropertyWithValue("isMainAccount", true);
                }
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("should create additional accounts when additionalCurrencies are not empty")
    void should_CreateAdditionalAccounts_When_AdditionalCurrenciesAreEmpty() {
        // given
        var customerId = 1000L;
        var mainAccountBalance = BigDecimal.valueOf(97.98);
        var mainAccountCurrency = Currency.PLN;

        NewAccountsRequest request = new NewAccountsRequest(customerId, mainAccountBalance, mainAccountCurrency, List.of(Currency.USD, Currency.EUR));
        given(repository.saveAll(anyList())).willAnswer(params -> Flux.fromIterable(params.getArgument(0)));

        // when
        Mono<List<CurrencyAccountDto>> createdAccountsMono = sut.createNewAccounts(request);

        // then
        StepVerifier.create(createdAccountsMono)
                .assertNext(accounts -> {
                                assertThat(accounts.size()).isEqualTo(3);
                                assertThat(accounts.get(0))
                                        .hasFieldOrPropertyWithValue("amount", mainAccountBalance)
                                        .hasFieldOrPropertyWithValue("currency", mainAccountCurrency)
                                        .hasFieldOrPropertyWithValue("isMainAccount", true);
                                assertThat(accounts.get(1))
                                        .hasFieldOrPropertyWithValue("amount", BigDecimal.ZERO)
                                        .hasFieldOrPropertyWithValue("currency", Currency.USD)
                                        .hasFieldOrPropertyWithValue("isMainAccount", false);
                                assertThat(accounts.get(2))
                                        .hasFieldOrPropertyWithValue("amount", BigDecimal.ZERO)
                                        .hasFieldOrPropertyWithValue("currency", Currency.EUR)
                                        .hasFieldOrPropertyWithValue("isMainAccount", false);
                            }
                )
                .verifyComplete();
    }
}