package io.github.lazoyoung.economy;

import io.github.lazoyoung.economy.handler.EconomyHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class Currency {
    
    private Economy economy;
    private EconomyHandler handler;
    private String currency;
    
    public Currency(Economy economy, EconomyHandler handler) throws IllegalArgumentException {
        if (economy.hasMultiCurrency()) {
            throw new IllegalArgumentException("This economy has multiple currency. Please define currency name.");
        }
        this.economy = economy;
        this.handler = handler;
    }
    
    public Currency(Economy economy, EconomyHandler handler, String currency) throws IllegalArgumentException {
        if (!economy.hasMultiCurrency()) {
            throw new IllegalArgumentException("This economy does not support multiple currency.");
        }
        if (!handler.isValidCurrency(currency)) {
            throw new IllegalArgumentException("Unknown currency: " + currency);
        }
        this.economy = economy;
        this.handler = handler;
        this.currency = currency.toLowerCase();
    }
    
    public Economy getEconomy() {
        return economy;
    }
    
    @Nullable
    public String getCurrency() {
        return currency;
    }
    
    @Nonnull
    public EconomyHandler getEconomyHandler() {
        return handler;
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
