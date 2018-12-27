package io.github.lazoyoung;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class Database {
    
    private static HikariDataSource dataSource;
    private static Map<DataType, String> table;
    
    static void init(FileConfiguration fileConfig) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(fileConfig.getString("database.mysql.jdbc-url"));
        config.setUsername(fileConfig.getString("database.mysql.username"));
        config.setPassword(fileConfig.getString("database.mysql.password"));
        config.addDataSourceProperty("prepStmtCacheSize", 250);
        config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        config.addDataSourceProperty("cachePrepStmts", true);
        config.addDataSourceProperty("useServerPrepStmts", true);
        dataSource = new HikariDataSource(config);
        table = new HashMap<>();
        for (String key : fileConfig.getConfigurationSection("database.mysql.tables").getKeys(false)) {
            String[] keyParts = key.split(".");
            DataType type = DataType.valueOf(keyParts[keyParts.length - 1].toUpperCase());
            table.put(type, fileConfig.getString(key));
        }
        initTables();
    }
    
    public static HikariDataSource getSource() {
        return dataSource;
    }
    
    public static String getTableName(DataType type) {
        return table.get(type);
    }
    
    private static void initTables() {
        try {
            Connection con = getSource().getConnection();
            Statement stmt = con.createStatement();
            String bill = getTableName(DataType.BILL);
            stmt.execute("SET NAMES 'utf8';");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + bill + " (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY COMMENT 'The unique ID for each bill.', " +
                    "economy VARCHAR(30) NOT NULL COMMENT 'The economy plugin where this bill belongs to.', " +
                    "currency VARCHAR(15) COMMENT 'Currency name. (Optional)', " +
                    "unit INT NOT NULL" +
                    ");");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
}