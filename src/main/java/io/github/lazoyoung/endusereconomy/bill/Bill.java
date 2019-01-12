package io.github.lazoyoung.endusereconomy.bill;

import io.github.lazoyoung.endusereconomy.economy.Currency;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public interface Bill {
    
    Currency getCurrency();
    int getUnit();
    UUID getUniqueId();
    String getIssueDate();
    String getOrigin();
    ItemStack getItem();
    
}