package io.github.lazoyoung;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import javax.annotation.Nullable;

public class VaultEconomyHandler extends AbstractEconomyHandler {
    
    private net.milkbowl.vault.economy.Economy api;
    
    private VaultEconomyHandler() {
        api = Bukkit.getServicesManager().load(net.milkbowl.vault.economy.Economy.class);
    }
    
    @Override
    public double getBalance(OfflinePlayer player, @Nullable String currency) {
        return api.getBalance(player);
    }
    
    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return api.hasAccount(player);
    }
    
    @Override
    public EconomyResponse deposit(OfflinePlayer player, @Nullable String currency, double amount) {
        return api.depositPlayer(player, amount);
    }
    
    @Override
    public EconomyResponse withdraw(OfflinePlayer player, @Nullable String currency, double amount) {
        return api.withdrawPlayer(player, amount);
    }
}
