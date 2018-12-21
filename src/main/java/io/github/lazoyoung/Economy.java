package io.github.lazoyoung;

import org.bukkit.OfflinePlayer;

import javax.annotation.Nullable;

public interface Economy {
    
    double getBalance(OfflinePlayer player, @Nullable String currency);
    void deposit(OfflinePlayer player, @Nullable String currency, double amount);
    void withdraw(OfflinePlayer player, @Nullable String currency, double amount);

}
