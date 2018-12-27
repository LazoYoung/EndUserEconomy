package io.github.lazoyoung;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class Database {
    
    private static HikariDataSource dataSource;
    private static Map<DataType, String> table;
    
    static void init(FileConfiguration fileConfig) throws SQLException {
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
            DataType type = DataType.valueOf(key.toUpperCase());
            table.put(type, fileConfig.getString("database.mysql.tables." + key).toLowerCase());
        }
        initTables();
    }
    
    static void shutdown() {
        dataSource.close();
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
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + getTableName(DataType.BILL) + " (" +
                    "id binary(16) PRIMARY KEY, " +
                    "economy VARCHAR(30) NOT NULL COMMENT 'Identical to enums in Economy.java.', " +
                    "currency VARCHAR(15) COMMENT 'This is null for those single-currency economies.', " +
                    "unit INT NOT NULL, " +
                    "birth TIMESTAMP, " +
                    "origin VARCHAR(30) COMMENT 'Can be a player or the server.'" +
                    ");");
            con.createStatement().execute("SET NAMES 'utf8';");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
}