package io.github.lazoyoung;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class Database {
    
    private static Connector connector;
    private static Map<DataType, String> table;
    
    static void init() throws SQLException {
        HikariConfig config = new HikariConfig();
        FileConfiguration fileConfig = Config.DATABASE.get();
        config.setJdbcUrl(fileConfig.getString("mysql.jdbc-url"));
        config.setUsername(fileConfig.getString("mysql.username"));
        config.setPassword(fileConfig.getString("mysql.password"));
        config.addDataSourceProperty("prepStmtCacheSize", 250);
        config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        config.addDataSourceProperty("cachePrepStmts", true);
        config.addDataSourceProperty("useServerPrepStmts", true);
        connector = new Connector(Bukkit.getLogger(), config);
        table = new HashMap<>();
        for (String key : fileConfig.getConfigurationSection("mysql.tables").getKeys(false)) {
            DataType type = DataType.valueOf(key.toUpperCase());
            table.put(type, fileConfig.getString("mysql.tables." + key).toLowerCase());
        }
        initTables();
    }
    
    static void shutdown() {
        connector.close();
    }
    
    public static Connection getConnection() throws SQLException {
        return connector.getConnection();
    }
    
    
}