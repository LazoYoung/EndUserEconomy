package io.github.lazoyoung;

import com.earth2me.essentials.api.UserDoesNotExistException;
import org.bukkit.OfflinePlayer;

import javax.annotation.Nullable;

public class EssentialsEconomy implements Economy {
    
    static {
        EconomyType.providers.put(EssentialsEconomy.class, new EssentialsEconomy());
    }
    
    @Override
    public double getBalance(OfflinePlayer player, @Nullable String currency) {
        try {
            return net.ess3.api.Economy.getMoneyExact(player.getName());
        } catch (UserDoesNotExistException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void deposit(OfflinePlayer player, @Nullable String currency, double amount) {
    
    }
    
    @Override
    public void withdraw(OfflinePlayer player, @Nullable String currency, double amount) {
    
    }
}
