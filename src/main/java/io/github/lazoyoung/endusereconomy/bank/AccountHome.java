package io.github.lazoyoung.endusereconomy.bank;

import me.kangarko.ui.menu.Menu;
import me.kangarko.ui.menu.menues.MenuStandard;

public class AccountHome extends MenuStandard {
    
    private static AccountHome instance = null;
    
    private AccountHome() {
        super(null);
        setTitle("Account view");
    }
    
    static Menu getMenu() {
        if (instance == null) {
            instance = new AccountHome();
        }
        return instance;
    }
    
    /**
     * Get the menu description. By default a nether star appear in the bottom left corner.
     * <p>
     * Return null to disable.
     */
    @Override
    protected String[] getInfo() {
        return new String[0];
    }
}
