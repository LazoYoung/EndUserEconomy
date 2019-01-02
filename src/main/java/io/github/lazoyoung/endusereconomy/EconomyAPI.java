package io.github.lazoyoung.endusereconomy;

import io.github.lazoyoung.endusereconomy.economy.Economy;
import io.github.lazoyoung.endusereconomy.economy.handler.EconomyHandler;

import java.util.HashMap;
import java.util.Map;

public class EconomyAPI {
    private static Map<Economy, EconomyHandler> economyMap = new HashMap<>();
    
    public static EconomyHandler get(Economy type) {
        return economyMap.get(type);
    }
    
    static void register(Economy type, EconomyHandler economyHandler) {
        economyMap.put(type, economyHandler);
    }
}
