package io.github.lazoyoung;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

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
    
    @Override
    public EconomyResponse setBalance(OfflinePlayer player, @Nullable String currency, double amount) {
        if (hasAccount(player)) {
            double bal = api.getBalance(player);
            double dif = amount - bal;
            if (dif == 0) {
                return new EconomyResponse(0, bal, EconomyResponse.ResponseType.SUCCESS, null);
            }
            if (dif > 0) {
                return api.depositPlayer(player, dif);
            }
            return api.withdrawPlayer(player, -dif);
        }
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Account was not found.");
    }
    
    @Override
    public boolean isValidCurrency(String currency) {
        return false;
    }
}
