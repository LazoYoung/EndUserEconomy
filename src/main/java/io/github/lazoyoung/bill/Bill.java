package io.github.lazoyoung.bill;

import io.github.lazoyoung.economy.Currency;

public interface Bill {
    
    Currency getCurrency();
    int getUnit();
    int getId();
    
}