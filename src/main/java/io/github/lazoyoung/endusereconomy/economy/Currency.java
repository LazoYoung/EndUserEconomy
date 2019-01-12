package io.github.lazoyoung.endusereconomy.economy;

import io.github.lazoyoung.endusereconomy.economy.handler.EconomyHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class Currency {
    
    private Economy economy;
    private EconomyHandler handler;
    private String currency;
    
    public Currency(Economy economy) throws IllegalArgumentException {
        if (economy.hasMultiCurrency()) {
            throw new IllegalArgumentException("This economy has multiple currency. Please define currency name.");
        }
        this.economy = economy;
        this.handler = economy.getHandler();
    }
    
    public Currency(Economy economy, String currency) throws IllegalArgumentException {
        this.economy = economy;
        this.handler = economy.getHandler();
        this.currency = currency.toLowerCase();
        if (!economy.hasMultiCurrency()) {
            throw new IllegalArgumentException("Economy " + economy.getPluginName() + " does not support multiple currency.");
        }
        if (economy.getHandler() == null) {
            throw new IllegalStateException("Economy " + economy.getPluginName() + " is not handled by this plugin yet.");
        }
        if (!economy.getHandler().isValidCurrency(currency)) {
            throw new IllegalArgumentException("Unknown currency: " + currency);
        }
    }
    
    public Economy getEconomy() {
        return economy;
    }
    
    @Nullable
    public String getName() {
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
    public String toString() {
        String str = economy.getPluginName();
        if (currency != null) {
            str += ("." + currency);
        }
        return str;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(economy, currency);
    }
}
