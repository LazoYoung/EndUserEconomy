package io.github.lazoyoung.database;

import com.zaxxer.hikari.HikariConfig;
import io.github.lazoyoung.Config;
import io.github.lazoyoung.Connector;
import io.github.lazoyoung.Table;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;

public enum Database {

    BILL;
    
    private static HashMap<Database, Table> tableMap = new HashMap<>();

    public static Table getTable(Database type) {
        return tableMap.get(type);
    }

    static void initTables() {
        HikariConfig config = initConfig();
        BillTable billTable = new BillTable(new Connector(Bukkit.getLogger(), config));
        tableMap.put(BILL, billTable);
    }

    static void shutdown() {
        tableMap.values().forEach(table -> table.getConnector().close());
    }

    private static HikariConfig initConfig() {
        FileConfiguration fileConfig = Config.DATABASE.get();
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(fileConfig.getString("mysql.jdbc-url"));
        config.setUsername(fileConfig.getString("mysql.username"));
        config.setPassword(fileConfig.getString("mysql.password"));
        config.setSchema(fileConfig.getString("mysql.schema"));
        return config;
    }

}