package io.github.lazoyoung.endusereconomy.bank.menu;

import io.github.lazoyoung.endusereconomy.bill.BillFactory;
import io.github.lazoyoung.endusereconomy.economy.Currency;
import io.github.lazoyoung.endusereconomy.economy.handler.EconomyHandler;
import me.kangarko.ui.menu.Menu;
import me.kangarko.ui.menu.MenuButton;
import me.kangarko.ui.menu.menues.MenuStandard;
import me.kangarko.ui.model.ItemCreator;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.function.Consumer;

public class AccountWithdraw extends MenuStandard {
    
    private Currency currency;
    private HashMap<Integer,ItemStack> moneyItem; // key: unit, value: item
    private HashMap<Integer,Integer> moneyQty; // key: unit, value: quantity
    private List<Integer> units;
    private ItemStack infoItem, returnItem;
    private HashMap<Integer,ItemStack> plusItem, minusItem;
    
    private AccountWithdraw(Currency currency) {
        super(null);
        this.currency = currency;
        this.infoItem = ItemCreator.of(Material.NETHER_STAR).name("클릭하여 인출").build().make();
        this.returnItem = ItemCreator.of(Material.OAK_DOOR).name("뒤로가기").build().make();
        setSize(27);
        setTitle("계좌 인출");
        initMoneyItems();
    }
    
    public static void displayTo(Player viewer, Currency currency) {
        AccountWithdraw menu = new AccountWithdraw(currency);
        menu.displayTo(viewer);
    }
    
    @Override
    protected List<MenuButton> getButtonsToAutoRegister() {
        List<MenuButton> list = new ArrayList<>();
        MenuButton returnBtn = new MenuButton() {
            @Override
            public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                AccountHome.getMenu().displayTo(player);
            }
            @Override
            public ItemStack getItem() {
                return returnItem;
            }
        };
        MenuButton actionBtn = new MenuButton() {
            @Override
            public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                int amount = getAmount();
                
                if (amount > 0) {
                    EconomyHandler eco = currency.getEconomyHandler();
                    int newBal = (int) eco.getBalance(player, currency.getName()) - amount;
                    if (newBal < 0) {
                        animateTitle(ChatColor.RED + "잔고가 부족합니다!");
                    }
                    else {
                        String[] infoLore = new String[] { "인출 금액: " + amount, "예상 잔고: " + newBal };
                        Consumer<Boolean> listener = (confirmed) -> {
                            if (confirmed) {
                                eco.withdraw(player, currency.getName(), amount);
                                giveMoneyItems(player);
                            }
                        };
                        ConfirmWindow.displayTo(player, infoLore, listener, null);
                    }
                }
                else {
                    animateTitle("Select the amount to withdraw.");
                }
            }
            @Override
            public ItemStack getItem() {
                return infoItem;
            }
        };
        
        for (int unit : units) {
            MenuButton plusBtn = new MenuButton() {
                @Override
                public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                    int qty = moneyQty.get(unit);
                    if (click.isShiftClick()) {
                        qty += 10;
                    } else {
                        qty += 1;
                    }
                    
                    if (qty <= 64) {
                        moneyQty.put(unit, qty);
                        restartMenu(getTitle());
                    }
                }
                @Override
                public ItemStack getItem() {
                    return plusItem.get(unit);
                }
            };
            MenuButton minusBtn = new MenuButton() {
                @Override
                public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                    int qty = moneyQty.get(unit);
                    if (click.isShiftClick()) {
                        qty -= 10;
                    } else {
                        qty -= 1;
                    }
        
                    if (qty <= 64) {
                        moneyQty.put(unit, qty);
                        restartMenu(getTitle());
                    }
                }
                @Override
                public ItemStack getItem() {
                    return minusItem.get(unit);
                }
            };
            list.add(plusBtn);
            list.add(minusBtn);
        }
        
        list.add(returnBtn);
        list.add(actionBtn);
        return list;
    }
    
    @Override
    protected ItemStack getItemAt(int slot) {
        if (slot == getInfoButtonPosition()) {
            return infoItem;
        }
        
        if ((slot > 0 && slot < 8) || (slot > 18 && slot < 26)) {
            /* switches */
            try {
                if (slot < 8) {
                    int unit = units.get(slot - 1);
                    ItemStack item = plusItem.get(unit);
                    int quantity = moneyQty.get(unit);
                    if (quantity >= 64) {
                        ItemMeta meta = item.getItemMeta();
                        item.setType(Material.BLACK_STAINED_GLASS_PANE);
                        meta.setDisplayName(" ");
                        item.setItemMeta(meta);
                    }
                    return item;
                } else {
                    int unit = units.get(slot - 19);
                    ItemStack item = minusItem.get(unit);
                    int quantity = moneyQty.get(unit);
                    if (quantity <= 0) {
                        ItemMeta meta = item.getItemMeta();
                        item.setType(Material.BLACK_STAINED_GLASS_PANE);
                        meta.setDisplayName(" ");
                        item.setItemMeta(meta);
                    }
                    return item;
                }
            } catch (IndexOutOfBoundsException ignored) {}
        }
        else if (slot > 9 && slot < 17) {
            /* bill units */
            try {
                int unit = units.get(slot - 10);
                ItemStack item = moneyItem.get(unit).clone();
                ItemMeta meta = item.getItemMeta();
                int quantity = moneyQty.get(unit);
                if (quantity < 1) {
                    item.setAmount(1);
                    meta.setDisplayName(ChatColor.GRAY + meta.getDisplayName());
                    item.setItemMeta(meta);
                } else {
                    item.setAmount(quantity);
                }
                return item;
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
    
    @Override
    protected String[] getInfo() {
        return null;
    }
    
    @Override
    protected boolean addReturnButton() {
        return false;
    }
    
    private void initMoneyItems() {
        this.moneyItem = new HashMap<>();
        this.moneyQty = new HashMap<>();
        this.units = new ArrayList<>();
        this.plusItem = new HashMap<>();
        this.minusItem = new HashMap<>();
        Set<Integer> units = BillFactory.getRegisteredUnits(currency);
        if (units != null) {
            this.units = new ArrayList<>(units);
            Collections.sort(this.units);
            for (int unit : this.units) {
                moneyItem.put(unit, BillFactory.getItemBase(currency, unit));
                moneyQty.put(unit, 0);
                plusItem.put(unit, ItemCreator.of(Material.LIME_STAINED_GLASS_PANE).name("+" + unit).build().make());
                minusItem.put(unit, ItemCreator.of(Material.RED_STAINED_GLASS_PANE).name("-" + unit).build().make());
            }
        }
    }
    
    private int getAmount() {
        int amount = 0;
        for (int unit : units) {
            amount += moneyQty.get(unit) * unit;
        }
        return amount;
    }
    
    private void giveMoneyItems(Player player) {
        moneyQty.forEach((unit, qty) -> {
            final Inventory inv = player.getInventory();
            final Location loc = player.getLocation();
            for (int i = 0; i < qty; i++)
                BillFactory.printNew(currency, unit, player.getName(), (bill) -> {
                    HashMap<Integer,ItemStack> map = inv.addItem(bill.getItem());
                    map.values().forEach(item -> loc.getWorld().dropItem(loc, item));
                });
        });
    }
}