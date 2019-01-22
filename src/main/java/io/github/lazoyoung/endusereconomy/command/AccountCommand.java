package io.github.lazoyoung.endusereconomy.command;

import io.github.lazoyoung.endusereconomy.bank.AccountRecords;
import io.github.lazoyoung.endusereconomy.economy.Currency;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class AccountCommand extends CommandBase {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String alias = "/" + label;
        String[] formats = new String[] {
                alias + " [view/deposit/withdraw/transfer] [<player>]"
        };
        
        switch (args.length) {
            case 0: {
                Arrays.stream(formats).forEach(sender::sendMessage);
                break;
            }
            case 1: {
                Arrays.stream(formats).forEach(sender::sendMessage);
                break;
            }
            case 2: {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    
                    switch (args[0].toLowerCase()) {
                        case "view":
                            view(player);
                            break;
                        case "deposit":
                        case "withdraw":
                        case "transfer":
                            sender.sendMessage("This menu is not ready.");
                            break;
                        default:
                            return false;
                    }
                }
                break;
            }
        }
        return true;
    }
    
    private void view(Player player) {
        Currency currency = getCurrency(player);
        
        if (currency != null) {
            AccountRecords.getMenu(player, currency, (menu) -> menu.displayTo(player));
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}
