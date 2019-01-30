package io.github.lazoyoung.endusereconomy.bill;

import io.github.lazoyoung.endusereconomy.Config;
import io.github.lazoyoung.endusereconomy.database.BillTable;
import io.github.lazoyoung.endusereconomy.database.Database;
import io.github.lazoyoung.endusereconomy.economy.Currency;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class BillFactory implements Bill {

    private Currency currency;
    private int unit;
    private int id;
    private boolean expired;
    private String date;
    private String origin;
    private String terminator;
    
    // Used to store into memory after querying
    public BillFactory(int id, Currency currency, int unit, String date, boolean expired, String origin, String terminator) {
        this.id = id;
        this.currency = currency;
        this.unit = unit;
        this.date = date;
        this.expired = expired;
        this.origin = origin;
        this.terminator = terminator;
    }
    
    // Used to print new one
    private BillFactory(Currency currency, int unit, String origin) {
        this.currency = currency;
        this.unit = unit;
        this.origin = origin;
    }
    
    /**
     * @param callback Callback gets called once the bill is constructed.
     *                 Beware it may supply null argument due to a failure on database transaction.
     */
    public static void printNew(Currency currency, int unit, String origin, Consumer<Bill> callback) {
        BillFactory factory = new BillFactory(currency, unit, origin);
        BillTable table = (BillTable) Database.getTable(Database.BILL_RECORD);
        Consumer<Integer> addResult = (id -> {
            Bill ret = null;
            if (id != null) {
                factory.id = id;
                ret = factory;
            }
            callback.accept(ret);
        });
        table.addRecord(addResult, factory);
    }
    
    public static void getBill(int id, Consumer<Bill> callback) {
        BillTable table = (BillTable) Database.getTable(Database.BILL_RECORD);
        table.queryRecord(id, callback);
    }
    
    public static void defineItemBase(Currency currency, int unit, ItemStack itemStack, String[] alias) throws IOException {
        FileConfiguration config = Config.BILL.get();
        String path = currency.toString() + "." + unit;
        
        if (alias != null) {
            ItemMeta meta = itemStack.getItemMeta();
            StringBuilder displayName = new StringBuilder();
            for (String str : alias) {
                displayName.append(str).append(" ");
            }
            meta.setDisplayName(displayName.substring(0, displayName.length() - 1));
            itemStack.setItemMeta(meta);
            itemStack.setAmount(1);
        }
        
        config.set(path, itemStack);
        Config.BILL.save(config);
    }
    
    public static Set<Integer> getRegisteredUnits(Currency currency) {
        ConfigurationSection section = Config.BILL.get().getConfigurationSection(currency.toString());
        if (section != null) {
            Set<Integer> list = new HashSet<>();
            for (String s : section.getKeys(false)) {
                list.add(Integer.parseInt(s));
            }
            return list;
        }
        return null;
    }
    
    public static ItemStack getItemBase(Currency currency, int unit) {
        return Config.BILL.get().getItemStack(currency.toString() + "." + unit);
    }
    
    @Override
    public ItemStack getItem() {
        ItemStack itemStack = getItemBase(currency, unit);
        if (itemStack != null) {
            itemStack = itemStack.clone();
            ItemMeta meta = itemStack.getItemMeta();
            if (meta != null) {
                List<String> lore = new ArrayList<>();
                if (meta.hasLore()) {
                    lore = meta.getLore();
                }
                lore.add("BILL ID " + id);
                meta.setLore(lore);
                itemStack.setItemMeta(meta);
            }
            itemStack.setAmount(1);
        }
        return itemStack;
    }
    
    public static void getBillFromItem(@Nonnull ItemStack item, Consumer<Bill> callback) {
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null && meta.hasLore()) {
            for (String lore : meta.getLore()) {
                if (lore.startsWith("BILL ID ")) {
                    int id;
                    try {
                        id = Integer.parseInt(lore.substring(8));
                    } catch (Exception e) {
                        break;
                    }
                    getBill(id, callback);
                    return;
                }
            }
        }
        callback.accept(null);
    }
    
    @Override
    public void discard(String director, Consumer<Boolean> callback) {
        BillTable table = (BillTable) Database.getTable(Database.BILL_RECORD);
        table.terminateRecord(id, director, callback);
    }
    
    @Override
    public boolean isExpired() {
        return expired;
    }
    
    @Override
    public String getTerminator() {
        return terminator;
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
    public int getId() {
        return id;
    }
    
    @Override
    public String getDate() {
        return date;
    }
    
    @Override
    public String getOrigin() {
        return origin;
    }
    
}