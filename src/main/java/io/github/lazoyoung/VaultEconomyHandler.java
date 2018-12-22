package io.github.lazoyoung;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;

import javax.annotation.Nullable;

public class VaultEconomyHandler extends AbstractEconomyHandler {
    
    private net.milkbowl.vault.economy.Economy api;
    
    public VaultEconomyHandler() throws InstantiationException {
        RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> provider = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (provider == null) {
            throw new InstantiationException("Failed to load Vault API. Make sure you've installed the economy plugin linked to Vault.");
        }
        api = provider.getProvider();
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
