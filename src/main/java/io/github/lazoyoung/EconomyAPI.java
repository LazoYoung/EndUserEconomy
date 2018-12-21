package io.github.lazoyoung;

import java.util.HashMap;
import java.util.Map;

public class EconomyAPI {
    private static Map<EconomyType, Economy> economyMap = new HashMap<>();
    
    public static Economy get(EconomyType type) {
        return economyMap.get(type);
    }
    
    static void register(EconomyType type, Economy economy) {
        economyMap.put(type, economy);
    }
    
}
