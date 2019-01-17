package io.github.lazoyoung.endusereconomy;

import com.zaxxer.hikari.HikariConfig;
import io.github.lazoyoung.database_util.Connector;
import io.github.lazoyoung.endusereconomy.command.BillCommand;
import io.github.lazoyoung.endusereconomy.command.EconomyCommand;
import io.github.lazoyoung.endusereconomy.database.BillTable;
import io.github.lazoyoung.endusereconomy.database.Database;
import io.github.lazoyoung.endusereconomy.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

public class Main extends JavaPlugin {
    
    static String pluginName;
    private static PluginCommand economyCmd;
    private static PluginCommand billCmd;
    
    public static void terminate(@Nonnull Plugin suspender, @Nullable String cause) {
        if (cause == null) {
            cause = "Plugin has been suspended.";
        }
        
        String logLine1 = "The plugin is terminated by " + suspender;
        String logLine2 = "Reason: " + cause;
        final String message = cause;
        CommandExecutor executor = (sender, command, label, args) -> {
            sender.sendMessage(message);
            return true;
        };
        
        if (suspender.getName().equals(pluginName)) {
            logLine1 = "The plugin is terminated.";
        }
        
        shutdownDatabasePool();
        log(ChatColor.YELLOW, logLine1, logLine2);
        economyCmd.setExecutor(executor);
        billCmd.setExecutor(executor);
    }
    
    public static void shutdownDatabasePool() {
        Arrays.stream(Database.values()).forEach(Database::shutdown);
    }
    
    public static Plugin getInstance() {
        return Bukkit.getPluginManager().getPlugin(pluginName);
    }
    
    public static void log(String... message) {
        String prefix = "[" + pluginName + "] ";
        Arrays.stream(message).forEachOrdered(string -> getInstance().getServer().getConsoleSender().sendMessage(prefix + string));
    }
    
    public static void log(ChatColor color, String... message) {
        String prefix = "[" + pluginName + "] ";
        Arrays.stream(message).forEachOrdered(string -> getInstance().getServer().getConsoleSender().sendMessage(color + prefix + string));
    }
    
    @Override
    public void onEnable() {
        if (!isServerCompatible()) {
            return;
        }
        
        pluginName = getName();
        initCommands();
        initDatabase();
        Arrays.stream(Economy.values()).forEach(this::loadEconomy);
    }
    
    @Override
    public void onDisable() {
        shutdownDatabasePool();
    }
    
    private void initDatabase() {
        try {
            HikariConfig config = Database.getHikariConfig();
            Connector connector = new Connector(Bukkit.getLogger(), config);
            BillTable billTable = BillTable.initTable(connector);
            Database.registerTable(Database.BILL_REC, billTable);
            log("Successfully connected to database.");
        } catch (Exception e) {
            e.printStackTrace();
            terminate(this, "Failed to initialize database.");
        }
    }
    
    private void initCommands() {
        BillCommand billExec = new BillCommand();
        EconomyCommand ecoExec = new EconomyCommand();
        economyCmd = getCommand("economy");
        billCmd = getCommand("bill");
        economyCmd.setExecutor(ecoExec);
        economyCmd.setTabCompleter(ecoExec);
        billCmd.setExecutor(billExec);
        billCmd.setTabCompleter(billExec);
    }
    
    private void loadEconomy(Economy type) {
        String pluginName = type.getPluginName();
        Plugin plugin = getServer().getPluginManager().getPlugin(pluginName);
        
        if (plugin != null && plugin.isEnabled()) {
            EconomyAPI.register(type, type.getHandler());
        }
    }
    
    private boolean isServerCompatible() {
        try {
            Class.forName("net.md_5.bungee.api.chat.ComponentBuilder");
            Class.forName("org.bukkit.command.CommandSender$Spigot");
        } catch (ClassNotFoundException e) {
            terminate(this, "This server is incompatible. Please use Spigot instead.");
            return false;
        }
        return true;
    }
    
}