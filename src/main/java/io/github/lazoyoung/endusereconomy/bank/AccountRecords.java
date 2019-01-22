package io.github.lazoyoung.endusereconomy.bank;

import io.github.lazoyoung.endusereconomy.bank.transaction.Transaction;
import io.github.lazoyoung.endusereconomy.economy.Currency;
import me.kangarko.ui.menu.menues.MenuPagged;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class AccountRecords extends MenuPagged<Transaction> {
    
    private AccountRecords(Iterable<Transaction> pages) {
        super(9*2, AccountHome.getMenu(), pages);
    }
    
    public static void getMenu(Player player, Currency currency, Consumer<AccountRecords> callback) {
        Transaction.getRecords(player.getName(), currency, 30, (records) -> callback.accept(new AccountRecords(records)));
    }
    
    @Override
    protected String getMenuTitle() {
        return "Account records";
    }
    
    @Override
    protected ItemStack convertToItemStack(Transaction object) {
        return null;
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
