package io.github.lazoyoung.endusereconomy.bank.transaction;

import io.github.lazoyoung.endusereconomy.economy.Currency;

import java.sql.Date;

public class Withdraw extends Transaction {
    
    private String user;
    
    public Withdraw(String user, int amount, int result, Date date, Currency currency) {
        super(amount, result, date, currency);
        this.user = user;
    }
    
    public Withdraw(String user, int amount, int result, Date date, Currency currency, String note) {
        super(amount, result, date, currency, note);
        this.user = user;
    }
    
}
