package io.github.lazoyoung.endusereconomy.economy.handler;

import io.github.lazoyoung.endusereconomy.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public interface EconomyHandler {
    
    Map<Economy, EconomyHandler> registry = new HashMap<>();
    
    @Nullable
    static EconomyHandler getInstance(Economy type) {
        return registry.get(type);
    }
    
    static void register(Economy type, EconomyHandler economyHandler) {
        registry.put(type, economyHandler);
    }
    
    double getBalance(OfflinePlayer player, @Nullable String currency);
    boolean hasAccount(OfflinePlayer player);
    EconomyResponse deposit(OfflinePlayer player, @Nullable String currency, double amount);
    EconomyResponse withdraw(OfflinePlayer player, @Nullable String currency, double amount);
    EconomyResponse setBalance(OfflinePlayer player, @Nullable String currency, double amount);
    boolean isValidCurrency(String currency);

}