package io.github.lazoyoung.endusereconomy.bank.menu;

import io.github.lazoyoung.endusereconomy.Main;
import io.github.lazoyoung.endusereconomy.economy.Currency;
import io.github.lazoyoung.endusereconomy.util.Text;
import me.kangarko.ui.menu.Menu;
import me.kangarko.ui.menu.MenuButton;
import me.kangarko.ui.menu.menues.MenuStandard;
import me.kangarko.ui.model.ItemCreator;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AccountTransfer extends MenuStandard {
    
    private ItemStack targetItem;
    private ItemStack amountItem;
    private ItemStack infoItem = ItemCreator.of(Material.NETHER_STAR, "Click to transfer").build().make();
    private OfflinePlayer target = null;
    private int amount = 0;
    private Currency currency;
    private final ConversationAbandonedListener abandonedListener = (event) -> {
        if (event.gracefulExit()) {
            event.getContext().getForWhom().sendRawMessage(ChatColor.YELLOW + "Input mode terminated.");
        }
    };
    
    public AccountTransfer(Currency currency) {
        super(AccountHome.getMenu());
        this.currency = currency;
        setSize(27);
        setTitle("이체");
    }
    
    @Override
    protected List<MenuButton> getButtonsToAutoRegister() {
        List<MenuButton> list = new ArrayList<>();
        // 12 - player sel, 14 - amount sel
        MenuButton playerSel = new MenuButton() {
            @Override
            public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                // StringPrompt
                StringPrompt prompt = new StringPrompt() {
                    @Override
                    public String getPromptText(ConversationContext context) {
                        Text.actionMessage(player, ChatColor.AQUA + "Type \'exit\' to terminate input mode.", 1200L);
                        return "Which player you will send money to?";
                    }
                    @SuppressWarnings("deprecation")
                    @Override
                    public Prompt acceptInput(ConversationContext context, String input) {
                        target = Bukkit.getOfflinePlayer(input);
                        if (target.hasPlayedBefore()) {
                            displayTo(player, true);
                            restartMenu(getTitle());
                            animateTitle("Recipient set to " + target.getName());
                            return END_OF_CONVERSATION;
                        }
                        target = null;
                        context.getForWhom().sendRawMessage("That player does not exist.");
                        return this;
                    }
                };
                startConversation(player, prompt, abandonedListener);
            }
            @Override
            public ItemStack getItem() {
                return targetItem;
            }
        };
        MenuButton amountSel = new MenuButton() {
            @Override
            public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                NumericPrompt prompt = new NumericPrompt() {
                    @Override
                    protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
                        int val = input.intValue();
                        
                        if (val > 0) {
                            amount = val;
                            displayTo(player, true);
                            restartMenu(getTitle());
                            animateTitle("Remittance set to " + amount);
                            return END_OF_CONVERSATION;
                        }
                        context.getForWhom().sendRawMessage("That's not a valid number.");
                        return this;
                    }
    
                    @Override
                    public String getPromptText(ConversationContext context) {
                        Text.actionMessage(player, ChatColor.AQUA + "Type \'exit\' to terminate input mode.", 1200L);
                        return "How much money will you send? (in number)";
                    }
                };
                startConversation(player, prompt, abandonedListener);
            }
            @Override
            public ItemStack getItem() {
                return amountItem;
            }
        };
        MenuButton info = new MenuButton() {
            @Override
            public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                if (target == null) {
                    animateTitle("Select the recipient.");
                    return;
                }
                if (amount == 0) {
                    animateTitle("Select the amount.");
                    return;
                }
                String[] infoLore = {"송금액: " + amount, "수령인: " + target.getName()};
                Consumer<Boolean> listener = (confirmed) -> {
                    if (confirmed) {
                        EconomyResponse response = currency.getEconomyHandler().transfer(player, target, currency.getName(), amount);
                        
                        if (response.transactionSuccess()) {
                            Text.actionMessage(player, "Sent " + amount + " to " + target.getName(), 100L);
                        } else {
                            player.sendMessage(ChatColor.RED + "Failed to transfer: " + response.errorMessage);
                        }
                    }
                };
                restartMenu(getTitle());
                ConfirmWindow.displayTo(player, infoLore, listener, null);
            }
            @Override
            public ItemStack getItem() {
                return infoItem;
            }
        };
        
        list.add(playerSel);
        list.add(amountSel);
        list.add(info);
        return list;
    }
    
    @Override
    protected ItemStack getItemAt(int slot) {
        if (slot == getInfoButtonPosition()) {
            return infoItem;
        }
        switch (slot) {
            case 12:
                if (target != null) {
                    String name = target.getName();
                    targetItem = ItemCreator.of(Material.SKELETON_SKULL, ChatColor.AQUA + "Recipient set.").name(name).build().setSkull(name).make();
                } else {
                    targetItem = ItemCreator.of(Material.SKELETON_SKULL, ChatColor.YELLOW + "Select Recipient").build().make();
                }
                return targetItem;
            case 14:
                if (amount > 0) {
                    amountItem = ItemCreator.of(Material.GOLD_INGOT, ChatColor.AQUA + "Remittance set.").build().make();
                } else {
                    amountItem = ItemCreator.of(Material.GOLD_INGOT, ChatColor.YELLOW + "Select Remittance").build().make();
                }
                return amountItem;
            default:
                return null;
        }
    }
    
    @Override
    protected String[] getInfo() {
        return null;
    }
    
    private void startConversation(Player player, Prompt prompt, ConversationAbandonedListener listener) {
        player.closeInventory();
        Conversation conv = Main.getConversationFactory().withFirstPrompt(prompt).withLocalEcho(true).buildConversation(player);
        conv.addConversationAbandonedListener(listener);
        conv.begin();
    }
}
