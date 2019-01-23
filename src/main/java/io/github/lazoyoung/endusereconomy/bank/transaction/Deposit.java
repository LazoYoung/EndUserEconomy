package io.github.lazoyoung.endusereconomy.bank.transaction;

import io.github.lazoyoung.endusereconomy.economy.Currency;

import java.sql.Timestamp;

public class Deposit extends Transaction {
    
    private String user;
    
    public Deposit(String user, int amount, int result, Timestamp date, Currency currency) {
        super(amount, result, date, currency);
        this.user = user;
    }
    
    public Deposit(String user, int amount, int result, Timestamp date, Currency currency, String note) {
        super(amount, result, date, currency, note);
        this.user = user;
    }
    
    @Override
    public TransactionType getType() {
        return TransactionType.DEPOSIT;
    }
    
}
