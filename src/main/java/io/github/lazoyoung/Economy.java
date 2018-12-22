package io.github.lazoyoung;

import org.bukkit.Bukkit;

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
    
    public @Nullable
    EconomyHandler getHandler() {
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