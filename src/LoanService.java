import java.util.ArrayList;
import java.util.TreeMap;

public class LoanService {
    private BankDatabase db;

    public LoanService() {
        this.db = BankDatabase.getInstance();
    }

    public String applyLoanString(String accNo, Loan.LoanType loanType,
                                  double amount, double interestRate, int tenureMonths) {
        Accounts acc = db.getAccount(accNo);
        if (acc == null) return "ERROR: Account not found!";
        if (!acc.isActive()) return "ERROR: Account is inactive!";
        if (amount < 10000) return "ERROR: Minimum loan amount is PKR 10,000";
        if (tenureMonths < 3 || tenureMonths > 360) return "ERROR: Tenure must be between 3 and 360 months!";


        // Check existing active loans
        ArrayList<Loan> existing = db.getLoans(accNo);
        int activeLoans = 0;
        for (
                Loan l : existing) {
            if (l.getStatus() == Loan.LoanStatus.Active) activeLoans++;
        }
        if (activeLoans >= 3) return "ERROR: Maximum 3 active loans allowed per account!";

        Loan loan = new Loan(accNo, loanType, amount, interestRate, tenureMonths);
        db.addLoan(loan);
        return "SUCCESS: Loan application submitted! Loan ID: " + loan.getLoanId()
                + " | Monthly EMI: PKR " + String.format("%.2f", loan.getMonthlyInstallment());
    }


    public String approveLoan(String loanId) {
        Loan loan = db.getLoanById(loanId);
        if (loan == null) return "ERROR: Loan not found!";
        if (loan.getStatus() != Loan.LoanStatus.Pending) return "ERROR: Loan is not in PENDING state!";

        loan.setStatus(Loan.LoanStatus.Active);


    // Disburse amount to account
    Accounts acc = db.getAccount(loan.getAccountNumber());
        if(acc !=null) {
            acc.deposit(loan.getPrincipalAmount());
            db.addTransaction(new Transaction(loan.getAccountNumber(),
                    Transaction.TransactionType.LOAN_DISBURSEMENT,
                    loan.getPrincipalAmount(), acc.getBalance(),
                    "Loan disbursement - " + loanId));
        }
        return "SUCCESS: Loan " + loanId + " approved and PKR "
                + String.format("%.2f", loan.getPrincipalAmount()) + " disbursed!";
    }

    public String rejectLoan(String loanId) {
        Loan loan = db.getLoanById(loanId);
        if (loan == null) return "ERROR: Loan not found!";
        if (loan.getStatus() != Loan.LoanStatus.Pending) return "ERROR: Loan is not in PENDING state!";
        loan.setStatus(Loan.LoanStatus.Reject);
        return "SUCCESS: Loan " + loanId + " rejected.";
    }

    public String makePayment(String loanId, String accNo, String pin) {
        Loan loan = db.getLoanById(loanId);
        if (loan == null) return "ERROR: Loan not found!";
        if (loan.getStatus() != Loan.LoanStatus.Active) return "ERROR: Loan is not active!";
        if (!loan.getAccountNumber().equals(accNo)) return "ERROR: Loan does not belong to this account!";

        Accounts acc = db.getAccount(accNo);
        if (acc == null) return "ERROR: Account not found!";
        if (!acc.getPin().equals(pin)) return "ERROR: Invalid PIN!";

        double emi = loan.getMonthlyInstallment();
        if (acc.getBalance() < emi)
            return "ERROR: Insufficient balance for EMI payment! Required: PKR " + String.format("%.2f", emi);

        acc.withdraw(emi);
        boolean closed = loan.makePayment(emi);

        db.addTransaction(new Transaction(accNo, Transaction.TransactionType.LOAN_PAYMENT,
                emi, acc.getBalance(), "EMI payment for " + loanId));

        if (closed) return "SUCCESS: EMI paid! Loan " + loanId + " is now FULLY PAID OFF!";
        return "SUCCESS: EMI of PKR " + String.format("%.2f", emi) + " paid. Remaining: PKR "
                + String.format("%.2f", loan.getRemainingAmount());
    }

    public ArrayList<Loan> getAccountLoans(String accNo) {
        return db.getLoans(accNo);
    }

    public TreeMap<String, ArrayList<Loan>> getAllLoans() {
        return db.getAllLoans();
    }

    public Loan getLoanById(String loanId) {
        return db.getLoanById(loanId);
    }

    public ArrayList<Loan> getPendingLoans() {
        ArrayList<Loan> pending = new ArrayList<>();
        for (ArrayList<Loan> loans : db.getAllLoans().values()) {
            for (Loan loan : loans) {
                if (loan.getStatus() == Loan.LoanStatus.Pending) pending.add(loan);
            }
        }
        return pending;
    }
}