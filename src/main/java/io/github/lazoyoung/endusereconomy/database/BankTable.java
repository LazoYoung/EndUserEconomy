package io.github.lazoyoung.endusereconomy.database;

import io.github.lazoyoung.database_util.Callback;
import io.github.lazoyoung.database_util.Connector;
import io.github.lazoyoung.database_util.Table;
import io.github.lazoyoung.endusereconomy.Config;
import io.github.lazoyoung.endusereconomy.Main;
import io.github.lazoyoung.endusereconomy.bank.transaction.TransactionType;
import io.github.lazoyoung.endusereconomy.economy.Currency;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BankTable extends Table {
    
    private Plugin plugin;
    
    public BankTable(Connector connector) {
        super(connector, Config.BANK.get().getString("transaction.table-name"));
    }
    
    @Override
    protected void init() {
        plugin = Main.getPlugin(Main.class);
        Callback<Integer, SQLException> createResult = (result, e) -> {
            if (e != null) {
                Main.terminate(Main.getInstance(), "Failed to create table " + tableName + ". SQLException code: " + e.getSQLState());
            }
        };
        new BukkitRunnable() {
            @Override
            public void run() {
                executeUpdate(createResult, "CREATE TABLE IF NOT EXISTS " + tableName +
                        " (" +
                        "type VARCHAR(16) NOT NULL COMMENT 'deposit, withdraw, transfer', " +
                        "economy VARCHAR(30) NOT NULL COMMENT 'Identical to enums in Economy.java.', " +
                        "currency VARCHAR(15) COMMENT 'This is null for those single-currency economies.', " +
                        "amount MEDIUMINT UNSIGNED NOT NULL, " +
                        "senderResult INT NOT NULL, " +
                        "receiverResult INT NOT NULL, " +
                        "sender VARCHAR(30), " +
                        "receiver VARCHAR(30), " +
                        "date TIMESTAMP NOT NULL, " +
                        "note VARCHAR(30)" +
                        ");");
                execute(null, "SET NAMES 'utf8';");
            }
        }.runTaskAsynchronously(plugin);
    }
    
    public void recordDeposit(Currency currency, int amount, int result, String user, String note) {
        String economy = currency.getEconomy().getPluginName();
        String currencyName = currency.getName();
        String user_ = user.toLowerCase();
        Callback<Integer, SQLException> insertResult = (key, thrown) -> {
            if (thrown == null) {
                Main.log(user_ + " deposit " + amount + " to his account in economy: " + currency.toString());
            }
        };
        new BukkitRunnable() {
            @Override
            public void run() {
                executeInsert(insertResult, "INSERT INTO " + tableName +
                                " (type, economy, currency, amount, senderResult, receiverResult, sender, receiver, date, note)" +
                                " VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?);",
                        TransactionType.DEPOSIT.toString(), economy, currencyName, amount, result, result, user_, user_, note);
            }
        }.runTaskAsynchronously(plugin);
    }
    
    public void recordWithdraw(Currency currency, int amount, int result, String user, String note) {
        String economy = currency.getEconomy().getPluginName();
        String currencyName = currency.getName();
        String user_ = user.toLowerCase();
        Callback<Integer, SQLException> insertResult = (key, thrown) -> {
            if (thrown == null) {
                Main.log(user_ + " withdrawn " + amount + " from his account in economy: " + currency.toString());
            }
        };
        new BukkitRunnable() {
            @Override
            public void run() {
                executeInsert(insertResult, "INSERT INTO " + tableName +
                                " (type, economy, currency, amount, senderResult, receiverResult, receiver, date, note)" +
                                " VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?);",
                        TransactionType.WITHDRAW.toString(), economy, currencyName, amount, result, result, user_, user_, note);
            }
        }.runTaskAsynchronously(plugin);
    }
    
    public void recordTransfer(Currency currency, int amount, int senderResult, int receiverResult, String sender, String receiver, String note) {
        String economy = currency.getEconomy().getPluginName();
        String currencyName = currency.getName();
        String sender_ = sender.toLowerCase();
        String receiver_ = receiver.toLowerCase();
        Callback<Integer, SQLException> insertResult = (key, thrown) -> {
            if (thrown == null) {
                Main.log(sender_ + " transferred " + amount + " to " + receiver_ + " in economy: " + currency.toString());
            }
        };
        new BukkitRunnable() {
            @Override
            public void run() {
                executeInsert(insertResult, "INSERT INTO " + tableName +
                                " (type, economy, currency, amount, senderResult, receiverResult, sender, receiver, date, note)" +
                                " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?);",
                        TransactionType.TRANSFER.toString(), economy, currencyName, amount, senderResult, receiverResult, sender_, receiver_, note);
            }
        }.runTaskAsynchronously(plugin);
    }
    
    /**
     * @apiNote This callback is not thread safe. (Asynchronous)
     */
    public void getRecords(String user, Currency currency, int maxCount, Callback<ResultSet, SQLException> callback) {
        String economy = currency.getEconomy().getPluginName();
        String currencyName = currency.getName();
        StringBuilder builder = new StringBuilder("SELECT * FROM " + tableName + " WHERE (sender = ? OR receiver = ?) AND economy = ? ");
        new BukkitRunnable() {
            @Override
            public void run() {
                if (currencyName == null) {
                    builder.append("AND currency IS NULL LIMIT ").append(maxCount).append(";");
                    executeQuery(callback, builder.toString(), user, user, economy);
                } else {
                    builder.append("AND currency = ? LIMIT ").append(maxCount).append(";");
                    executeQuery(callback, builder.toString(), user, user, economy, currencyName);
                }
            }
        }.runTaskAsynchronously(plugin);
    }
}