package io.github.lazoyoung.endusereconomy.bank.menu;

import io.github.lazoyoung.endusereconomy.Main;
import io.github.lazoyoung.endusereconomy.bill.Bill;
import io.github.lazoyoung.endusereconomy.bill.BillFactory;
import io.github.lazoyoung.endusereconomy.economy.Currency;
import me.kangarko.ui.menu.Menu;
import me.kangarko.ui.menu.MenuButton;
import me.kangarko.ui.menu.MenuClickLocation;
import me.kangarko.ui.menu.menues.MenuStandard;
import me.kangarko.ui.model.ItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AccountDeposit extends MenuStandard implements Listener {
    
    private ItemStack rightSignItem, leftSignItem, infoItem, returnItem;
    private Currency currency;
    private List<Integer> processSlots;
    
    private AccountDeposit(Currency currency) {
        super(null);
        this.rightSignItem = ItemCreator.of(Material.BLACK_STAINED_GLASS_PANE).name("->").build().make();
        this.leftSignItem = ItemCreator.of(Material.BLACK_STAINED_GLASS_PANE).name("<-").build().make();
        this.processSlots = new ArrayList<>();
        this.currency = currency;
        this.infoItem = ItemCreator.of(Material.NETHER_STAR).name("클릭하여 입금").build().make();
        this.returnItem = ItemCreator.of(Material.OAK_DOOR).name("뒤로가기").build().make();
        setSize(27);
        setTitle("Deposit to account");
        Bukkit.getPluginManager().registerEvents(this, Main.getInstance());
    }
    
    public static void displayTo(Player viewer, Currency currency) {
        AccountDeposit menu = new AccountDeposit(currency);
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
                if (getInserts(player.getOpenInventory().getTopInventory()).size() == 0) {
                    menu.animateTitle(ChatColor.RED + "Please insert the bill.");
                    return;
                }
                menu.animateTitle("Loading...");
            }
            @Override
            public ItemStack getItem() {
                return infoItem;
            }
        };
        MenuButton returnBack = new MenuButton() {
            @Override
            public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                AccountHome.getMenu().displayTo(player);
            }
            @Override
            public ItemStack getItem() {
                return returnItem;
            }
        };
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
            animateTitle(ChatColor.RED + "Please insert the bill.");
        }
        else if (isInlet(slot)) {
            switch (action) {
                case NOTHING:
                    break;
                case PICKUP_ALL:
                case PICKUP_HALF:
                case PICKUP_SOME:
                case PICKUP_ONE:
                    addInsert(player, cursor.clone(), slot);
                    break;
                case PLACE_ALL:
                case PLACE_SOME:
                case PLACE_ONE:
                    addInsert(player, cursor.clone(), slot);
                    break;
            }
        }
    }
    
    @Override
    public void onMenuClose(Player player, Inventory inv) {
        List<ItemStack> inserts = getInserts(inv);
        if (inserts.size() > 0) {
            HashMap<Integer, ItemStack> map = player.getInventory().addItem(inserts.toArray(new ItemStack[0]));
            map.forEach((index, item) -> player.getWorld().dropItem(player.getLocation(), item));
            player.sendMessage("You have cancelled the transaction.");
        }
        HandlerList.unregisterAll(this);
    }
    
    @EventHandler
    public void onItemDrag(InventoryDragEvent event) {
        if (getTitle().substring(2).equals(event.getView().getTitle())) {
            Integer[] slots = event.getRawSlots().toArray(new Integer[0]);
            InventoryView view = event.getView();
            for (int slot : slots) {
                if (slot < view.getTopInventory().getSize()) {
                    if (slots.length == 1) {
                        ItemStack item = event.getOldCursor().clone();
                        addInsert((Player) event.getWhoClicked(), item, slot);
                    } else {
                        animateTitle(ChatColor.RED + "Do not drag items.");
                        event.setCancelled(true);
                        break;
                    }
                }
            }
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
    
    private List<ItemStack> getInserts(Inventory inv) {
        List<ItemStack> list = new ArrayList<>();
        for (int i = 0; i < getSize(); i++) {
            if (isInlet(i)) {
                ItemStack item = inv.getItem(i);
                if (item != null) {
                    list.add(item);
                }
            }
        }
        return list;
    }
    
    private boolean isInlet(int slot) {
        return (1 < slot && slot < 7) || (10 < slot && slot < 16) || (19 < slot && slot < 25);
    }
    
    private void addInsert(Player player, ItemStack insert, int slot) {
        if (insert.getType().equals(Material.AIR))
            return;
        
        processSlots.add(slot);
        BillFactory.getBillFromItem(insert, (bill) -> {
            if (!checkItemValid(bill)) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        InventoryView view = player.getOpenInventory();
                        HashMap<Integer,ItemStack> map = player.getInventory().addItem(insert);
                        view.getTopInventory().setItem(slot, null);
                        view.setCursor(null);
                        player.updateInventory();
                        map.values().forEach((item) -> player.getWorld().dropItem(player.getLocation(), item));
                    }
                }.runTask(Main.getInstance());
            }
            processSlots.remove(new Integer(slot));
        });
    }
    
    private boolean checkItemValid(Bill bill) {
        if (bill == null) {
            animateTitle(ChatColor.RED + "That is not a bill.");
            return false;
        }
        if (!bill.getCurrency().toString().equals(currency.toString())) {
            animateTitle(ChatColor.RED + "The bill currency is invalid.");
            return false;
        }
        if (bill.isExpired()) {
            animateTitle(ChatColor.RED + "The bill is expired.");
            return false;
        }
        return true;
    }
}
