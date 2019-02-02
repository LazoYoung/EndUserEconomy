package io.github.lazoyoung.endusereconomy.bank.menu;

import io.github.lazoyoung.endusereconomy.util.Text;
import me.kangarko.ui.menu.Menu;
import me.kangarko.ui.menu.MenuButton;
import me.kangarko.ui.menu.menues.MenuStandard;
import me.kangarko.ui.model.ItemCreator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class ConfirmWindow extends MenuStandard {
    
    private String[] infoLore;
    private MenuButton confirmBtn, cancelBtn;
    private List<ItemStack> items;
    private boolean succeed;
    
    private ConfirmWindow() {
        super(AccountHome.getMenu());
        setSize(27);
        setTitle("Are you going to proceed?");
    }
    
    public static void displayTo(Player viewer, String[] infoLore, Consumer<Boolean> listener, @Nullable List<ItemStack> itemsToRestore) {
        ConfirmWindow menu = new ConfirmWindow();
        menu.infoLore = infoLore;
        menu.confirmBtn = new MenuButton() {
            @Override
            public void onClickedInMenu(Player pl, Menu menu1, ClickType click) {
                menu.close(true);
                listener.accept(true);
            }
    
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.LIME_STAINED_GLASS_PANE).name("승인").build().make();
            }
        };
        menu.cancelBtn = new MenuButton() {
            @Override
            public void onClickedInMenu(Player pl, Menu menu1, ClickType click) {
                menu.close(false);
                listener.accept(false);
            }
    
            @Override
            public ItemStack getItem() {
                return ItemCreator.of(Material.RED_STAINED_GLASS_PANE).name("취소").build().make();
            }
        };
        menu.items = itemsToRestore;
        menu.succeed = false;
        menu.displayTo(viewer);
        menu.restartMenu(menu.getTitle());
    }
    
    @Override
    protected List<MenuButton> getButtonsToAutoRegister() {
        if (confirmBtn == null) {
            return null;
        }
        
        List<MenuButton> list = new ArrayList<>();
        list.add(confirmBtn);
        list.add(cancelBtn);
        return list;
    }
    
    @Override
    protected ItemStack getItemAt(int slot) {
        if (confirmBtn == null) {
            return null;
        }
        
        switch (slot) {
            case 12:
                return cancelBtn.getItem();
            case 14:
                return confirmBtn.getItem();
            default:
                return null;
        }
    }
    
    @Override
    public void onMenuClose(Player player, Inventory inv) {
        if (succeed) {
            return;
        }
        if (items != null && !items.isEmpty()) {
            HashMap<Integer, ItemStack> map = player.getInventory().addItem(items.toArray(new ItemStack[0]));
            map.forEach((index, item) -> player.getWorld().dropItem(player.getLocation(), item));
        }
        Text.actionMessage(player, ChatColor.YELLOW + "거래를 취소하셨습니다.", 40L);
    }
    
    @Override
    protected String[] getInfo() {
        return infoLore;
    }
    
    private void close(boolean succeed) {
        this.succeed = succeed;
        getViewer().closeInventory();
    }
}
