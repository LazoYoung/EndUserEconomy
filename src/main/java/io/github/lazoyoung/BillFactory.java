package io.github.lazoyoung;

import io.github.lazoyoung.economy.Currency;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

public class BillFactory {

    private Currency currency;
    private FileConfiguration config;
    
    public BillFactory(Currency currency) {
        this.currency = currency;
    }
    
    
    
}
