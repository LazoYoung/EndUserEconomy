package io.github.lazoyoung.endusereconomy.command;

import io.github.lazoyoung.endusereconomy.bank.menu.AccountDeposit;
import io.github.lazoyoung.endusereconomy.bank.menu.AccountTransfer;
import io.github.lazoyoung.endusereconomy.bank.menu.AccountView;
import io.github.lazoyoung.endusereconomy.bank.menu.AccountWithdraw;
import io.github.lazoyoung.endusereconomy.economy.Currency;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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
        
        if (!(sender instanceof Player)) {
            sender.sendMessage("You cannot open account menu in console.");
            return true;
        }
        
        switch (args.length) {
            case 0: {
                Arrays.stream(formats).forEach(sender::sendMessage);
                break;
            }
            case 1: {
                switch (args[0].toLowerCase()) {
                    case "view":
                        view(sender, null);
                        break;
                    case "deposit":
                        deposit(sender);
                        break;
                    case "withdraw":
                        withdraw(sender);
                        break;
                    case "transfer":
                        transfer(sender);
                        break;
                    default:
                        return false;
                }
                break;
            }
            case 2: {
                switch (args[0].toLowerCase()) {
                    case "view":
                        view(sender, args[1]);
                        break;
                    case "deposit":
                        return false;
                    case "withdraw":
                        return false;
                    case "transfer":
                        return false;
                    default:
                        return false;
                }

                break;
            }
        }
        return true;
    }
    
    @SuppressWarnings("deprecated")
    private void view(CommandSender sender, String target) {
        Currency currency = getCurrency(sender);
        Player viewer = (Player) sender;
        
        if (currency != null) {
            if (target != null) {
                OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(target);
                if (targetPlayer.hasPlayedBefore()) {
                    AccountView.displayTo(viewer, targetPlayer, currency);
                } else {
                    sender.sendMessage("That player is unknown in this server.");
                }
            } else {
                AccountView.displayTo(viewer, viewer, currency);
            }
        }
    }
    
    private void deposit(CommandSender sender) {
        Currency currency = getCurrency(sender);
        
        if (currency != null) {
            AccountDeposit.displayTo((Player) sender, currency);
        }
    }
    
    private void withdraw(CommandSender sender) {
        Currency currency = getCurrency(sender);
        
        if (currency != null) {
            AccountWithdraw.displayTo((Player) sender, currency);
        }
    }
    
    private void transfer(CommandSender sender) {
        Currency currency = getCurrency(sender);
        
        if (currency != null) {
            new AccountTransfer(currency).displayTo((Player) sender);
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}
