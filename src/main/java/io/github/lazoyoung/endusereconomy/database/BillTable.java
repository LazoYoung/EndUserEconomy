package io.github.lazoyoung.endusereconomy.database;

import io.github.lazoyoung.database_util.Callback;
import io.github.lazoyoung.database_util.Connector;
import io.github.lazoyoung.database_util.Table;
import io.github.lazoyoung.endusereconomy.Config;
import io.github.lazoyoung.endusereconomy.Main;
import io.github.lazoyoung.endusereconomy.bill.Bill;
import io.github.lazoyoung.endusereconomy.bill.BillFactory;
import io.github.lazoyoung.endusereconomy.economy.Currency;
import io.github.lazoyoung.endusereconomy.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;

public class BillTable extends Table {
    
    private Plugin plugin;
    
    public BillTable(Connector connector) {
        super(connector, Config.BILL.get().getString("records.table-name"));
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
                        "id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, " +
                        "economy VARCHAR(30) NOT NULL COMMENT 'Identical to enums in Economy.java.', " +
                        "currency VARCHAR(15) COMMENT 'This is null for those single-currency economies.', " +
                        "unit INT NOT NULL, " +
                        "date TIMESTAMP NOT NULL COMMENT 'Birth or expiration date.', " +
                        "expired BOOLEAN NOT NULL DEFAULT FALSE, " +
                        "origin VARCHAR(30) COMMENT 'Can be a player or the server.', " +
                        "terminator VARCHAR(30) COMMENT 'Can be a player or the server.', " +
                        "PRIMARY KEY (id)" +
                        ");");
                execute(null, "SET NAMES 'utf8';");
            }
        }.runTaskAsynchronously(plugin);
    }
    
    public void addRecord(final Consumer<Integer> callback, BillFactory factory) {
        final String economy = factory.getCurrency().getEconomy().toString();
        final String currency = factory.getCurrency().getName();
        final int unit = factory.getUnit();
        final String origin = factory.getOrigin();
        final Callback<Integer, SQLException> insertResult = (key, e) -> new BukkitRunnable() {
            @Override
            public void run() {
                if (key != null && e == null) {
                    callback.accept(key);
                    return;
                }
                callback.accept(null);
            }
        }.runTask(plugin);
        new BukkitRunnable() {
            @Override
            public void run() {
                executeInsert(insertResult, "INSERT INTO " + tableName + " (id, economy, currency, unit, date, origin)" +
                        " VALUES (NULL, ?, ?, ?, CURRENT_TIMESTAMP, ?);", economy, currency, unit, origin);
            }
        }.runTaskAsynchronously(plugin);
    }
    
    public void queryRecord(final int id, Consumer<Bill> callback) {
        final Callback<ResultSet, SQLException> selectQuery = (result, thrown) -> {
            try {
                Bill ret = null;
                if (result.next()) {
                    Economy economy = Economy.valueOf(result.getString("economy"));
                    Currency currency = new Currency(economy, result.getString("currency"));
                    int unit = result.getInt("unit");
                    String date = result.getDate("date").toString();
                    boolean expired = result.getBoolean("expired");
                    String origin = result.getString("origin");
                    String terminator = result.getString("terminator");
                    ItemStack itemStack = BillFactory.getItemBase(currency, unit);
                    if (itemStack == null) {
                        Main.log(ChatColor.RED, "Failed to load ItemStack of bill: " + id);
                    }
                    else {
                        ret = new BillFactory(id, currency, unit, date, expired, origin, terminator);
                    }
                }
                final Bill ret_ = ret;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        callback.accept(ret_);
                    }
                }.runTask(Main.getInstance());
            } catch(SQLException e) {
                e.printStackTrace();
            }
        };
        new BukkitRunnable() {
            @Override
            public void run() {
                executeQuery(selectQuery, "SELECT economy, currency, unit, date, expired, origin, terminator FROM " + tableName + " WHERE id = ?;", id);
            }
        }.runTaskAsynchronously(Main.getInstance());
    }
    
    @Deprecated
    public void terminateRecord(int id, @Nullable String director, final Consumer<Boolean> callback) {
        Callback<Integer, SQLException> onUpdate = (count, thrown) -> new BukkitRunnable() {
            @Override
            public void run() {
                if (thrown != null || count == 0) {
                    callback.accept(false);
                    return;
                }
                callback.accept(true);
            }
        }.runTask(plugin);
    
        new BukkitRunnable() {
            @Override
            public void run() {
                if (director != null) {
                    executeUpdate(onUpdate, "UPDATE " + tableName + " SET terminator = ?, date = CURRENT_TIMESTAMP(), expired = TRUE WHERE id = ?;", director, id);
                } else {
                    executeUpdate(onUpdate, "UPDATE " + tableName + " SET date = CURRENT_TIMESTAMP(), expired = TRUE WHERE id = ?;", id);
                }
            }
        }.runTaskAsynchronously(plugin);
    }
    
    /**
     * @apiNote This callback is not thread safe. (Asynchronous)
     */
    public void clearRecords(int interval, Callback<Integer, SQLException> callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                executeUpdate(callback, "DELETE FROM " + tableName + " WHERE expired IS TRUE AND (date + INTERVAL ? DAY) < CURRENT_TIMESTAMP();", interval);
            }
        }.runTaskAsynchronously(plugin);
    }
    
    public void clearRecord(int id, Consumer<Boolean> callback) {
        Callback<Integer, SQLException> onDelete = (count, thrown) -> new BukkitRunnable() {
            @Override
            public void run() {
                if (count > 0) {
                    callback.accept(true);
                } else {
                    callback.accept(false);
                }
            }
        }.runTask(plugin);
        new BukkitRunnable() {
            @Override
            public void run() {
                executeUpdate(onDelete, "DELETE FROM " + tableName + " WHERE id = ?;", id);
            }
        }.runTaskAsynchronously(plugin);
    }
    
}