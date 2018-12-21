package io.github.lazoyoung;

import org.bukkit.Bukkit;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public enum EconomyType {
    
    ESSENTIALS("Essentials", EssentialsEconomy.class, false),
    GEMS_ECONOMY("GemsEconomy", GemsEconomy.class, true);
    
    public static Map<Class, Economy> providers = new HashMap<>();
    private String pluginName;
    private Class classType;
    private boolean multiCurrency;
    
    EconomyType(String pluginName, Class classType, boolean multiCurrency) {
        this.pluginName = pluginName;
        this.classType = classType;
        this.multiCurrency = multiCurrency;
    }
    
    public String getPluginName() {
        return pluginName;
    }
    
    public @Nullable Economy getEconomyService() throws IllegalAccessException, InstantiationException {
        return providers.get(classType);
    }
    
    public boolean hasMultiCurrency() {
        return multiCurrency;
    }
    
}