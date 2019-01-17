package io.github.lazoyoung.endusereconomy.bill;

import io.github.lazoyoung.endusereconomy.economy.Currency;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public interface Bill {
    
    Currency getCurrency();
    int getUnit();
    int getId();
    void discard(Consumer<Boolean> callback);
    String getIssueDate();
    String getOrigin();
    ItemStack getItem();
    
}