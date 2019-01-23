package io.github.lazoyoung.endusereconomy.economy.handler;

import io.github.lazoyoung.endusereconomy.database.BankTable;
import io.github.lazoyoung.endusereconomy.database.Database;
import io.github.lazoyoung.endusereconomy.economy.Currency;
import io.github.lazoyoung.endusereconomy.economy.Economy;
import net.ess3.api.events.UserBalanceUpdateEvent;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;

import javax.annotation.Nullable;

public class VaultEconomyHandler extends AbstractEconomyHandler implements Listener {
    
    private net.milkbowl.vault.economy.Economy api;
    
    public VaultEconomyHandler() throws InstantiationException {
        RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> provider = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (provider == null) {
            throw new InstantiationException("Failed to load Vault API. Make sure you've installed the economy plugin linked to Vault.");
        }
        api = provider.getProvider();
    }
    
    @EventHandler
    public void onBalanceUpdate(UserBalanceUpdateEvent event) {
        int newBal = 0, oldBal = 0;
        try {
            oldBal = event.getOldBalance().toBigInteger().intValue();
            newBal = event.getNewBalance().toBigInteger().intValueExact();
        } catch (ArithmeticException e) {
            e.printStackTrace();
        }
        Player player = event.getPlayer();
        BankTable table = (BankTable) Database.getTable(Database.BANK_TRANSACTION);
        int amount = Math.abs(newBal - oldBal);
        
        if (newBal > oldBal) {
            table.recordDeposit(new Currency(Economy.VAULT), amount, newBal, player.getName(), null);
        } else if (newBal < oldBal) {
            table.recordWithdraw(new Currency(Economy.VAULT), amount, newBal, player.getName(), null);
        }
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
