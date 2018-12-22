package io.github.lazoyoung;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

class CommandData {
    
    static final UUID consoleId = UUID.randomUUID();
    static Map<UUID, Currency> currencySel = new HashMap<>();
    
    Currency getCurrency(CommandSender sender) {
        Currency c = null;
        if (sender instanceof ConsoleCommandSender) {
            c = currencySel.get(consoleId);
        }
        else if (sender instanceof Player) {
            UUID id = ((Player) sender).getUniqueId();
            c = currencySel.get(id);
        }
        else {
            sender.sendMessage("Unsupported CommandSender.");
            return null;
        }
        
        if (c == null) {
            sender.sendMessage("Select a currency first: /eco select <economy> [currency]");
        }
        return c;
    }
    
}