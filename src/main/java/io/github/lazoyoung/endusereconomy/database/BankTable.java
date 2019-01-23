package io.github.lazoyoung.endusereconomy.database;

import io.github.lazoyoung.database_util.Callback;
import io.github.lazoyoung.database_util.Connector;
import io.github.lazoyoung.database_util.Table;
import io.github.lazoyoung.endusereconomy.Config;
import io.github.lazoyoung.endusereconomy.Main;
import io.github.lazoyoung.endusereconomy.bank.transaction.TransactionType;
import io.github.lazoyoung.endusereconomy.economy.Currency;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BankTable extends Table {
    
    public BankTable(Connector connector) {
        super(connector, Config.BANK.get().getString("transaction.table-name"));
    }
    
    @Override
    protected void init() {
        Callback<Integer, SQLException> createResult = (result, e) -> {
            if (e != null) {
                Main.terminate(Main.getInstance(), "Failed to create table " + tableName + ". SQLException code: " + e.getSQLState());
            }
        };
        executeUpdate(createResult, "CREATE TABLE IF NOT EXISTS " + tableName +
                " (" +
                "type VARCHAR(16) NOT NULL COMMENT 'deposit, withdraw, transfer', " +
                "economy VARCHAR(30) NOT NULL COMMENT 'Identical to enums in Economy.java.', " +
                "currency VARCHAR(15) COMMENT 'This is null for those single-currency economies.', " +
                "amount MEDIUMINT UNSIGNED NOT NULL, " +
                "result INT NOT NULL, " +
                "sender VARCHAR(30), " +
                "receiver VARCHAR(30), " +
                "date TIMESTAMP NOT NULL, " +
                "note VARCHAR(30)" +
                ");");
        execute(null, "SET NAMES 'utf8';");
    }
    
    public void recordDeposit(Currency currency, int amount, int result, String user, String note) {
        String economy = currency.getEconomy().getPluginName();
        String currencyName = currency.getName();
        Callback<Integer, SQLException> insertResult = (key, thrown) -> {
            if (thrown == null) {
                Main.log(user + " deposit " + amount + " to his account in economy: " + currency.toString());
            }
        };
        executeInsert(insertResult, "INSERT INTO " + tableName +
                        " (type, economy, currency, amount, result, sender, receiver, date, note)" +
                        " VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?);",
                TransactionType.DEPOSIT.toString(), economy, currencyName, amount, result, user, user, note);
        
    }
    
    public void recordWithdraw(Currency currency, int amount, int result, String user, String note) {
        String economy = currency.getEconomy().getPluginName();
        String currencyName = currency.getName();
        Callback<Integer, SQLException> insertResult = (key, thrown) -> {
            if (thrown == null) {
                Main.log(user + " withdrawn " + amount + " from his account in economy: " + currency.toString());
            }
        };
        executeInsert(insertResult, "INSERT INTO " + tableName +
                        " (type, economy, currency, amount, result, sender, receiver, date, note)" +
                        " VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?);",
                TransactionType.WITHDRAW.toString(), economy, currencyName, amount, result, user, user, note);
    }
    
    public void recordTransfer(Currency currency, int amount, int result, String user, String target, String note) {
        String economy = currency.getEconomy().getPluginName();
        String currencyName = currency.getName();
        Callback<Integer, SQLException> insertResult = (key, thrown) -> {
            if (thrown == null) {
                Main.log(user + " transferred " + amount + " to " + target + " in economy: " + currency.toString());
            }
        };
        executeInsert(insertResult, "INSERT INTO " + tableName +
                        " (type, economy, currency, amount, result, sender, receiver, date, note)" +
                        " VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?);",
                TransactionType.TRANSFER.toString(), economy, currencyName, amount, result, user, target, note);
    }
    
    public void getRecords(String user, Currency currency, int maxCount, Callback<ResultSet, SQLException> callback) {
        String economy = currency.getEconomy().getPluginName();
        String currencyName = currency.getName();
        StringBuilder builder = new StringBuilder("SELECT * FROM " + tableName + " WHERE (sender = ? OR receiver = ?) AND economy = ? ");
        if (currencyName == null) {
            builder.append("AND currency IS NULL LIMIT ").append(maxCount).append(";");
            executeQuery(callback, builder.toString(), user, user, economy);
        } else {
            builder.append("AND currency = ? LIMIT ").append(maxCount).append(";");
            executeQuery(callback, builder.toString(), user, user, economy, currencyName);
        }
    }
}