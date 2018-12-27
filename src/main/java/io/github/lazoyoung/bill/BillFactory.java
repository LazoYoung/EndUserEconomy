package io.github.lazoyoung.bill;

import io.github.lazoyoung.DataType;
import io.github.lazoyoung.Database;
import io.github.lazoyoung.economy.Currency;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.sql.*;
import java.util.UUID;
import java.util.logging.Logger;

public class BillFactory implements Bill {

    private Currency currency;
    private int unit;
    private UUID id;
    
    public BillFactory(Currency currency, int unit) {
        this.currency = currency;
        this.unit = unit;
    }
    
    public Bill printNew(String origin) throws SQLException {
        
        Logger log = Bukkit.getLogger();
        Connection con = Database.getSource().getConnection();
        con.createStatement().execute("SET @uuid = UUID()");
        String table = Database.getTableName(DataType.BILL);
        String economy = this.currency.getEconomy().toString();
        String currency = this.currency.getName();
        PreparedStatement insert = con.prepareStatement("INSERT INTO " + table + " (id, economy, currency, unit, birth, origin)" +
                " VALUES (UUID_TO_BIN(@uuid, 1), ?, ?, ?, CURRENT_TIMESTAMP, ?);");
        insert.setString(1, economy);
        insert.setString(2, currency);
        insert.setInt(3, unit);
        insert.setString(4, origin);
        
        if (insert.executeUpdate() > 0) {
            ResultSet select = con.createStatement().executeQuery("SELECT @uuid;");
            if (select.next()) {
                id = UUID.fromString(select.getString(1));
                log.info("New bill UUID: " + id.toString()); // TODO DEBUG
                return this;
            }
            throw new SQLException("Unable to select the generated ID.");
        }
        throw new SQLException("Unable to insert a row.");
    }
    
    public Bill findBill(int id) throws SQLException {
        // TODO implement this method
        return null;
    }
    
    @Override
    public Currency getCurrency() {
        return currency;
    }
    
    @Override
    public int getUnit() {
        return unit;
    }
    
    @Override
    public UUID getUniqueId() {
        return id;
    }
    
    @Override
    public ItemStack getItemStack() {
        // TODO implement this method
        return null;
    }
    
}