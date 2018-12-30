package io.github.lazoyoung;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;

public enum Config {
    
    DATABASE("database.yml", 1),
    BILL("bill.yml", 1);
    
    private FileConfiguration fileConfig;
    private String fileName;
    private int version;
    
    Config(String fileName, int version) {
        this.fileConfig = null;
        this.fileName = fileName;
        this.version = version;
    }
    
    public FileConfiguration get() {
        if (fileConfig == null) {
            loadFileConfig();
        }
        
        return fileConfig;
    }
    
    public void loadFileConfig() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(Main.pluginName);
        File dataFolder = plugin.getDataFolder();
        File file = new File(dataFolder, fileName);
        FileConfiguration fileConfig1;
        
        if (!file.isFile()) {
            plugin.saveResource(fileName, true);
        }
    
        fileConfig1 = YamlConfiguration.loadConfiguration(file);
        
        if (!(fileConfig1.isInt("version")) || fileConfig1.getInt("version") != version) {
            plugin.saveResource(fileName, true);
            fileConfig1 = YamlConfiguration.loadConfiguration(file);
        }
        
        this.fileConfig = fileConfig1;
    }
    
}
