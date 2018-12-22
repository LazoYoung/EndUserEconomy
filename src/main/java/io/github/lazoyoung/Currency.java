package io.github.lazoyoung;

import java.util.Objects;

public class Currency {
    
    private Economy economy;
    private String currency;
    
    public Currency(Economy economy) throws IllegalArgumentException {
        if (economy.hasMultiCurrency()) {
            throw new IllegalArgumentException("This economy has multiple currency. Please define currency name.");
        }
        this.economy = economy;
    }
    
    public Currency(Economy economy, String currency) {
        if (!economy.hasMultiCurrency()) {
            throw new IllegalArgumentException("This economy does not support multiple currency.");
        }
        this.economy = economy;
        this.currency = currency.toLowerCase();
    }
    
    public Economy getEconomy() {
        return economy;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Currency currency1 = (Currency) o;
        return economy == currency1.economy &&
                Objects.equals(currency, currency1.currency);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(economy, currency);
    }
}
