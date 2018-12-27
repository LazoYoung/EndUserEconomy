package io.github.lazoyoung;

import io.github.lazoyoung.bill.BillFactory;
import io.github.lazoyoung.command.BillCommand;
import io.github.lazoyoung.command.EconomyCommand;
import io.github.lazoyoung.economy.Economy;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.Arrays;

public class Main extends JavaPlugin {
    
    public static String pluginName;
    
    @Override
    public void onEnable() {
        pluginName = getName();
        saveDefaultConfig();
        initDatabase();
        Arrays.stream(Economy.values()).forEach(this::loadEconomy);
        getCommand("economy").setExecutor(new EconomyCommand());
        getCommand("bill").setExecutor(new BillCommand());
    }
    
    @Override
    public void onDisable() {
        Database.shutdown();
    }
    
    private void initDatabase() {
        try {
            Database.init(getConfig());
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            CommandExecutor e = (sender, command, label, args) -> {
                sender.sendMessage("Database has not been initialized.");
                return true;
            };
            getCommand("economy").setExecutor(e);
            getCommand("bill").setExecutor(e);
        }
    }
    
    private void loadEconomy(Economy type) {
        String pluginName = type.getPluginName();
        Plugin plugin = getServer().getPluginManager().getPlugin(pluginName);
        
        if (plugin != null && plugin.isEnabled()) {
            EconomyAPI.register(type, type.getHandler());
        }
    }
    
}
