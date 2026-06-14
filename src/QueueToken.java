import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class QueueToken implements Comparable<QueueToken> {
    private static int counter = 0;

    String tokenNumber;
    String customerName;
    String accountNumber;
    ServiceType serviceType;
    Priority priority;
    TokenStatus status;
    LocalDateTime issuedAt;
    LocalDateTime servedAt;

    public enum ServiceType {
        Deposit, Withdrawal, Account_Opening, Loan_Inquiry,
        Account_Update, Transfer, General_Inquiry
    }

    public enum Priority {
        Normal(1), Senior_Citizen(2), VIP(3), Emergency(4);
        private final int level;

        Priority(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
    }

    public enum TokenStatus {
        Waiting, Serving, Completed, Cancelled
    }
    public QueueToken(String customerName, String accountNumber,
                      ServiceType serviceType, Priority priority) {
        this.tokenNumber = String.format("T%04d", ++counter);
        this.customerName = customerName;
        this.accountNumber = accountNumber;
        this.serviceType = serviceType;
        this.priority = priority;
        this.status = TokenStatus.Waiting;
        this.issuedAt = LocalDateTime.now();
    }
    public String getTokenNumber() {
        return tokenNumber;
    }
    public String getCustomerName() {
        return customerName;
    }
    public String getAccountNumber() {
        return accountNumber;
    }
    public ServiceType getServiceType() {
        return serviceType;
    }
    public Priority getPriority() {
        return priority;
    }
    public TokenStatus getStatus() {
        return status;
    }
    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void markServing() {
        this.status = TokenStatus.Serving;
        this.servedAt = LocalDateTime.now();
    }
    public void markCompleted() {
        this.status = TokenStatus.Completed;
    }
    public void markCancelled() {
        this.status = TokenStatus.Cancelled;
    }

    public long getWaitMinutes() {
        LocalDateTime end = (servedAt != null) ? servedAt : LocalDateTime.now();
        return java.time.Duration.between(issuedAt, end).toMinutes();
    }
    @Override
    public int compareTo(QueueToken other) {
        // Higher priority first
        int prioCmp = Integer.compare(other.priority.getLevel(), this.priority.getLevel());
        if (prioCmp != 0) return prioCmp;
        return this.issuedAt.compareTo(other.issuedAt);
    }
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss");
        return String.format(
                "Token: %s | Customer: %-20s | Service: %-18s | Priority: %-14s | Status: %-10s | Issued: %s",
                tokenNumber, customerName, serviceType, priority, status, issuedAt.format(fmt)
        );
    }
}