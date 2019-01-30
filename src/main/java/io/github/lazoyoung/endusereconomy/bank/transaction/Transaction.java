package io.github.lazoyoung.endusereconomy.bank.transaction;

import io.github.lazoyoung.database_util.Callback;
import io.github.lazoyoung.endusereconomy.database.BankTable;
import io.github.lazoyoung.endusereconomy.database.Database;
import io.github.lazoyoung.endusereconomy.economy.Currency;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public abstract class Transaction {
    
    private String note;
    private int amount;
    private int result;
    private Timestamp date;
    private Currency currency;
    
    Transaction(int amount, int result, Timestamp date, Currency currency) {
        this(amount, result, date, currency, null);
    }
    
    Transaction(int amount, int result, Timestamp date, Currency currency, String note) {
        this.note = note;
        this.amount = amount;
        this.result = result;
        this.date = date;
        this.currency = currency;
    }
    
    public static void getRecordsReversed(String user, Currency currency, int maxCount, Consumer<List<Transaction>> records) {
        getRecords(user, currency, maxCount, (list) -> {
            Collections.reverse(list);
            records.accept(list);
        });
    }
    
    public static void getRecords(String user, final Currency currency, int maxCount, Consumer<List<Transaction>> records) {
        final String userName = user.toLowerCase();
        BankTable table = (BankTable) Database.getTable(Database.BANK_TRANSACTION);
        Callback<ResultSet, SQLException> callback = (resultSet, thrown) -> {
            if (thrown != null)
                return;
            
            try {
                List<Transaction> list = new ArrayList<>();
                while (resultSet.next()) {
                    TransactionType type = TransactionType.valueOf(resultSet.getString("type").toUpperCase());
                    int amount = resultSet.getInt("amount");
                    int senderResult = resultSet.getInt("senderResult");
                    int receiverResult = resultSet.getInt("receiverResult");
                    String sender = resultSet.getString("sender");
                    String receiver = resultSet.getString("receiver");
                    Timestamp date = resultSet.getTimestamp("date");
                    String note = resultSet.getString("note");
                    switch (type) {
                        case DEPOSIT:
                            list.add(new Deposit(userName, amount, senderResult, date, currency, note));
                            break;
                        case WITHDRAW:
                            list.add(new Withdraw(userName, amount, senderResult, date, currency));
                            break;
                        case TRANSFER: {
                            if (sender.equals(userName)) {
                                list.add(new Send(userName, sender, amount, senderResult, date, currency, note));
                            } else if (receiver.equals(userName)) {
                                list.add(new Receive(userName, receiver, amount, receiverResult, date, currency, note));
                            }
                            break;
                        }
                    }
                }
                records.accept(list);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        };
        table.getRecords(user, currency, maxCount, callback);
    }
    
    public abstract TransactionType getType();
    
    public String getNote() {
        return note;
    }
    
    public int getAmount() {
        return amount;
    }
    
    public int getResult() {
        return result;
    }
    
    public Timestamp getDate() {
        return date;
    }
    
    public Currency getCurrency() {
        return currency;
    }
    
}