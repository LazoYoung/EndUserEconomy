package io.github.lazoyoung.endusereconomy.bank.menu;

import io.github.lazoyoung.endusereconomy.economy.Currency;
import me.kangarko.ui.menu.MenuButton;
import me.kangarko.ui.menu.menues.MenuStandard;
import me.kangarko.ui.model.ItemCreator;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AccountView extends MenuStandard {
    
    private OfflinePlayer user;
    private Currency curr;
    private int balance;
    private boolean ready = false;
    private ItemStack balanceItem, recordsItem;
    
    private AccountView(OfflinePlayer user, Currency currency, int balance) {
        super(AccountHome.getMenu());
        this.user = user;
        this.curr = currency;
        this.balance = balance;
        setTitle(user.getName() + "'s account");
    }
    
    public static void displayTo(Player viewer, OfflinePlayer targetPlayer, Currency currency) {
        int balance = (int) currency.getEconomyHandler().getBalance(targetPlayer, currency.getName());
        AccountView menu = new AccountView(targetPlayer, currency, balance);
        menu.displayTo(viewer);
        menu.animateTitle("Loading...");
        menu.ready = true;
        menu.restartMenu(menu.getTitle());
    }
    
    @Override
    public void onMenuClick(Player player, int slot, ItemStack clicked) {
        if (clicked.isSimilar(recordsItem)) {
            AccountRecords.getMenu(player, curr, (menu) -> menu.displayTo(player));
        }
    }
    
    @Override
    protected List<MenuButton> getButtonsToAutoRegister() {
        List<MenuButton> list = new ArrayList<>();
        if (this.ready) {
            ItemCreator.ItemCreatorBuilder balance = ItemCreator.of(Material.GOLD_INGOT).name("Balance: " + this.balance);
            ItemCreator.ItemCreatorBuilder records = ItemCreator.of(Material.PAPER).name("Transaction records").lore("Click to view.");
            balanceItem = balance.build().make();
            recordsItem = records.build().make();
            MenuButton balanceBtn = MenuButton.makeDummy(balance);
            MenuButton recordsBtn = MenuButton.makeDummy(records);
            list.add(balanceBtn);
            list.add(recordsBtn);
        }
        else {
            MenuButton loading = MenuButton.makeDummy(ItemCreator.of(Material.RED_STAINED_GLASS_PANE).name("Loading.."));
            list.add(loading);
        }
        return list;
    }
    
    @Override
    protected ItemStack getItemAt(int slot) {
        switch (slot) {
            case 12:
                return balanceItem;
            case 14:
                return recordsItem;
            default:
                return null;
        }
    }
    
    @Override
    protected String[] getInfo() {
        return new String[] {
                "Currency: " + curr.toString(),
                "This menu lists the following items:",
                "- Account balance",
                "- Transaction records"
        };
    }
    
}
