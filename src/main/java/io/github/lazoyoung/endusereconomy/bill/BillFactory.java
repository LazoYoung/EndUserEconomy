package io.github.lazoyoung.endusereconomy.bill;

import io.github.lazoyoung.endusereconomy.Config;
import io.github.lazoyoung.endusereconomy.database.BillTable;
import io.github.lazoyoung.endusereconomy.database.Database;
import io.github.lazoyoung.endusereconomy.economy.Currency;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
        BillTable table = (BillTable) Database.getTable(Database.BILL_REC);
        Consumer<Integer> addResult = (id -> {
            if (id == null) {
                callback.accept(null);
                return;
            }
            factory.id = id;
            callback.accept(factory);
        });
        
        table.addRecord(addResult, factory);
    }
    
    public static void getBill(int id, Consumer<Bill> callback) {
        BillTable table = (BillTable) Database.getTable(Database.BILL_REC);
        table.queryRecord(id, callback);
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
    
    public static void getBillFromItem(@Nonnull ItemStack item, Consumer<Bill> callback) {
        for (String lore : item.getItemMeta().getLore()) {
            if (lore.startsWith("ID ")) {
                int id;
                try {
                    id = Integer.parseInt(lore.substring(3));
                } catch (Exception e) {
                    break;
                }
                getBill(id, callback);
                return;
            }
        }
        callback.accept(null);
    }
    
    @Override
    public void discard(String director, Consumer<Boolean> callback) {
        BillTable table = (BillTable) Database.getTable(Database.BILL_REC);
        table.terminateRecord(id, director, callback);
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
            lore.add("ID " + id);
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
            itemStack.setAmount(1);
        }
        return itemStack;
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