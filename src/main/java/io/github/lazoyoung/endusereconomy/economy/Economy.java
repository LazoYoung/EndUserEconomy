package io.github.lazoyoung.endusereconomy.economy;

import io.github.lazoyoung.endusereconomy.economy.handler.EconomyHandler;
import io.github.lazoyoung.endusereconomy.economy.handler.GemsEconomyHandler;
import io.github.lazoyoung.endusereconomy.economy.handler.VaultEconomyHandler;

import javax.annotation.Nullable;

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
    
    @Nullable
    public EconomyHandler getHandler() {
        EconomyHandler val = null;
        try {
            val = (EconomyHandler) handlerClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return val;
    }
    
    public boolean hasMultiCurrency() {
        return multiCurrency;
    }
    
}