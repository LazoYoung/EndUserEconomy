package io.github.lazoyoung.endusereconomy.database;

import com.zaxxer.hikari.HikariConfig;
import io.github.lazoyoung.database_util.Table;
import io.github.lazoyoung.endusereconomy.Config;
import org.bukkit.configuration.file.FileConfiguration;

import javax.annotation.Nonnull;
import java.util.EnumMap;
import java.util.Map;

public enum Database {

    BILL;
    
    private static Map<Database, Table> tableMap = new EnumMap<>(Database.class);
    
    public static Table getTable(Database type) {
        return tableMap.get(type);
    }
    
    public void shutdown() {
        if (tableMap.containsKey(this)) {
            tableMap.get(this).getConnector().close();
        }
    }
    
    public static void registerTable(Database type, @Nonnull Table table) {
        tableMap.put(type, table);
    }
    
    public static HikariConfig getHikariConfig() {
        FileConfiguration fileConfig = Config.DATABASE.get();
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(fileConfig.getString("mysql.jdbc-url"));
        config.setUsername(fileConfig.getString("mysql.username"));
        config.setPassword(fileConfig.getString("mysql.password"));
        return config;
    }

}