package io.github.lazoyoung.endusereconomy.command;

import io.github.lazoyoung.endusereconomy.bill.Bill;
import io.github.lazoyoung.endusereconomy.bill.BillFactory;
import io.github.lazoyoung.endusereconomy.economy.Currency;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class BillCommand extends CommandBase {
    
    @Override
    public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            String alias = "/" + label;
            sender.sendMessage(new String[] {
                    alias + " setitem <unit> [display-name]\n",
                    alias + " print <unit> [origin]"
            });
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "setitem":
                return setItem(sender, args);
            case "print":
                return print(sender, args);
            default:
                return false;
        }
    }
    
    private boolean setItem(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("Missing argument.");
            return false;
        }
        Player player = (Player) sender;
        Currency currency = getCurrency(sender);
        ItemStack itemStack;
        int unit;
        String[] alias = null;
        
        try {
            unit = Integer.parseInt(args[1]);
            itemStack = player.getInventory().getItemInMainHand().clone();
            if (itemStack == null || itemStack.getType().equals(Material.AIR)) {
                sender.sendMessage("Please hold the item.");
                return false;
            }
            if (currency == null) {
                return true;
            }
            if (args.length > 2) {
                alias = new String[args.length - 2];
                alias = Arrays.asList(args).subList(2, args.length).toArray(alias);
            }
            BillFactory.defineItemBase(currency, unit, itemStack, alias);
            sender.sendMessage("Base item set for " + currency.toString() + ".");
        } catch (NumberFormatException e) {
            sender.sendMessage("Invalid argument: " + args[1]);
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            sender.sendMessage("Failed to record data.");
            return false;
        }
        return true;
    }
    
    private boolean print(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("Missing argument.");
            return false;
        }
        
        Currency c = getCurrency(sender);
        Player p = (Player) sender;
        String origin = p.getName();
        int unit;
        
        try {
            unit = Integer.parseInt(args[1]);
            if (unit <= 0)
                throw new IllegalArgumentException("This is not a positive number.");
        } catch (Exception e) {
            sender.sendMessage("Invalid argument: " + args[1] + ". Possible input: positive number");
            return false;
        }
        if (c == null) {
            return true;
        }
        if (BillFactory.getItemBase(c, unit) == null) {
            sender.sendMessage("Item is not defined. Do \'/bill setitem\'");
            return true;
        }
    
        Consumer<Bill> callback = (bill -> {
            if (bill != null) {
                Map<Integer,ItemStack> map = p.getInventory().addItem(bill.getItem());
                if (!map.isEmpty()) {
                    p.getWorld().dropItem(p.getEyeLocation(), map.get(0));
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Your inventory is full."));
                }
                TextComponent text = new TextComponent("A new bill has been printed.");
                text.setUnderlined(true);
                text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(bill.getUniqueId().toString()).create()));
                sender.spigot().sendMessage(text);
                return;
            }
            sender.sendMessage("Failed to print a bill.");
        });
        BillFactory.printNew(c, unit, origin, callback);
        return true;
    }
    
    /**
     * Requests a list of possible completions for a command argument.
     *
     * @param sender  Source of the command.  For players tab-completing a
     *                command inside of a command block, this will be the player, not
     *                the command block.
     * @param command Command which was executed
     * @param alias   The alias used
     * @param args    The arguments passed to the command, including final
     *                partial argument to be completed and command label
     * @return A List of possible completions for the final argument, or null
     * to default to the command executor
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}
