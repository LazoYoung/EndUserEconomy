package io.github.lazoyoung.endusereconomy.bank.menu;

import me.kangarko.ui.menu.MenuButton;
import me.kangarko.ui.menu.menues.MenuStandard;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ConfirmWindow extends MenuStandard {
    
    private String[] infoLore;
    private MenuButton confirmBtn, cancelBtn;
    
    private ConfirmWindow(String[] infoLore, MenuButton confirmBtn, MenuButton cancelBtn) {
        super(AccountHome.getMenu());
        this.infoLore = infoLore;
        this.confirmBtn = confirmBtn;
        this.cancelBtn = cancelBtn;
        setSize(27);
        setTitle("Are you going to proceed?");
    }
    
    public static void displayTo(Player viewer, String[] infoLore, MenuButton confirmBtn, MenuButton cancelBtn) {
        ConfirmWindow menu = new ConfirmWindow(infoLore, confirmBtn, cancelBtn);
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
    protected String[] getInfo() {
        return infoLore;
    }
}
