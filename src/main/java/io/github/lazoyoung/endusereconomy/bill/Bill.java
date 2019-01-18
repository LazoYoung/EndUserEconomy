package io.github.lazoyoung.endusereconomy.bill;

import io.github.lazoyoung.endusereconomy.economy.Currency;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public interface Bill {
    
    Currency getCurrency();
    int getUnit();
    int getId();
    void discard(@Nullable String director, Consumer<Boolean> callback);
    boolean isExpired();
    String getTerminator();
    String getDate();
    String getOrigin();
    ItemStack getItem();
    
}