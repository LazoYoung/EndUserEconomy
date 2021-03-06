package io.github.lazoyoung.endusereconomy.command;

import io.github.lazoyoung.endusereconomy.economy.Currency;
import io.github.lazoyoung.endusereconomy.economy.Economy;
import io.github.lazoyoung.endusereconomy.economy.handler.EconomyHandler;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class EconomyCommand extends CommandBase {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(new String[] {
                    "/economy select <economy> [currency]\n",
                    ChatColor.STRIKETHROUGH + "/economy balance [player]\n",
                    ChatColor.STRIKETHROUGH + "/economy <deposit/withdraw/set> <player> <amount>"
            });
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "select":
                selectCurrency(sender, args);
                break;
            case "balance":
                balance(sender, args);
                break;
            case "deposit":
            case "withdraw":
            case "set":
                return setBalance(sender, args);
            default:
                return false;
        }
        return true;
    }
    
    private void selectCurrency(CommandSender sender, String[] args) {
        UUID id;
        String economyArg, currency = null;
        if (sender instanceof Player) {
            id = ((Player) sender).getUniqueId();
        }
        else if (sender instanceof ConsoleCommandSender) {
            id = consoleId;
        }
        else {
            sender.sendMessage("Unsupported CommandSender.");
            return;
        }
        
        try {
            economyArg = args[1].toUpperCase();
        } catch (Exception e) {
            sender.sendMessage("Missing parameter.");
            return;
        }
        try {
            currency = args[2].toLowerCase();
        } catch (Exception ignored) {
        
        }
        
        try {
            Economy economy = Economy.valueOf(economyArg);
            EconomyHandler handler = EconomyHandler.getInstance(economy);
            
            if(handler == null) {
                sender.sendMessage("That economy is not available.");
                return;
            }
            if (currency == null) {
                currencySel.put(id, new Currency(economy));
            } else {
                currencySel.put(id, new Currency(economy, currency));
            }
        } catch (IllegalArgumentException e) {
            sender.sendMessage("Unknown economy: " + economyArg);
            return;
        }
        
        sender.sendMessage("Selected: " + economyArg + "/" + currency);
    }
    
    @Deprecated
    private void balance(CommandSender sender, String[] args) {
        OfflinePlayer player = null;
        Currency c = getCurrency(sender);
        
        if (c != null) {
            if (sender instanceof Player) {
                player = ((OfflinePlayer) sender);
            } else if (args.length < 2) {
                sender.sendMessage("Please define the player.");
                return;
            }
            
            EconomyHandler eco = c.getEconomyHandler();
            if (args.length > 1) {
                player = Bukkit.getOfflinePlayer(args[1].toLowerCase());
            }
            if (player == null || !eco.hasAccount(player)) {
                sender.sendMessage("That account does not exist.");
                return;
            }
            double bal = eco.getBalance(player, c.getName());
            sender.sendMessage(player.getName() + "'s balance: " + bal);
            return;
        }
        sender.sendMessage("Please select currency: /eco select <economy> [currency]");
    }
    
    @Deprecated
    private boolean setBalance(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("Missing parameter.");
            return false;
        }
        
        Currency c = getCurrency(sender);
        if (c != null) {
            double amount;
            OfflinePlayer player;
            EconomyHandler eco = c.getEconomyHandler();
            EconomyResponse result;
            
            try {
                amount = Double.parseDouble(args[2]);
                player = Bukkit.getOfflinePlayer(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage("Please define the amount of balance.");
                return false;
            }
            
            switch(args[0].toLowerCase()) {
                case "deposit":
                    result = eco.deposit(player, c.getName(), amount);
                    break;
                case "withdraw":
                    result = eco.withdraw(player, c.getName(), amount);
                    break;
                case "set":
                    result = eco.setBalance(player, c.getName(), amount);
                    break;
                default:
                    sender.sendMessage("Invalid parameter.");
                    return false;
            }
            if (result.transactionSuccess()) {
                sender.sendMessage("Transaction succeed. New balance: " + result.balance);
                return true;
            }
            sender.sendMessage("Transaction failed: " + result.errorMessage);
            return true;
        }
        sender.sendMessage("Please select currency: /eco select <economy> [currency]");
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
            if ("select".startsWith(arg))
                comps.add("select");
            if ("balance".startsWith(arg))
                comps.add("balance");
            if ("deposit".startsWith(arg))
                comps.add("deposit");
            if ("withdraw".startsWith(arg))
                comps.add("withdraw");
            if ("set".startsWith(arg))
                comps.add("set");
            return comps;
        }
        
        if (arg.equals("select")) {
            switch (args.length) {
                case 2:
                    Arrays.stream(Economy.values()).forEach(economy -> {
                        if (economy.toString().startsWith(args[1].toUpperCase()))
                            comps.add(economy.toString());
                    });
                    break;
                case 3:
                    if (args[2].isEmpty())
                        comps.add("[economy]");
                    break;
                default:
                    return comps;
            }
            return comps;
        }
        
        Currency currency = getCurrency(sender);
        
        if (currency == null) {
            return comps;
        }
        if (arg.equals("balance") && args.length == 2) {
            Bukkit.getServer().getOnlinePlayers().forEach(player -> {
                if (player.getName().toLowerCase().startsWith(args[1].toLowerCase()))
                    comps.add(player.getName());
            });
        }
        else if (arg.equals("deposit") || arg.equals("withdraw") || arg.equals("set")) {
            switch (args.length) {
                case 2:
                    String playerArg = args[1].toLowerCase();
                    Bukkit.getServer().getOnlinePlayers().forEach(player -> {
                        if (player.getName().toLowerCase().startsWith(playerArg))
                            comps.add(player.getName());
                    });
                    break;
                case 3:
                    if (args[2].isEmpty())
                        comps.add("<amount>");
                    break;
                default:
                    return comps;
            }
        }
        return comps;
    }
}
