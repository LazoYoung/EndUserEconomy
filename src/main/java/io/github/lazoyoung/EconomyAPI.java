package io.github.lazoyoung;

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
