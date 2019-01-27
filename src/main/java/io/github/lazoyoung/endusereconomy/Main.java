package io.github.lazoyoung.endusereconomy;

import com.zaxxer.hikari.HikariConfig;
import io.github.lazoyoung.database_util.Connector;
import io.github.lazoyoung.endusereconomy.command.AccountCommand;
import io.github.lazoyoung.endusereconomy.command.BillCommand;
import io.github.lazoyoung.endusereconomy.command.EconomyCommand;
import io.github.lazoyoung.endusereconomy.database.BankTable;
import io.github.lazoyoung.endusereconomy.database.BillTable;
import io.github.lazoyoung.endusereconomy.database.Database;
import io.github.lazoyoung.endusereconomy.economy.Economy;
import io.github.lazoyoung.endusereconomy.economy.handler.EconomyHandler;
import me.kangarko.ui.UIDesignerAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

public class Main extends JavaPlugin {
    
    static String pluginName;
    private static PluginCommand economyCmd;
    private static PluginCommand billCmd;
    private static PluginCommand accountCmd;
    
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
        return JavaPlugin.getPlugin(Main.class);
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
        
        try {
            pluginName = getName();
            initCommands();
            initDatabase();
            UIDesignerAPI.setPlugin(this);
            Arrays.stream(Economy.values()).forEach(this::loadEconomy);
        } catch (Exception e) {
            e.printStackTrace();
            terminate(this, "Failed to initialize plugin.");
        }
    }
    
    @Override
    public void onDisable() {
        shutdownDatabasePool();
    }
    
    private void initDatabase() {
        HikariConfig config = Database.getHikariConfig();
        Connector connector = new Connector(Bukkit.getLogger(), config);
        BillTable billTable = new BillTable(connector);
        BankTable bankTable = new BankTable(connector);
        Database.registerTable(Database.BILL_RECORD, billTable);
        Database.registerTable(Database.BANK_TRANSACTION, bankTable);
        log("Successfully connected to database.");
    }
    
    private void initCommands() {
        BillCommand billExec = new BillCommand();
        EconomyCommand ecoExec = new EconomyCommand();
        AccountCommand accountExec = new AccountCommand();
        economyCmd = getCommand("economy");
        billCmd = getCommand("bill");
        accountCmd = getCommand("account");
        economyCmd.setExecutor(ecoExec);
        economyCmd.setTabCompleter(ecoExec);
        billCmd.setExecutor(billExec);
        billCmd.setTabCompleter(billExec);
        accountCmd.setExecutor(accountExec);
        accountCmd.setTabCompleter(accountExec);
    }
    
    private void loadEconomy(Economy type) {
        String pluginName = type.getPluginName();
        Plugin plugin = getServer().getPluginManager().getPlugin(pluginName);
        
        if (plugin != null && plugin.isEnabled()) {
            try {
                EconomyHandler handler = (EconomyHandler) type.getHandlerClass().newInstance();
                EconomyHandler.register(type, handler);
                getServer().getPluginManager().registerEvents((Listener) handler, this);
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
            log("Hooked economy: " + pluginName);
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