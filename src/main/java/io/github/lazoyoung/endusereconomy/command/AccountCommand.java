package io.github.lazoyoung.endusereconomy.command;

import io.github.lazoyoung.endusereconomy.bank.AccountRecords;
import io.github.lazoyoung.endusereconomy.economy.Currency;
import org.bukkit.Bukkit;
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
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    
                    switch (args[0].toLowerCase()) {
                        case "view":
                            view(player, player);
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
                else {
                    Arrays.stream(formats).forEach(sender::sendMessage);
                }
                break;
            }
            case 2: {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    
                    switch (args[0].toLowerCase()) {
                        case "view":
                            Player target = Bukkit.getPlayer(args[1]);
                            view(player, target);
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
    
    private void view(Player player, Player target) {
        Currency currency = getCurrency(player);
        
        if (target == null) {
            player.sendMessage("That player is not online.");
        }
        else if (currency != null) {
            AccountRecords.getMenu(target, currency, (menu) -> menu.displayTo(player));
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}
