package io.github.lazoyoung.endusereconomy.command;

import io.github.lazoyoung.endusereconomy.economy.Currency;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

abstract class CommandBase implements TabExecutor {
    
    static final UUID consoleId = UUID.randomUUID();
    static Map<UUID, Currency> currencySel = new HashMap<>();
    
    @Nullable
    Currency getCurrency(CommandSender sender) {
        Currency c;
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