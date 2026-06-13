import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction implements Comparable<Transaction> {
    private static int counter = 1000;
    String transactionId;
    String accountNumber;
     TransactionType type;
     double amount;
     double balanceAfter;
     String description;
     LocalDateTime timestamp;

    public enum TransactionType {
        DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT, LOAN_DISBURSEMENT, LOAN_PAYMENT
    }
    public Transaction(String accountNumber, TransactionType type,
                       double amount, double balanceAfter, String description) {
        this.transactionId = "TXN" + (++counter);
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.description = description;
        this.timestamp = LocalDateTime.now();
    }
    public String getTransactionId() {
        return transactionId;
    }
    public String getAccountNumber() {
        return accountNumber;
    }
    public TransactionType getType() {
        return type;
    }
    public double getAmount() {
        return amount;
    }
    public double getBalanceAfter() {
        return balanceAfter;
    }
    public String getDescription() {
        return description;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public int compareTo(Transaction other) {
        return this.timestamp.compareTo(other.timestamp);
    }
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return String.format(
                "%-12s | %-20s | %-15s | PKR %10.2f | Balance: PKR %10.2f | %s | %s",
                transactionId, accountNumber, type, amount, balanceAfter,
                timestamp.format(fmt), description
        );
    }
}
