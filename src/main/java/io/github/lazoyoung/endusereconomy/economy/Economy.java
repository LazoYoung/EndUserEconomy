package io.github.lazoyoung.endusereconomy.economy;

import io.github.lazoyoung.endusereconomy.economy.handler.GemsEconomyHandler;
import io.github.lazoyoung.endusereconomy.economy.handler.VaultEconomyHandler;

public enum Economy {
    
    VAULT("Vault", VaultEconomyHandler.class, false),
    GEMS_ECONOMY("GemsEconomy", GemsEconomyHandler.class, true);
    
    private String pluginName;
    private Class handlerClass;
    private boolean multiCurrency;
    
    Economy(String pluginName, Class handlerClass, boolean multiCurrency) {
        this.pluginName = pluginName;
        this.handlerClass = handlerClass;
        this.multiCurrency = multiCurrency;
    }
    
    public String getPluginName() {
        return pluginName;
    }
    
    public Class getHandlerClass() {
        return handlerClass;
    }
    
    public boolean hasMultiCurrency() {
        return multiCurrency;
    }
    
}