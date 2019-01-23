package io.github.lazoyoung.endusereconomy.bank;

import io.github.lazoyoung.endusereconomy.bank.transaction.Receive;
import io.github.lazoyoung.endusereconomy.bank.transaction.Send;
import io.github.lazoyoung.endusereconomy.bank.transaction.Transaction;
import io.github.lazoyoung.endusereconomy.bank.transaction.TransactionType;
import io.github.lazoyoung.endusereconomy.economy.Currency;
import me.kangarko.ui.menu.menues.MenuPagged;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AccountRecords extends MenuPagged<Transaction> {
    
    private AccountRecords(Iterable<Transaction> pages) {
        super(9*2, AccountHome.getMenu(), pages);
    }
    
    public static void getMenu(Player player, Currency currency, Consumer<AccountRecords> callback) {
        Transaction.getRecordsReversed(player.getName(), currency, 30, (records) -> callback.accept(new AccountRecords(records)));
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
                "This menu lists the following items:",
                "- Transfer records",
                "- Deposit/withdraw records",
                "- Account history"
        };
    }
}
