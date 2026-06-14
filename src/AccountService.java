import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;

public class AccountService {
    private BankDatabase db;

    public AccountService() {
        this.db = BankDatabase.getInstance();
    }

    public String createAccount(String ownerName, String cnic, String phone,
                                String address, Accounts.AccountType type,
                                double initialDeposit, String pin) {
        if (ownerName.isEmpty() || cnic.isEmpty() || phone.isEmpty()) {
            return "ERROR: Required fields missing!";
        }
        if (initialDeposit < 1000) {
            return "ERROR: Minimum initial deposit is PKR 1,000";
        }
        if (pin.length() != 4) {
            return "ERROR: PIN must be exactly 4 digits";
        }

        String accNo = db.generateAccountNumber();
        Accounts acc = new Accounts(accNo, ownerName, cnic, phone, address, type, initialDeposit, pin);
        db.addAccount(acc);

        // Record initial deposit transaction
        Transaction txn = new Transaction(accNo, Transaction.TransactionType.DEPOSIT,
                initialDeposit, initialDeposit, "Account opening - Initial deposit");
        db.addTransaction(txn);

        return "SUCCESS:" + accNo;
    }
    public String deposit(String accNo, double amount, String description) {
        Accounts acc = db.getAccount(accNo);
        if (acc == null) return "ERROR: Account not found!";
        if (!acc.isActive()) return "ERROR: Account is inactive!";
        if (amount <= 0) return "ERROR: Invalid amount!";

        acc.deposit(amount);
        Transaction txn = new Transaction(accNo, Transaction.TransactionType.DEPOSIT,
                amount, acc.getBalance(), description.isEmpty() ? "Cash deposit" : description);
        db.addTransaction(txn);
        return "SUCCESS: PKR " + String.format("%.2f", amount) + " deposited. New balance: PKR " + String.format("%.2f", acc.getBalance());
    }
    public String withdraw(String accNo, double amount, String pin) {
        Accounts acc = db.getAccount(accNo);
        if (acc == null) return "ERROR: Account not found!";
        if (!acc.isActive()) return "ERROR: Account is inactive!";
        if (!acc.getPin().equals(pin)) return "ERROR: Invalid PIN!";
        if (amount <= 0) return "ERROR: Invalid amount!";
        if (amount > acc.getBalance()) return "ERROR: Insufficient balance! Available: PKR " + String.format("%.2f", acc.getBalance());

        acc.withdraw(amount);
        Transaction txn = new Transaction(accNo, Transaction.TransactionType.WITHDRAWAL,
                amount, acc.getBalance(), "Cash withdrawal");
        db.addTransaction(txn);
        return "SUCCESS: PKR " + String.format("%.2f", amount) + " withdrawn. Remaining: PKR " + String.format("%.2f", acc.getBalance());
    }

    public String transfer(String fromAccNo, String toAccNo, double amount, String pin) {
        Accounts from = db.getAccount(fromAccNo);
        Accounts to = db.getAccount(toAccNo);

        if (from == null) return "ERROR: Source account not found!";
        if (to == null) return "ERROR: Destination account not found!";
        if (!from.isActive()) return "ERROR: Source account is inactive!";
        if (!to.isActive()) return "ERROR: Destination account is inactive!";
        if (!from.getPin().equals(pin)) return "ERROR: Invalid PIN!";
        if (amount <= 0) return "ERROR: Invalid amount!";
        if (amount > from.getBalance()) return "ERROR: Insufficient balance!";

        from.withdraw(amount);
        to.deposit(amount);

        db.addTransaction(new Transaction(fromAccNo, Transaction.TransactionType.TRANSFER_OUT,
                amount, from.getBalance(), "Transfer to " + toAccNo));
        db.addTransaction(new Transaction(toAccNo, Transaction.TransactionType.TRANSFER_IN,
                amount, to.getBalance(), "Transfer from " + fromAccNo));

        return "SUCCESS: PKR " + String.format("%.2f", amount) + " transferred from " + fromAccNo + " to " + toAccNo;
    }

    public String updateAccount(String accNo, String newPhone, String newAddress) {
        Accounts acc = db.getAccount(accNo);
        if (acc == null) return "ERROR: Account not found!";
        if (!newPhone.isEmpty()) acc.setPhone_no(newPhone);
        if (!newAddress.isEmpty()) acc.setAddress(newAddress);
        return "SUCCESS: Account updated!";
    }

    public String changePin(String accNo, String oldPin, String newPin) {
        Accounts acc = db.getAccount(accNo);
        if (acc == null) return "ERROR: Account not found!";
        if (!acc.getPin().equals(oldPin)) return "ERROR: Old PIN is incorrect!";
        if (newPin.length() != 4) return "ERROR: New PIN must be 4 digits!";
        acc.setPin(newPin);
        return "SUCCESS: PIN changed!";
    }

    public String closeAccount(String accNo, String pin) {
        Accounts acc = db.getAccount(accNo);
        if (acc == null) return "ERROR: Account not found!";
        if (!acc.getPin().equals(pin)) return "ERROR: Invalid PIN!";
        if (acc.getBalance() > 0) return "ERROR: Please withdraw remaining balance of PKR " + String.format("%.2f", acc.getBalance()) + " first!";
        db.deleteAccount(accNo);
        return "SUCCESS: Account " + accNo + " closed!";
    }

    public Accounts getAccount(String accNo) {
        return db.getAccount(accNo);
    }

    public Collection<Accounts> getAllAccounts() {
        return db.getAllAccounts();
    }

    public TreeSet<Accounts> getSortedAccounts() {
        return db.getSortedAccounts();
    }

    public ArrayList<Transaction> getTransactionHistory(String accNo) {
        return db.getTransactions(accNo);
    }
}
