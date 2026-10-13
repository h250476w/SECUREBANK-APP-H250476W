
package bank.model;

import java.math.BigDecimal;

public class Account {
    private final String accountNumber;
    private final String ownerUsername;
    private BigDecimal balance;

    public Account(String accountNumber, String ownerUsername, BigDecimal balance) {
        this.accountNumber = accountNumber;
        this.ownerUsername = ownerUsername;
        this.balance = balance;
    }

    public String getAccountNumber() { return accountNumber; }
    public String getOwnerUsername() { return ownerUsername; }
    public BigDecimal getBalance() { return balance; }

    public synchronized void deposit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    public synchronized boolean withdraw(BigDecimal amount) {
        if (this.balance.compareTo(amount) >= 0) {
            this.balance = this.balance.subtract(amount);
            return true;
        }
        return false;
    }

    public String toFileFormat() {
        return accountNumber + "|" + ownerUsername + "|" + balance.toPlainString();
    }

    public static Account fromFileFormat(String line) {
        String[] parts = line.split("\\|");
        if (parts.length != 3) return null;
        return new Account(parts[0], parts[1], new BigDecimal(parts[2]));
    }
}