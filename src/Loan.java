import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Loan implements Comparable<Loan> {
    static int counter = 2000;
    String loanId;
    String accountNumber;
    LoanType loanType;
    double principalAmount;
    double remainingAmount;
    double interestRate;
    int tenureMonth;
    double monthlyInstallment;
    LoanStatus loanStatus;
    LocalDateTime appliedAt;
    LocalDateTime approveAt;

    public enum LoanType {
        Personal, Home, Auto, Business, Education
    }

    public enum LoanStatus {
        Pending, Approve, Reject, Active, Closed
    }

    public Loan(String accountNumber, LoanType loanType, double principalAmount,
                double interestRate, int tenureMonths) {
        this.loanId = "LN" + (++counter);
        this.accountNumber = accountNumber;
        this.loanType = loanType;
        this.principalAmount = principalAmount;
        this.remainingAmount = principalAmount;
        this.interestRate = interestRate;
        this.tenureMonth = tenureMonths;
        this.loanStatus = LoanStatus.Pending;
        this.appliedAt = LocalDateTime.now();
        // EMI Calculation: EMI = P * r * (1+r)^n / ((1+r)^n - 1)
        double r = (interestRate / 100) / 12;
        double emi = principalAmount * r * Math.pow(1 + r, tenureMonths)
                / (Math.pow(1 + r, tenureMonths) - 1);
        this.monthlyInstallment = Math.round(emi * 100.0) / 100.0;
    }

    public String getLoanId() {
        return loanId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public LoanType getLoanType() {
        return loanType;
    }

    public double getPrincipalAmount() {
        return principalAmount;
    }

    public double getRemainingAmount() {
        return remainingAmount;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public int getTenureMonths() {
        return tenureMonth;
    }

    public double getMonthlyInstallment() {
        return monthlyInstallment;
    }

    public LoanStatus getStatus() {
        return loanStatus;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setStatus(LoanStatus status) {
        this.loanStatus = status;
        if (status == LoanStatus.Approve || status == LoanStatus.Active) {
            this.approveAt = LocalDateTime.now();
        }
    }

    public boolean makePayment(double amount) {
        if (amount >= remainingAmount) {
            remainingAmount = 0;
            loanStatus = LoanStatus.Closed;
        }
        remainingAmount -= amount;
        return false;
    }

    public int compareTo(Loan other) {
        return this.loanId.compareTo(other.loanId);
    }

    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return String.format(
                "Loan: %-8s | Acc: %s | Type: %-10s | Amount: PKR %10.2f | Remaining: PKR %10.2f | EMI: PKR %.2f | Status: %-8s | Applied: %s",
                loanId, accountNumber, loanType, principalAmount, remainingAmount,
                monthlyInstallment, loanStatus, appliedAt.format(fmt)
        );
    }
}

