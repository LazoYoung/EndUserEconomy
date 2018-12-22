package io.github.lazoyoung;

import org.bukkit.Bukkit;

import javax.annotation.Nullable;

public enum Economy {
    
    VAULT_ECONOMY("Vault", VaultEconomyHandler.class, false),
    GEMS_ECONOMY("GemsEconomy", GemsEconomyHandler.class, true);
    
    private String pluginName;
    private Class classType;
    private boolean multiCurrency;
    
    Economy(String pluginName, Class classType, boolean multiCurrency) {
        this.pluginName = pluginName;
        this.classType = classType;
        this.multiCurrency = multiCurrency;
    }
    
    public String getPluginName() {
        return pluginName;
    }
    
    public @Nullable
    EconomyHandler getHandler() {
        EconomyHandler val = null;
        try {
            val = (EconomyHandler) classType.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            Bukkit.getLogger().severe("Failed to load EconomyHandler.");
        }
        return val;
    }
    
    public boolean hasMultiCurrency() {
        return multiCurrency;
    }
    
}