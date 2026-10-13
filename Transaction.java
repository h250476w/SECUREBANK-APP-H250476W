
package bank.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {
    private final String transactionId;
    private final String accountNumber;
    private final String type; // DEPOSIT or WITHDRAWAL
    private final BigDecimal amount;
    private final String timestamp;

    public Transaction(String transactionId, String accountNumber, String type, BigDecimal amount, String timestamp) {
        this.transactionId = transactionId;
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public String getAccountNumber() { return accountNumber; }
    public String getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public String getTimestamp() { return timestamp; }

    public String toFileFormat() {
        return transactionId + "|" + accountNumber + "|" + type + "|" + amount.toPlainString() + "|" + timestamp;
    }

    public static Transaction fromFileFormat(String line) {
        String[] parts = line.split("\\|");
        if (parts.length != 5) return null;
        return new Transaction(parts[0], parts[1], parts[2], new BigDecimal(parts[3]), parts[4]);
    }
}
