package io.github.lazoyoung.endusereconomy.economy.handler;

import io.github.lazoyoung.endusereconomy.database.BankTable;
import io.github.lazoyoung.endusereconomy.database.Database;
import io.github.lazoyoung.endusereconomy.economy.Currency;
import io.github.lazoyoung.endusereconomy.economy.Economy;
import me.xanium.gemseconomy.api.GemsEconomyAPI;
import me.xanium.gemseconomy.economy.AccountManager;
import me.xanium.gemseconomy.event.GemsPayEvent;
import me.xanium.gemseconomy.event.GemsTransactionEvent;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import javax.annotation.Nullable;
import java.util.UUID;

public class GemsEconomyHandler extends AbstractEconomyHandler implements Listener {
    
    private GemsEconomyAPI api = new GemsEconomyAPI();
    
    @EventHandler
    public void onPay(GemsPayEvent event) {
        if (!event.isCancelled()) {
            me.xanium.gemseconomy.economy.Currency c = event.getCurrency();
            double amount = event.getAmount();
            String sender = event.getPayer().getNickname();
            String receiver = event.getReceived().getNickname();
            int senderResult = (int) Math.abs(event.getPayer().getBalance(c) - amount);
            int receiverResult = (int) Math.abs(event.getReceived().getBalance(c) + amount);
            BankTable table = (BankTable) Database.getTable(Database.BANK_TRANSACTION);
            table.recordTransfer(new Currency(Economy.GEMS_ECONOMY, c.getSingular()), (int) amount, senderResult, receiverResult, sender, receiver, null);
        }
    }
    
    @EventHandler
    public void onTransfer(GemsTransactionEvent event) {
        if (!event.isCancelled()) {
            me.xanium.gemseconomy.economy.Currency c = event.getCurrency();
            double amount = event.getAmount();
            String player = event.getAccount().getNickname();
            
            switch (event.getType()) {
                case DEPOSIT:
                
            }
        }
    }
    
    @Override
    public double getBalance(OfflinePlayer player, @Nullable String currency) {
        if (hasAccount(player)) {
            return api.getBalance(player.getUniqueId(), api.getCurrency(currency));
        }
        return 0;
    }
    
    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return AccountManager.getAccount(player.getUniqueId()) != null;
    }
    
    @Override
    public EconomyResponse deposit(OfflinePlayer player, @Nullable String currency, double amount) {
        UUID uuid = player.getUniqueId();
        api.deposit(uuid, amount, api.getCurrency(currency));
        return new EconomyResponse(amount, api.getBalance(uuid), EconomyResponse.ResponseType.SUCCESS, null);
    }
    
    @Override
    public EconomyResponse withdraw(OfflinePlayer player, @Nullable String currency, double amount) {
        UUID uuid = player.getUniqueId();
        if (api.getBalance(uuid) >= amount) {
            api.withdraw(uuid, amount, api.getCurrency(currency));
            return new EconomyResponse(amount, api.getBalance(uuid), EconomyResponse.ResponseType.SUCCESS, null);
        }
        return new EconomyResponse(amount, api.getBalance(uuid), EconomyResponse.ResponseType.FAILURE, null);
    }
    
    @Override
    public EconomyResponse transfer(OfflinePlayer sender, OfflinePlayer recipient, @Nullable String currency, double amount) {
        return new EconomyResponse(amount, api.getBalance(sender.getUniqueId(), api.getCurrency(currency)), EconomyResponse.ResponseType.FAILURE, "Not implemented yet.");
    }
    
    @Override
    public EconomyResponse setBalance(OfflinePlayer player, @Nullable String currency, double amount) {
        if (hasAccount(player)) {
            double bal = getBalance(player, currency);
            double dif = amount - bal;
            if (dif == 0) {
                return new EconomyResponse(0, bal, EconomyResponse.ResponseType.SUCCESS, null);
            }
            if (dif > 0) {
                return deposit(player, currency, dif);
            }
            return withdraw(player, currency, -dif);
        }
        return new EconomyResponse(0D, amount, EconomyResponse.ResponseType.FAILURE, "Account was not found.");
    }
    
    @Override
    public boolean isValidCurrency(String currency) {
        return api.getCurrency(currency) != null;
    }
}
