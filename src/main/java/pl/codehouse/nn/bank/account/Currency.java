package pl.codehouse.nn.bank.account;

/**
 * Represents the currencies supported by the multi-currency bank account system.
 * This enum defines the available currencies for transactions and account balances.
 */
public enum Currency {
    /**
     * Polish Złoty (PLN) - The official currency of Poland.
     * It is the primary currency for Polish bank accounts in this system.
     */
    PLN,

    /**
     * United States Dollar (USD) - The official currency of the United States and several other countries.
     * It is widely used in international transactions and is a common secondary currency option.
     */
    USD,

    /**
     * Euro (EUR) - The official currency of the Eurozone and several other European countries.
     * It is an important currency for European transactions and is often used as a secondary currency option.
     */
    EUR;

    /**
     * Checks if the currency is a valid main account currency.
     * Currently, all defined currencies are valid as main account currencies.
     *
     * @return true if the currency can be used as a main account currency, false otherwise.
     */
    public boolean isValidMainAccountCurrency() {
        return true; // All currencies are currently valid as main account currencies
    }

    /**
     * Provides the default currency for new accounts when no specific currency is specified.
     *
     * @return the default Currency (PLN in this case)
     */
    public static Currency getDefaultCurrency() {
        return PLN;
    }
}
