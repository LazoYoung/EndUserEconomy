package io.github.lazoyoung.bill;

import io.github.lazoyoung.DataType;
import io.github.lazoyoung.Database;
import io.github.lazoyoung.economy.Currency;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.*;

public class BillFactory {

    private Currency currency;
    private int unit;
    private FileConfiguration config;
    
    public BillFactory(Currency currency, int unit) {
        this.currency = currency;
        this.unit = unit;
    }
    
    public Bill printNew() throws SQLException {
        Connection con = Database.getSource().getConnection();
        PreparedStatement insert = con.prepareStatement("INSERT INTO ? (id, economy, currency, unit)" +
                " VALUES (NULL, ?, ?, ?);", Statement.RETURN_GENERATED_KEYS);
        String table = Database.getTableName(DataType.BILL);
        String economy = this.currency.getEconomy().toString();
        String currency = this.currency.getName();
        insert.setString(1, table);
        insert.setString(2, economy);
        insert.setString(3, currency);
        insert.setInt(4, unit);
        
        if (insert.executeUpdate() > 0) {
            ResultSet resultSet = insert.getGeneratedKeys();
            if (resultSet.next()) {
                int id = resultSet.getInt(1);
                return new Bill(this.currency, this.unit, id);
            }
            throw new SQLException("Unable to select the generated ID.");
        }
        throw new SQLException("Unable to insert a row.");
    }
    
}