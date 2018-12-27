package io.github.lazoyoung.bill;

import io.github.lazoyoung.economy.Currency;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public interface Bill {
    
    Currency getCurrency();
    int getUnit();
    UUID getUniqueId();
    ItemStack getItemStack();
    
}