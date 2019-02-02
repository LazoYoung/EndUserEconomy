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
import io.github.lazoyoung.endusereconomy.util.Text;
import me.kangarko.ui.UIDesignerAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.PluginNameConversationPrefix;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

public class Main extends JavaPlugin {
    
    public static String pluginName;
    private static ConversationFactory convFactory;
    
    @Override
    public void onEnable() {
        if (!isServerCompatible()) {
            return;
        }
        
        try {
            pluginName = getName();
            initConversation();
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
        Arrays.stream(Database.values()).forEach(Database::shutdown);
    }
    
    private void initDatabase() {
        HikariConfig config = Database.getHikariConfig();
        Connector connector = new Connector(Bukkit.getLogger(), config);
        BillTable billTable = new BillTable(connector);
        BankTable bankTable = new BankTable(connector);
        Database.registerTable(Database.BILL_RECORD, billTable);
        Database.registerTable(Database.BANK_TRANSACTION, bankTable);
        Text.log("Successfully connected to database.");
    }
    
    public static void terminate(@Nonnull Plugin suspender, @Nullable String cause) {
        Text.log("Plugin is being terminated.", "Suspender: " + suspender.getName() + ", Cause: " + cause);
        Bukkit.getPluginManager().disablePlugin(Main.getInstance());
    }
    
    public static Plugin getInstance() {
        return JavaPlugin.getPlugin(Main.class);
    }
    
    public static ConversationFactory getConversationFactory() {
        return convFactory;
    }
    
    private void initCommands() {
        BillCommand billExec = new BillCommand();
        EconomyCommand ecoExec = new EconomyCommand();
        AccountCommand accountExec = new AccountCommand();
        PluginCommand economyCmd = getCommand("economy");
        PluginCommand billCmd = getCommand("bill");
        PluginCommand accountCmd = getCommand("account");
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
                Text.log("Hooked economy: " + pluginName);
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Text.log("Failed to load economy: " + pluginName);
    }
    
    private void initConversation() {
        convFactory = new ConversationFactory(this)
                .withPrefix(new PluginNameConversationPrefix(this))
                .withTimeout(60)
                .withEscapeSequence("exit")
                .thatExcludesNonPlayersWithMessage("Only players can do this.")
                .withModality(false);
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