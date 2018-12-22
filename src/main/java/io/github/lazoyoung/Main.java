package io.github.lazoyoung;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class Main extends JavaPlugin {
    
    @Override
    public void onEnable() {
        Arrays.stream(Economy.values()).forEach(this::loadEconomy);
        getCommand("economy").setExecutor(new EconomyCommand());
    }
    
    private void loadEconomy(Economy type) {
        String pluginName = type.getPluginName();
        Plugin plugin = getServer().getPluginManager().getPlugin(pluginName);
        
        if (plugin != null && plugin.isEnabled()) {
            EconomyAPI.register(type, type.getHandler());
        }
    }
    
}
