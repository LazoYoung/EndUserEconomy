package io.github.lazoyoung.endusereconomy.command;

import io.github.lazoyoung.endusereconomy.Config;
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
import java.util.*;
import java.util.function.Consumer;

public class BillCommand extends CommandBase {
    
    @Override
    public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            String alias = "/" + label;
            sender.sendMessage(new String[] {
                    alias + " setitem <unit> [display-name]\n",
                    alias + " print <unit> [origin]\n",
                    alias + " discard"
            });
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "setitem":
                return setItem(sender, args);
            case "print":
                return print(sender, args);
            case "discard":
                return discard(sender);
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
                sender.sendMessage("Please hold the item while executing this.");
                return true;
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
                text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(String.valueOf(bill.getId())).create()));
                sender.spigot().sendMessage(text);
                return;
            }
            sender.sendMessage("Failed to print a bill.");
        });
        BillFactory.printNew(c, unit, origin, callback);
        return true;
    }
    
    // TODO log the action for a week
    private boolean discard(final CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        
        final Player player = (Player) sender;
        final ItemStack target = player.getInventory().getItemInMainHand();
        Consumer<Bill> callback = (bill -> {
            if (bill == null) {
                sender.sendMessage("That is not a valid item.");
                return;
            }
            bill.discard((succeed) -> {
                if (succeed) {
                    player.getInventory().remove(target);
                    sender.sendMessage("Bill has been discarded.");
                    return;
                }
                sender.sendMessage("Failed to discard.");
            });
        });
        BillFactory.getBillFromItem(target, callback);
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
        List<String> comps = new ArrayList<>();
        String arg = args[0].toLowerCase();
        
        if (args.length == 1) {
            if ("setitem".startsWith(arg))
                comps.add("setitem");
            if ("print".startsWith(arg))
                comps.add("print");
            return comps;
        }
        
        Currency currency = getCurrency(sender);
    
        if (currency == null) {
            return comps;
        }
        if (arg.equals("setitem")) {
            switch (args.length) {
                case 2:
                    comps.addAll(Config.BILL.get().getConfigurationSection(currency.toString()).getKeys(false));
                    break;
                case 3:
                    comps.add("[display name]");
                    break;
                default:
                    return comps;
            }
        }
        else if (arg.equals("print")) {
            switch (args.length) {
                case 2:
                    Set<String> units = Config.BILL.get().getConfigurationSection(currency.toString()).getKeys(false);
                    if (units != null) {
                        comps.addAll(units);
                    }
                    break;
                case 3:
                    comps.add("[origin]");
                    break;
                default:
                    return comps;
            }
        }
        else {
            return comps;
        }
        
        return comps;
    }
}
