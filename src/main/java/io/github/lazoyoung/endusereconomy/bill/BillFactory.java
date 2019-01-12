package io.github.lazoyoung.endusereconomy.bill;

import io.github.lazoyoung.endusereconomy.Config;
import io.github.lazoyoung.endusereconomy.database.BillTable;
import io.github.lazoyoung.endusereconomy.database.Database;
import io.github.lazoyoung.endusereconomy.economy.Currency;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class BillFactory implements Bill {

    private Currency currency;
    private int unit;
    private UUID uniqueId; // TODO convert to integer id
    private String date;
    private String origin;
    
    public BillFactory(UUID uniqueId, Currency currency, int unit, String date, String origin) {
        this.uniqueId = uniqueId;
        this.currency = currency;
        this.unit = unit;
        this.date = date;
        this.origin = origin;
    }
    
    /**
     * @param callback Callback gets called once the bill is constructed.
     *                 Beware it may supply null argument due to a failure on database transaction.
     */
    public static void printNew(Currency currency, int unit, String origin, Consumer<Bill> callback) {
        BillFactory factory = new BillFactory(null, currency, unit, null, origin);
        BillTable table = (BillTable) Database.getTable(Database.BILL);
        Consumer<UUID> addResult = (uuid -> {
            if (uuid == null) {
                callback.accept(null);
                return;
            }
            factory.uniqueId = uuid;
            callback.accept(factory);
        });
        
        table.addRecord(addResult, factory);
    }
    
    public static void getBill(UUID uniqueId, Consumer<Bill> callback) {
        BillTable table = (BillTable) Database.getTable(Database.BILL);
        table.queryRecord(uniqueId, callback);
    }
    
    public static void defineItemBase(Currency currency, int unit, ItemStack itemStack, String[] alias) throws IOException {
        FileConfiguration config = Config.BILL.get();
        String path = currency.toString() + "." + unit;
        
        if (alias != null) {
            ItemMeta meta = itemStack.getItemMeta();
            StringBuilder displayName = new StringBuilder(meta.getDisplayName());
            for (String str : alias) {
                displayName.append(str).append(" ");
            }
            meta.setDisplayName(displayName.substring(0, displayName.length() - 1));
            itemStack.setItemMeta(meta);
        }
        
        config.set(path, itemStack);
        Config.BILL.save(config);
    }
    
    public static ItemStack getItemBase(Currency currency, int unit) {
        return Config.BILL.get().getItemStack(currency.toString() + "." + unit);
    }
    
    @Override
    public ItemStack getItem() {
        ItemStack itemStack = getItemBase(currency, unit).clone();
        if (itemStack != null) {
            ItemMeta meta = itemStack.getItemMeta();
            List<String> lore = new ArrayList<>();
            if (meta.hasLore()) {
                lore = meta.getLore();
            }
            lore.add(uniqueId.toString());
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
            itemStack.setAmount(1);
        }
        return itemStack;
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
    public String getIssueDate() {
        return date;
    }
    
    @Override
    public String getOrigin() {
        return origin;
    }
    
}