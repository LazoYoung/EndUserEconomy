package io.github.lazoyoung;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class EconomyCommand extends CommandData implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
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
                break;
            case "withdraw":
                break;
            default:
                return false;
        }
        
        return true;
    }
    
    private void selectCurrency(CommandSender sender, String[] args) {
        UUID id;
        String economy, currency = null;
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
            economy = args[1].toUpperCase();
        } catch (Exception e) {
            sender.sendMessage("Missing argument.");
            return;
        }
        try {
            currency = args[2].toLowerCase();
        } catch (Exception ignored) {
        
        }
        
        try {
            if (currency == null) {
                currencySel.put(id, new Currency(Economy.valueOf(economy)));
            } else {
                currencySel.put(id, new Currency(Economy.valueOf(economy)));
            }
        } catch (IllegalArgumentException e) {
            // TODO recognize invalid currency input
            sender.sendMessage(e.getMessage());
            return;
        }
        
        sender.sendMessage("Selected: " + economy + "/" + currency);
    }
    
    @SuppressWarnings("deprecation")
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
            
            EconomyHandler eco = c.getEconomy().getHandler();
            
            if (args.length > 1) {
                player = Bukkit.getOfflinePlayer(args[1].toLowerCase());
            }
            if (player == null || !eco.hasAccount(player)) {
                sender.sendMessage("That account does not exist.");
                return;
            }
            
            double bal = eco.getBalance(player, c.getCurrency());
            sender.sendMessage(player.getName() + "'s balance: " + bal);
        }
    }
    
}
