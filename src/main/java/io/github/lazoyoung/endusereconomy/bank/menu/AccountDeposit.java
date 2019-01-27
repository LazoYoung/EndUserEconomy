package io.github.lazoyoung.endusereconomy.bank.menu;

import io.github.lazoyoung.endusereconomy.bill.Bill;
import io.github.lazoyoung.endusereconomy.bill.BillFactory;
import io.github.lazoyoung.endusereconomy.economy.Currency;
import me.kangarko.ui.menu.Menu;
import me.kangarko.ui.menu.MenuButton;
import me.kangarko.ui.menu.MenuClickLocation;
import me.kangarko.ui.menu.menues.MenuStandard;
import me.kangarko.ui.model.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class AccountDeposit extends MenuStandard {
    
    private ItemStack rightSignItem, leftSignItem, infoItem, returnItem;
    private Currency currency;
    private Map<Integer,Bill> insertBills;
    private List<ItemStack> insertItems;
    private List<Integer> processSlots;
    private int amount;
    
    private AccountDeposit() {
        super(null);
        this.rightSignItem = ItemCreator.of(Material.BLACK_STAINED_GLASS_PANE).name("->").build().make();
        this.leftSignItem = ItemCreator.of(Material.BLACK_STAINED_GLASS_PANE).name("<-").build().make();
        this.insertBills = new HashMap<>();
        this.insertItems = new ArrayList<>();
        this.processSlots = new ArrayList<>();
        this.amount = 0;
        setTitle("Deposit to account");
    }
    
    public static void displayTo(Player viewer, Currency currency) {
        AccountDeposit menu = new AccountDeposit();
        menu.currency = currency;
        menu.displayTo(viewer);
    }
    
    @Override
    protected ItemStack getItemAt(int slot) {
        if (slot == getInfoButtonPosition()) {
            return infoItem;
        }
        if (slot == getReturnButtonPosition()) {
            return returnItem;
        }
        switch (slot) {
            case 0:
            case 1:
            case 9:
            case 10:
            case 19:
                return rightSignItem;
            case 7:
            case 8:
            case 16:
            case 17:
            case 25:
                return leftSignItem;
            default:
                return null;
        }
    }
    
    @Override
    protected List<MenuButton> getButtonsToAutoRegister() {
        List<MenuButton> list = new ArrayList<>();
        MenuButton info = new MenuButton() {
            @Override
            public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                if (insertBills.isEmpty()) {
                    menu.animateTitle("Please insert the bill.");
                    return;
                }
                
                menu.animateTitle("Please wait...");
            }
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.NETHER_STAR).name("클릭하여 입금").lore("투입 금액: " + amount).build().make();
            }
        };
        MenuButton returnBack = new MenuButton() {
            @Override
            public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                AccountHome.getMenu().displayTo(player);
            }
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.OAK_DOOR).name("뒤로가기").build().make();
            }
        };
        infoItem = info.getItem();
        returnItem = returnBack.getItem();
        list.add(info);
        list.add(returnBack);
        return list;
    }
    
    @Override
    public boolean isActionAllowed(MenuClickLocation location, int slot, ItemStack clicked, ItemStack cursor) {
        if (location == MenuClickLocation.PLAYER) {
            return true;
        }
        else if (location == MenuClickLocation.MENU) {
            if (isInlet(slot)) {
                return !processSlots.contains(slot);
            }
        }
        return false;
    }
    
    // non-button click event
    @Override
    public void onMenuClick(Player player, int slot, InventoryAction action, ClickType click, ItemStack cursor, ItemStack clicked, boolean cancelled) {
        if (clicked.isSimilar(leftSignItem) || clicked.isSimilar(rightSignItem)) {
            animateTitle("Please insert the bill.");
        }
        else if (isInlet(slot)) {
            switch (action) {
                case NOTHING:
                    return;
                case PLACE_ALL:
                case PLACE_SOME:
                case PLACE_ONE:
                    updateInsert(player, cursor.clone(), slot, false);
                    break;
                case SWAP_WITH_CURSOR:
                    updateInsert(player, cursor.clone(), slot, true);
                    break;
                case PICKUP_ALL:
                case PICKUP_HALF:
                case PICKUP_SOME:
                case PICKUP_ONE:
                    updateInsert(player, null, slot, true);
                    break;
            }
        }
    }
    
    @Override
    public void onMenuClose(Player player, Inventory inv) {
        if (!insertItems.isEmpty()) {
            HashMap<Integer, ItemStack> map = player.getInventory().addItem(insertItems.toArray(new ItemStack[0]));
            map.forEach((index, item) -> player.getWorld().dropItem(player.getLocation(), item));
        }
    }
    
    @Override
    protected boolean addReturnButton() {
        return false;
    }
    
    @Override
    protected String[] getInfo() {
        return null;
    }
    
    private boolean isInlet(int slot) {
        return (1 < slot && slot < 7) || (10 < slot && slot < 16) || (19 < slot && slot < 25);
    }
    
    private void updateInsert(Player player, ItemStack insert, int slot, boolean replaceOld) {
        if (replaceOld) {
            insertBills.remove(slot);
        }
        if (insert != null) {
            processSlots.add(slot);
            BillFactory.getBillFromItem(insert, (bill) -> {
                if (checkItemValid(bill)) {
                    amount += bill.getUnit();
                    insertBills.put(slot, bill);
                    insertItems.add(insert);
                    restartMenu("Inserting " + bill.getUnit());
                }
                else {
                    InventoryView view = player.getOpenInventory();
                    HashMap<Integer,ItemStack> map = player.getInventory().addItem(insert);
                    view.getTopInventory().setItem(slot, null);
                    view.setCursor(null);
                    map.values().forEach((item) -> player.getWorld().dropItem(player.getLocation(), item));
                }
                processSlots.remove(new Integer(slot));
            });
        }
    }
    
    private boolean checkItemValid(Bill bill) {
        if (bill == null) {
            animateTitle("That is not a bill.");
            return false;
        }
        if (!bill.getCurrency().toString().equals(currency.toString())) {
            animateTitle("The bill currency is invalid.");
            return false;
        }
        if (bill.isExpired()) {
            animateTitle("The bill is expired.");
            return false;
        }
        return true;
    }
}
