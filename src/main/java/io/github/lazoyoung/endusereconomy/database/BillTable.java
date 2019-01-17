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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;

public class BillTable extends Table {

    private String tableName;

    private BillTable(Connector connector) {
        super(connector);
    }
    
    public static BillTable initTable(Connector connector) {
        return new BillTable(connector);
    }
    
    @Override
    protected void init() {
        this.tableName = Config.DATABASE.get().getString("mysql.tables.bill");
        Callback<Integer, SQLException> createResult = ((result, e) -> {
            if (e != null) {
                Main.terminate(Main.getInstance(), "Failed to init table " + tableName + ". SQLException code: " + e.getSQLState());
            }
        });
        executeUpdate(createResult, "CREATE TABLE IF NOT EXISTS " + tableName +
                " (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "economy VARCHAR(30) NOT NULL COMMENT 'Identical to enums in Economy.java.', " +
                "currency VARCHAR(15) COMMENT 'This is null for those single-currency economies.', " +
                "unit INT NOT NULL, " +
                "birth TIMESTAMP, " +
                "origin VARCHAR(30) COMMENT 'Can be a player or the server.'" +
                ");");
        execute(null, "SET NAMES 'utf8';");
    }
    
    public void addRecord(final Consumer<Integer> callback, BillFactory factory) {
        final String economy = factory.getCurrency().getEconomy().toString();
        final String currency = factory.getCurrency().getName();
        final int unit = factory.getUnit();
        final String origin = factory.getOrigin();
        final Callback<Integer, SQLException> insertResult = ((key, e) -> {
            if (key != null && e == null) {
                callback.accept(key);
                return;
            }
            callback.accept(null);
        });
        executeInsert(insertResult, "INSERT INTO " + tableName + " (id, economy, currency, unit, birth, origin)" +
                " VALUES (null, ?, ?, ?, CURRENT_TIMESTAMP, ?);", economy, currency, unit, origin);
    }
    
    public void queryRecord(final int id, Consumer<Bill> callback) {
        final Callback<ResultSet, SQLException> selectQuery = ((result, thrown) -> {
            try {
                if (result.next()) {
                    Economy economy = Economy.valueOf(result.getString("economy"));
                    Currency currency = new Currency(economy, result.getString("currency"));
                    int unit = result.getInt("unit");
                    String date = result.getDate("birth").toString();
                    String origin = result.getString("origin");
                    ItemStack itemStack = BillFactory.getItemBase(currency, unit);
                    if (itemStack == null) {
                        callback.accept(null);
                        Main.log(ChatColor.RED, "Failed to load ItemStack of bill: " + id);
                        return;
                    }
                    callback.accept(new BillFactory(id, currency, unit, date, origin));
                }
                else {
                    callback.accept(null);
                }
            } catch(SQLException e) {
                e.printStackTrace();
            }
        });
        executeQuery(selectQuery, "SELECT economy, currency, unit, birth, origin FROM " + tableName + " WHERE id = ?;", id);
    }
    
    public void deleteRecord(int id, final Consumer<Boolean> callback) {
        Callback<Integer, SQLException> onDelete = ((count, thrown) -> {
            if (thrown != null || count == 0) {
                callback.accept(false);
                return;
            }
            callback.accept(true);
        });
        executeUpdate(onDelete, "DELETE FROM " + tableName + " WHERE id=?;", id);
    }

}