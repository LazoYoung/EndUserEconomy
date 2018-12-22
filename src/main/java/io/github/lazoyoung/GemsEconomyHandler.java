package io.github.lazoyoung;

import me.xanium.gemseconomy.api.GemsEconomyAPI;
import me.xanium.gemseconomy.economy.AccountManager;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;

import javax.annotation.Nullable;
import java.util.UUID;

public class GemsEconomyHandler extends AbstractEconomyHandler {
    
    private GemsEconomyAPI api = new GemsEconomyAPI();
    
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
}
