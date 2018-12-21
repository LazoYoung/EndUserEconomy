package io.github.lazoyoung;

import me.xanium.gemseconomy.api.GemsEconomyAPI;
import org.bukkit.OfflinePlayer;

import javax.annotation.Nullable;

public class GemsEconomy implements Economy {
    
    static {
        EconomyType.providers.put(GemsEconomy.class, new GemsEconomy());
    }
    
    private GemsEconomyAPI api = new GemsEconomyAPI();
    
    @Override
    public double getBalance(OfflinePlayer player, @Nullable String currency) {
        return api.getBalance(player.getUniqueId(), api.getCurrency(currency));
    }
    
    @Override
    public void deposit(OfflinePlayer player, @Nullable String currency, double amount) {
        api.deposit(player.getUniqueId(), amount, api.getCurrency(currency));
    }
    
    @Override
    public void withdraw(OfflinePlayer player, @Nullable String currency, double amount) {
        api.withdraw(player.getUniqueId(), amount, api.getCurrency(currency));
    }
}
