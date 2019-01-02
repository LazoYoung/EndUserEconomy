package io.github.lazoyoung.endusereconomy.database;

import io.github.lazoyoung.database_util.Callback;
import io.github.lazoyoung.database_util.Connector;
import io.github.lazoyoung.database_util.Table;
import io.github.lazoyoung.endusereconomy.Config;
import io.github.lazoyoung.endusereconomy.Main;
import io.github.lazoyoung.endusereconomy.bill.BillFactory;
import io.github.lazoyoung.endusereconomy.economy.Currency;
import io.github.lazoyoung.endusereconomy.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
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
        Callback<Object, SQLException> createResult = ((result, e) -> {
            if (e != null) {
                Main.terminate(Main.getInstance(), "Failed to init table " + tableName + ". SQLException code: " + e.getSQLState());
            }
        });
        executeUpdate(createResult, "CREATE TABLE IF NOT EXISTS " + tableName +
                " (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "uuid binary(16) NOT NULL, " +
                "economy VARCHAR(30) NOT NULL COMMENT 'Identical to enums in Economy.java.', " +
                "currency VARCHAR(15) COMMENT 'This is null for those single-currency economies.', " +
                "unit INT NOT NULL, " +
                "birth TIMESTAMP, " +
                "origin VARCHAR(30) COMMENT 'Can be a player or the server.'" +
                ");");
        execute(null, "SET NAMES 'utf8';");
    }
    
    public void addRecord(final Consumer<UUID> callback, BillFactory factory) {
        final String economy = factory.getCurrency().getEconomy().toString();
        final String currency = factory.getCurrency().getName();
        final int unit = factory.getUnit();
        final String origin = factory.getOrigin();
        final Callback<UUID, SQLException> convertResult = ((uuid, thrown) -> {
            if (uuid != null) {
                callback.accept(uuid);
                return;
            }
            callback.accept(null);
        });
        final Callback<ResultSet, SQLException> selectResult1 = ((result, thrown) -> {
            try {
                if (result != null) {
                    if (result.next()) {
                        convertBinaryToUUID(convertResult, result.getBinaryStream(1));
                        return;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            callback.accept(null);
        });
        final Callback<Object, SQLException> insertResult = ((key, e) -> {
            if (key != null && e == null) {
                executeQuery(selectResult1, "SELECT uuid FROM " + tableName + " WHERE id = ?;", (int) key);
                return;
            }
            callback.accept(null);
        });
        Callback<ResultSet, SQLException> selectResult = ((result, thrown) -> {
            if (thrown == null) {
                try {
                    if (result.next()) {
                        executeUpdate(insertResult, "INSERT INTO " + tableName + " (id, uuid, economy, currency, unit, birth, origin)" +
                                " VALUES (null, UUID_TO_BIN(?, 1), ?, ?, ?, CURRENT_TIMESTAMP, ?);", result.getString(1), economy, currency, unit, origin);
                        return;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    callback.accept(null);
                }
            }
            callback.accept(null);
        });
        
        executeQuery(selectResult, "SELECT UUID();");
    }
    
    public void queryRecord(final UUID uuid, Consumer<BillFactory> callback) {
        final Callback<ResultSet, SQLException> selectQuery = ((result, thrown) -> {
            try {
                if (result.next()) {
                    Economy economy = Economy.valueOf(result.getString("economy"));
                    Currency currency = new Currency(economy, result.getString("currency"));
                    int unit = result.getInt("unit");
                    String date = result.getDate("birth").toString();
                    String origin = result.getString("origin");
                    ItemStack itemStack = BillFactory.loadItemStack(uuid);
                    if (itemStack == null) {
                        callback.accept(null);
                        Main.log(ChatColor.RED, "Failed to load ItemStack of bill: " + uuid);
                        return;
                    }
                    callback.accept(new BillFactory(uuid, currency, unit, date, origin, itemStack));
                }
            } catch(SQLException e) {
                e.printStackTrace();
            }
        });
        Callback<InputStream, SQLException> convertResult = ((binary, thrown) -> {
            if (binary != null) {
                executeQuery(selectQuery, "SELECT economy, currency, unit, birth, origin FROM " + tableName + " WHERE uuid = ?;", binary);
            }
        });
        convertUUIDToBinary(convertResult, uuid);
    }

}