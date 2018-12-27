package io.github.lazoyoung.bill;

import io.github.lazoyoung.DataType;
import io.github.lazoyoung.Database;
import io.github.lazoyoung.economy.Currency;

import java.sql.*;

public class BillFactory implements Bill {

    private Currency currency;
    private int unit;
    private int id;
    
    public BillFactory(Currency currency, int unit) {
        this.currency = currency;
        this.unit = unit;
    }
    
    public Bill printNew(String origin) throws SQLException {
        
        // TODO consider replacing id to UUID, due to reliability between YAML and MySQL record synchronization
        Connection con = Database.getSource().getConnection();
        PreparedStatement insert = con.prepareStatement("INSERT INTO ? (id, economy, currency, unit, birth, origin)" +
                " VALUES (NULL, ?, ?, ?, CURRENT_TIMESTAMP, ?);", Statement.RETURN_GENERATED_KEYS);
        String table = Database.getTableName(DataType.BILL);
        String economy = this.currency.getEconomy().toString();
        String currency = this.currency.getName();
        insert.setString(1, table);
        insert.setString(2, economy);
        insert.setString(3, currency);
        insert.setInt(4, unit);
        insert.setString(5, origin);
        
        if (insert.executeUpdate() > 0) {
            ResultSet resultSet = insert.getGeneratedKeys();
            if (resultSet.next()) {
                id = resultSet.getInt(1);
                return this;
            }
            throw new SQLException("Unable to select the generated ID.");
        }
        throw new SQLException("Unable to insert a row.");
    }
    
    public Bill getFromId(int id) throws SQLException {
        // TODO implement this method
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
    public int getId() {
        return id;
    }
    
}