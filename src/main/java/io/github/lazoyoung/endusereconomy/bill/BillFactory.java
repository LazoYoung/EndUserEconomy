package io.github.lazoyoung.endusereconomy.bill;

import io.github.lazoyoung.endusereconomy.Config;
import io.github.lazoyoung.endusereconomy.database.BillTable;
import io.github.lazoyoung.endusereconomy.database.Database;
import io.github.lazoyoung.endusereconomy.economy.Currency;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.UUID;
import java.util.function.Consumer;

public class BillFactory implements Bill {

    private Currency currency;
    private int unit;
    private UUID uniqueId;
    private ItemStack itemStack;
    private String date;
    private String origin;
    
    public BillFactory(UUID uniqueId, Currency currency, int unit, String date, String origin, ItemStack itemStack) {
        this.uniqueId = uniqueId;
        this.currency = currency;
        this.unit = unit;
        this.date = date;
        this.origin = origin;
        this.itemStack = itemStack;
    }
    
    /**
     * @param callback Callback gets called once the bill is constructed.
     *                 Beware it may supply null argument due to a failure on database transaction.
     */
    public static void printNew(Currency currency, int unit, String origin, ItemStack itemStack, Consumer<Bill> callback) {
        BillFactory factory = new BillFactory(null, currency, unit, null, origin, itemStack);
        BillTable table = (BillTable) Database.getTable(Database.BILL);
        Consumer<UUID> addResult = (uuid -> {
            if (uuid == null) {
                callback.accept(null);
                return;
            }
            factory.uniqueId = uuid;
            try {
                factory.recordItemStack();
            } catch (IOException e) {
                e.printStackTrace();
                callback.accept(null);
                // TODO delete record from database
                return;
            }
            callback.accept(factory);
        });
        
        table.addRecord(addResult, factory);
    }
    
    public static void getBill(UUID uniqueId, Consumer<BillFactory> callback) {
        BillTable table = (BillTable) Database.getTable(Database.BILL);
        table.queryRecord(uniqueId, callback);
    }
    
    public static ItemStack loadItemStack(UUID uniqueId) {
    
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
    
    @Override
    public String getIssueDate() {
        return date;
    }
    
    @Override
    public String getOrigin() {
        return origin;
    }
    
    private void recordItemStack() throws IOException {
        FileConfiguration config = Config.BILL.get();
        String pluginName = currency.getEconomy().getPluginName();
        String currencyName = currency.getName();
        config.set(pluginName + "." + currencyName + ".unit", unit);
        config.set(pluginName + "." + currencyName + ".itemstack", itemStack);
        Config.BILL.save(config);
    }
    
}