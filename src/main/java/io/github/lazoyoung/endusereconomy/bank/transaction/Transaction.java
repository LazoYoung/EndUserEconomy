package io.github.lazoyoung.endusereconomy.bank.transaction;

import io.github.lazoyoung.database_util.Callback;
import io.github.lazoyoung.endusereconomy.database.BankTable;
import io.github.lazoyoung.endusereconomy.database.Database;
import io.github.lazoyoung.endusereconomy.economy.Currency;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Transaction {
    
    private String note;
    private int amount;
    private int result;
    private Date date;
    private Currency currency;
    
    public Transaction(int amount, int result, Date date, Currency currency) {
        new Transaction(amount, result, date, currency, null);
    }
    
    public Transaction(int amount, int result, Date date, Currency currency, String note) {
        this.note = note;
        this.amount = amount;
        this.result = result;
        this.date = date;
        this.currency = currency;
    }
    
    public static void getRecords(final String user, final Currency currency, int maxCount, Consumer<List<Transaction>> records) {
        BankTable table = (BankTable) Database.getTable(Database.BANK_TRANSACTION);
        Callback<ResultSet, SQLException> callback = (resultSet, thrown) -> {
            if (thrown != null)
                return;
            
            List<Transaction> list = new ArrayList<>();
            try {
                while (resultSet.next()) {
                    TransactionType type = TransactionType.valueOf(resultSet.getString("type").toUpperCase());
                    int amount = resultSet.getInt("amount");
                    int result = resultSet.getInt("result");
                    String sender = resultSet.getString("sender");
                    String receiver = resultSet.getString("receiver");
                    Date date = resultSet.getDate("date");
                    String note = resultSet.getString("note");
                    
                    switch (type) {
                        case DEPOSIT:
                            list.add(new Deposit(user, amount, result, date, currency, note));
                            break;
                        case WITHDRAW:
                            list.add(new Withdraw(user, amount, result, date, currency));
                            break;
                        case TRANSFER: {
                            if (sender.equals(user)) {
                                list.add(new Send(user, sender, amount, result, date, currency, note));
                            } else if (receiver.equals(user)) {
                                list.add(new Receive(user, receiver, amount, result, date, currency, note));
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
    
    public String getNote() {
        return note;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public double getResult() {
        return result;
    }
    
    public Date getDate() {
        return date;
    }
    
    public Currency getCurrency() {
        return currency;
    }
    
}