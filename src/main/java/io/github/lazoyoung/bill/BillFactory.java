package io.github.lazoyoung.bill;

import io.github.lazoyoung.DataType;
import io.github.lazoyoung.Database;
import io.github.lazoyoung.economy.Currency;
import org.bukkit.inventory.ItemStack;

import java.sql.*;
import java.util.UUID;

public class BillFactory implements Bill {

    private Currency currency;
    private int unit;
    private UUID uniqueId;
    private ItemStack itemStack;
    
    public static Bill loadBill(UUID id) throws SQLException {
        // TODO implement this method
        return null;
    }
    
    public BillFactory(Currency currency, int unit, ItemStack itemStack) {
        this.currency = currency;
        this.unit = unit;
        this.itemStack = itemStack;
    }
    
    BillFactory(Currency currency, int unit, UUID uniqueId) {
        this.currency = currency;
        this.unit = unit;
        this.uniqueId = uniqueId;
    }
    
    public Bill printNew(String origin) throws SQLException {
        Connection con = null;
        PreparedStatement preStmt = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        String table = Database.getTableName(DataType.BILL);
        String economy = this.currency.getEconomy().toString();
        String currency = this.currency.getName();
        
        try {
            con = Database.getConnection();
            con.createStatement().execute("SET @uuid = UUID()");
            preStmt = con.prepareStatement("INSERT INTO " + table + " (id, economy, currency, unit, birth, origin)" +
                    " VALUES (UUID_TO_BIN(@uuid, 1), ?, ?, ?, CURRENT_TIMESTAMP, ?);");
            preStmt.setString(1, economy);
            preStmt.setString(2, currency);
            preStmt.setInt(3, unit);
            preStmt.setString(4, origin);
    
            if (preStmt.executeUpdate() > 0) {
                stmt = con.createStatement();
                resultSet = stmt.executeQuery("SELECT @uuid;");
                if (resultSet.next()) {
                    uniqueId = UUID.fromString(resultSet.getString(1));
                    return this;
                }
                throw new SQLException("Unable to select the generated ID.");
            }
            throw new SQLException("Unable to insert a row.");
        } finally {
            try {
                if (con != null)
                    con.close();
                if (preStmt != null)
                    preStmt.close();
                if (stmt != null)
                    stmt.close();
                if (resultSet != null)
                    resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
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
        return uniqueId;
    }
    
    @Override
    public ItemStack getItemStack() {
        return itemStack;
    }
    
}