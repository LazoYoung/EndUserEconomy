package io.github.lazoyoung.endusereconomy.command;

import io.github.lazoyoung.endusereconomy.Config;
import io.github.lazoyoung.endusereconomy.bill.Bill;
import io.github.lazoyoung.endusereconomy.bill.BillFactory;
import io.github.lazoyoung.endusereconomy.database.BillTable;
import io.github.lazoyoung.endusereconomy.database.Database;
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

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class BillCommand extends CommandBase {
    
    @Override
    public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
        String alias = "/" + label;
        String[] formats = new String[]{
                alias + " setitem <unit> [display-name]",
                alias + " print <unit> [origin]",
                alias + " discard",
                alias + " record <inspect/clear> <all/this>"
        };
        Object[] arguments = new Object[2];
        ItemStack targetItem = null;
        
        if (args.length > 0) {
            String subCmd = args[0].toLowerCase();
            switch (subCmd) {
                case "record":
                case "setitem":
                case "discard": {
                    if (subCmd.equals("record") && (args.length < 3 || !args[2].equalsIgnoreCase("this"))) {
                        break;
                    }
                    if (!(sender instanceof Player)) {
                        sender.sendMessage("Only players can use this command.");
                        return true;
                    }
                    targetItem = ((Player) sender).getInventory().getItemInMainHand();
                    if (targetItem == null || targetItem.getType().equals(Material.AIR)) {
                        sender.sendMessage("Please hold the item while executing this.");
                        return true;
                    }
                    break;
                }
            }
            try {
                switch (subCmd) {
                    case "setitem":
                    case "print": {
                        if (args.length >= 2) {
                            arguments[0] = Integer.parseInt(args[1]);
                            if (((int) arguments[0]) <= 0) {
                                sender.sendMessage("Invalid range of unit.");
                                return false;
                            }
                        }
                        if (args.length >= 3) {
                            arguments[1] = args[2];
                        }
                        break;
                    }
                    case "record": {
                        if (args.length >= 2) {
                            String act = args[1].toLowerCase();
                            if (!(act.equals("inspect") || act.equals("clear"))) {
                                throw new IllegalArgumentException();
                            }
                            arguments[0] = act;
                        }
                        if (args.length >= 3) {
                            String act = args[2].toLowerCase();
                            if (!(act.equals("all") || act.equals("this"))) {
                                throw new IllegalArgumentException();
                            }
                            arguments[1] = act;
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                sender.sendMessage("Invalid argument.");
                return false;
            }
        }
        
        switch(args.length) {
            case 0: {
                Arrays.stream(formats).forEach(sender::sendMessage);
                break;
            }
            case 1: {
                switch (args[0].toLowerCase()) {
                    case "setitem":
                        sender.sendMessage(formats[0]);
                        break;
                    case "print":
                        sender.sendMessage(formats[1]);
                        break;
                    case "discard":
                        discard((Player) sender, targetItem);
                        break;
                    case "record":
                        sender.sendMessage(formats[3]);
                        break;
                    default:
                        return false;
                }
                break;
            }
            case 2: {
                switch (args[0].toLowerCase()) {
                    case "setitem":
                        setItem((Player) sender, targetItem, (int) arguments[0], null);
                        break;
                    case "print":
                        print((Player) sender, (int) arguments[0], null);
                        break;
                    case "record":
                        sender.sendMessage(formats[3]);
                        break;
                    default:
                        return false;
                }
                break;
            }
            case 3: {
                switch (args[0].toLowerCase()) {
                    case "setitem": {
                        setItem((Player) sender, targetItem, (int) arguments[0], Arrays.copyOfRange(args, 2, args.length));
                        break;
                    }
                    case "print":
                        print((Player) sender, (int) arguments[0], (String) arguments[1]);
                        break;
                    case "record": {
                        String act = String.valueOf(arguments[0]);
                        if (act.equals("inspect")) {
                            inspect(sender, (String) arguments[1], targetItem);
                        } else if (act.equals("clear")) {
                            clear(sender, (String) arguments[1], targetItem);
                        }
                        break;
                    }
                    default:
                        return false;
                }
                break;
            }
        }
        return true;
    }
    
    private void setItem(Player player, ItemStack item, int unit, @Nullable String[] displayName) {
        Currency currency = getCurrency(player);
        String[] alias = null;
        
        try {
            if (currency != null) {
                BillFactory.defineItemBase(currency, unit, item, alias);
                player.sendMessage("Base item set for " + currency.toString() + ".");
            }
        } catch (IOException e) {
            e.printStackTrace();
            player.sendMessage("Failed to record data.");
        }
    }
    
    private void print(Player player, int unit, @Nullable String origin) {
        Currency c = getCurrency(player);
        
        if (origin == null) {
            origin = player.getName();
        }
        if (c != null) {
            if (BillFactory.getItemBase(c, unit) == null) {
                player.sendMessage("Item is not defined. Do \'/bill setitem\'");
                return;
            }
    
            Consumer<Bill> callback = (bill -> {
                if (bill != null) {
                    Map<Integer, ItemStack> map = player.getInventory().addItem(bill.getItem());
                    if (!map.isEmpty()) {
                        player.getWorld().dropItem(player.getEyeLocation(), map.get(0));
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Your inventory is full."));
                    }
                    TextComponent text = new TextComponent("A new bill has been printed.");
                    text.setUnderlined(true);
                    text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(String.valueOf(bill.getId())).create()));
                    player.spigot().sendMessage(text);
                    return;
                }
                player.sendMessage("Failed to print a bill.");
            });
            BillFactory.printNew(c, unit, origin, callback);
        }
    }
    
    private void discard(final Player player, ItemStack item) {
        Consumer<Bill> callback = (bill -> {
            if (bill == null) {
                player.sendMessage("This is not a valid item.");
                return;
            }
            bill.discard(player.getName(), (succeed) -> {
                if (succeed) {
                    player.getInventory().remove(item);
                    player.sendMessage("Bill has been discarded.");
                    return;
                }
                player.sendMessage("Failed to discard.");
            });
        });
        BillFactory.getBillFromItem(item, callback);
    }
    
    private void inspect(CommandSender sender, String scope, @Nullable ItemStack item) {
        // TODO implement record inspection
        sender.sendMessage("This is not implemented yet.");
    }
    
    private void clear(final CommandSender sender, String scope, @Nullable ItemStack item) {
        if (scope.equals("this") && item != null) {
            BillFactory.getBillFromItem(item, (bill -> {
                if (bill.isExpired()) {
                    BillTable table = (BillTable) Database.getTable(Database.BILL_REC);
                    table.clearRecord(bill.getId(), (succeed) -> {
                        if (succeed) {
                            sender.sendMessage("Bill record has been deleted.");
                        } else {
                            sender.sendMessage("Failed to delete bill record.");
                        }
                    });
                    return;
                }
                sender.sendMessage("Bill is still valid. Use \'/bill discard\' and try again.");
            }));
        }
        else if (scope.equals("all")) {
            BillTable table = (BillTable) Database.getTable(Database.BILL_REC);
            table.clearRecords(7, (count, thrown) -> {
                if (thrown == null) {
                    sender.sendMessage("Cleared " + count + " records expired for more than 7 days.");
                } else {
                    sender.sendMessage("Failed to delete bill record.");
                }
            });
        }
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
            if ("discard".startsWith(arg))
                comps.add("discard");
            if ("record".startsWith(arg))
                comps.add("record");
            return comps;
        }
        
        switch (arg) {
            case "setitem": {
                switch (args.length) {
                    case 2:
                        Currency currency = getCurrency(sender);
                        if (currency != null) {
                            comps.addAll(Config.BILL.get().getConfigurationSection(currency.toString()).getKeys(false));
                        } else {
                            return comps;
                        }
                        break;
                    case 3:
                        comps.add("[display name]");
                        break;
                    default:
                        return comps;
                }
                break;
            }
            case "print": {
                switch (args.length) {
                    case 2: {
                        Currency currency = getCurrency(sender);
                        Set<String> units = null;
                        if (currency != null) {
                            units = Config.BILL.get().getConfigurationSection(currency.toString()).getKeys(false);
                            if (units != null) {
                                comps.addAll(units);
                            }
                        } else {
                            return comps;
                        }
                        break;
                    }
                    case 3:
                        comps.add("[origin]");
                        break;
                    default:
                        return comps;
                }
                break;
            }
            case "record": {
                switch (args.length) {
                    case 2:
                        comps.add("inspect");
                        comps.add("clear");
                        break;
                    case 3:
                        comps.add("all");
                        comps.add("this");
                        break;
                    default:
                        return comps;
                }
                break;
            }
            default:
                return comps;
        }
        
        return comps;
    }
}
