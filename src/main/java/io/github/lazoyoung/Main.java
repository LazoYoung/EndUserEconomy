package io.github.lazoyoung;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class Main extends JavaPlugin {
    
    @Override
    public void onEnable() {
        Arrays.stream(EconomyType.values()).forEach(this::loadEconomy);
    }
    
    private void loadEconomy(EconomyType type) {
        String pluginName = type.getPluginName();
        Plugin plugin = getServer().getPluginManager().getPlugin(pluginName);
        
        if (plugin != null && plugin.isEnabled()) {
            // TODO register economy systems in STARTUP phase
            try {
                EconomyAPI.register(type, type.getEconomyService());
            } catch (IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
                getLogger().severe("Failed to load economy \'" + type.getPluginName() + "\' while service instantiation.");
            }
        }
    }
    
}
