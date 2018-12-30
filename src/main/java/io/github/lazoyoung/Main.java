package io.github.lazoyoung;

import io.github.lazoyoung.command.BillCommand;
import io.github.lazoyoung.command.EconomyCommand;
import io.github.lazoyoung.database.Database;
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
        if (!canBoot()) {
            setEnabled(false);
            return;
        }
        
        pluginName = getName();
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
            Database.init();
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
    
    private boolean canBoot() {
        try {
            Class.forName("net.md_5.bungee.api.chat.ComponentBuilder");
            Class.forName("org.bukkit.command.CommandSender.Spigot");
        } catch (ClassNotFoundException e) {
            getLogger().severe("Use Spigot server to run this plugin.");
            return false;
        }
        return true;
    }
    
}
