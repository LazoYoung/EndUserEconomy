package io.github.lazoyoung.endusereconomy.bank.menu;

import io.github.lazoyoung.endusereconomy.bank.transaction.Receive;
import io.github.lazoyoung.endusereconomy.bank.transaction.Send;
import io.github.lazoyoung.endusereconomy.bank.transaction.Transaction;
import io.github.lazoyoung.endusereconomy.bank.transaction.TransactionType;
import io.github.lazoyoung.endusereconomy.economy.Currency;
import me.kangarko.ui.menu.MenuInventory;
import me.kangarko.ui.menu.menues.MenuPagged;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AccountRecords extends MenuPagged<Transaction> {
    
    private boolean empty = false;
    private Currency currency;
    
    private AccountRecords(OfflinePlayer user, Currency currency, Iterable<Transaction> pages) {
        super(9*2, AccountHome.getMenu(), pages);
        this.currency = currency;
        setTitle("Account records of " + user.getName().toUpperCase());
        registerFields();
    }
    
    private AccountRecords(OfflinePlayer user, Currency currency) {
        super(9*2, AccountHome.getMenu(), new ArrayList<>());
        this.empty = true;
        this.currency = currency;
        setTitle("Account records of " + user.getName().toUpperCase());
        registerFields();
    }
    
    public static void getMenu(OfflinePlayer user, Currency currency, Consumer<AccountRecords> callback) {
        Transaction.getRecordsReversed(user.getName(), currency, 30, (records) -> {
            if (records.isEmpty()) {
                callback.accept(new AccountRecords(user, currency));
            } else {
                callback.accept(new AccountRecords(user, currency, records));
            }
        });
    }
    
    @Override
    protected String getMenuTitle() {
        return "Account records";
    }
    
    @Override
    protected ItemStack convertToItemStack(Transaction t) {
        int amount = t.getAmount();
        int result = t.getResult();
        String date = t.getDate().toLocalDateTime().format(DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm"));
        String note = t.getNote();
        TransactionType type = t.getType();
        ItemStack item = new ItemStack(Material.GOLD_BLOCK);
        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<>();
        
        switch (type) {
            case DEPOSIT:
                meta.setDisplayName("Deposit");
                lore.add("You have deposit " + amount);
                break;
            case WITHDRAW:
                meta.setDisplayName("Withdraw");
                lore.add("You have withdrawn " + amount);
                break;
            case TRANSFER:
                meta.setDisplayName("Transfer");
                if (t instanceof Send) {
                    Send send = (Send) t;
                    lore.add("You have sent " + amount + " to " + send.getReceiver());
                } else if (t instanceof Receive) {
                    Receive receive = (Receive) t;
                    lore.add("You have received " + amount + " from " + receive.getSender());
                }
                break;
        }
        lore.add(ChatColor.GRAY + note);
        lore.add("Total: " + result);
        lore.add(date);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    @Override
    protected void onMenuClickPaged(Player pl, Transaction object, ClickType click) {
    
    }
    
    @Override
    protected String[] getInfo() {
        return new String[] {
                "Currency: " + currency.toString(),
                "This menu lists the following items:",
                "- Transfer records",
                "- Deposit/withdraw records",
                "- Account history"
        };
    }
    
    @Override
    protected void paint(MenuInventory inv) {
        if (empty) {
            ItemStack item = new ItemStack(Material.BARRIER);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.RED + "Record not found.");
            item.setItemMeta(meta);
            inv.setItem(0, item);
        }
    }
    
}
