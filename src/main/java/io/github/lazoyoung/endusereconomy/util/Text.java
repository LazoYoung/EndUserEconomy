package io.github.lazoyoung.endusereconomy.util;

import io.github.lazoyoung.endusereconomy.Main;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Text {
    
    private static Map<UUID,Long> actionTick = new HashMap<>();
    
    public static void log(String... message) {
        String prefix = "[" + Main.pluginName + "] ";
        Arrays.stream(message).forEachOrdered(string -> Bukkit.getConsoleSender().sendMessage(prefix + string));
    }
    
    public static void log(ChatColor color, String... message) {
        String prefix = "[" + Main.pluginName + "] ";
        Arrays.stream(message).forEachOrdered(string -> Bukkit.getConsoleSender().sendMessage(color + prefix + string));
    }
    
    public static void actionMessage(Player player, String message) {
        UUID id = player.getUniqueId();
        if (actionTick.containsKey(id)) {
            actionTick.put(id, 0L);
            new BukkitRunnable() {
                @Override
                public void run() {
                    actionMessage(player, message);
                }
            }.runTaskLater(Main.getInstance(), 5L);
        }
        else {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
        }
    }
    
    public static void actionMessage(Player player, String message, long ticks) {
        final Player.Spigot playerSpigot = player.spigot();
        final UUID id = player.getUniqueId();
        final BaseComponent comp = new TextComponent(message);
        
        if (actionTick.containsKey(id)) {
            actionTick.put(id, 0L);
            new BukkitRunnable() {
                @Override
                public void run() {
                    actionMessage(player, message, ticks);
                }
            }.runTaskLater(Main.getInstance(), 5L);
        }
        else {
            actionTick.put(id, ticks);
            new BukkitRunnable() {
                @Override
                public void run() {
                    long timeLeft = actionTick.get(id) - 4;
                    if (timeLeft > 0) {
                        playerSpigot.sendMessage(ChatMessageType.ACTION_BAR, comp);
                        actionTick.put(id, timeLeft);
                    } else {
                        this.cancel();
                        actionTick.remove(id);
                    }
                }
            }.runTaskTimer(Main.getInstance(), 0L, 4L);
        }
    }
    
}