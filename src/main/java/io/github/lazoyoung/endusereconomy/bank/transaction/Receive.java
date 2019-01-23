package io.github.lazoyoung.endusereconomy.bank.transaction;

import io.github.lazoyoung.endusereconomy.economy.Currency;

import java.sql.Timestamp;

public class Receive extends Transaction {
    
    private String user;
    private String sender;
    
    public Receive(String user, String sender, int amount, int result, Timestamp date, Currency currency) {
        super(amount, result, date, currency);
        this.user = user;
        this.sender = sender;
    }
    
    public Receive(String user, String sender, int amount, int result, Timestamp date, Currency currency, String note) {
        super(amount, result, date, currency, note);
        this.user = user;
        this.sender = sender;
    }
    
    public String getUser() {
        return user;
    }
    
    public String getSender() {
        return sender;
    }
    
    @Override
    public TransactionType getType() {
        return TransactionType.TRANSFER;
    }
    
}