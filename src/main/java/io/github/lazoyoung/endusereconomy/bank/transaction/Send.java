package io.github.lazoyoung.endusereconomy.bank.transaction;

import io.github.lazoyoung.endusereconomy.economy.Currency;

import java.sql.Timestamp;

public class Send extends Transaction {
    
    private String user;
    private String receiver;
    
    public Send(String user, String receiver, int amount, int result, Timestamp date, Currency currency) {
        super(amount, result, date, currency);
        this.user = user;
        this.receiver = receiver;
    }
    
    public Send(String user, String receiver, int amount, int result, Timestamp date, Currency currency, String note) {
        super(amount, result, date, currency, note);
        this.user = user;
        this.receiver = receiver;
    }
    
    public String getUser() {
        return user;
    }
    
    public String getReceiver() {
        return receiver;
    }
    
    @Override
    public TransactionType getType() {
        return TransactionType.TRANSFER;
    }
    
}
