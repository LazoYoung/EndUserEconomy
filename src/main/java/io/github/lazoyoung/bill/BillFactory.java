package io.github.lazoyoung.bill;

import io.github.lazoyoung.database.BillTable;
import io.github.lazoyoung.database.Database;
import io.github.lazoyoung.economy.Currency;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class BillFactory implements Bill {

    private Currency currency;
    private int unit;
    private UUID uniqueId;
    private ItemStack itemStack;
    
    public static Bill loadBill(UUID id) {
        // TODO implement this method
        return null;
    }
    
    public BillFactory(Currency currency, int unit, ItemStack itemStack) {
        this.currency = currency;
        this.unit = unit;
        this.itemStack = itemStack;
    }
    
    BillFactory(Currency currency, int unit, UUID uniqueId) {
        this.currency = currency;
        this.unit = unit;
        this.uniqueId = uniqueId;
    }
    
    public Bill printNew(String origin) {
        BillTable table = (BillTable) Database.getTable(Database.BILL);
        table.addRecord(this, origin);
    }
    
    @Override
    public Currency getCurrency() {
        return currency;
    }
    
    @Override
    public int getUnit() {
        return unit;
    }
    
    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }
    
    @Override
    public ItemStack getItemStack() {
        return itemStack;
    }
    
}