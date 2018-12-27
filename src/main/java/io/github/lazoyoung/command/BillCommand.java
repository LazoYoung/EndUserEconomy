package io.github.lazoyoung.command;

import io.github.lazoyoung.bill.Bill;
import io.github.lazoyoung.bill.BillFactory;
import io.github.lazoyoung.economy.Currency;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.List;

public class BillCommand extends CommandBase {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            String alias = "/" + label;
            sender.sendMessage(alias + " print <unit> [origin]");
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "print":
                return print(sender, args);
            default:
                return false;
        }
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
        ItemStack item = p.getInventory().getItemInMainHand();
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
        if (item == null || item.getType().equals(Material.AIR)) {
            sender.sendMessage("Please try again while holding the item you want to print of.");
            return true;
        }
    
        try {
            Bill bill = new BillFactory(c, unit).printNew(origin);
            TextComponent text = new TextComponent("A new bill has been printed.");
            text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(bill.getUniqueId().toString()).create()));
            sender.spigot().sendMessage(text);
        } catch (SQLException e) {
            e.printStackTrace();
            sender.sendMessage("Failed to print a bill.");
        }
    
        // TODO implement item
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
