package io.github.lazoyoung;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class Database {
    
    private static HikariDataSource dataSource;
    private static Map<String, String> table;
    
    static void init(FileConfiguration fileConfig) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(fileConfig.getString("database.mysql.jdbc-url"));
        config.addDataSourceProperty("prepStmtCacheSize", 250);
        config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        config.addDataSourceProperty("cachePrepStmts", true);
        config.addDataSourceProperty("useServerPrepStmts", true);
        dataSource = new HikariDataSource(config);
        table = new HashMap<>();
        for (String key : fileConfig.getConfigurationSection("database.mysql.tables").getKeys(false)) {
            String[] keyParts = key.split(".");
            table.put(keyParts[keyParts.length - 1].toUpperCase(), fileConfig.getString(key));
        }
    }
    
    public static HikariDataSource getDataSource() {
        return dataSource;
    }
    
    public String getTableName(DataType type) {
        return table.get(type.toString());
    }
    
}

enum DataType {
    BILL, BILL_REGISTRY
}
