import java.util.*;

public class BankDatabase {
    HashMap<String, Accounts> accountsMap;
    HashMap<String, ArrayList<Transaction>> transactionMap;
    LinkedList<QueueToken> bankQueue;
    TreeSet<Accounts> sortedAccounts;
    TreeMap<String, ArrayList<Loan>> loanMap;
    Stack<String> operationStack;
    ArrayList<Admin> adminList;
    AccountBST accountBST;
    int accountCenter = 10000;
    static BankDatabase Instance;

    private BankDatabase() {
        accountsMap = new HashMap<>();
        transactionMap = new HashMap<>();
        bankQueue = new LinkedList<>();
        sortedAccounts = new TreeSet<>();
        loanMap = new TreeMap<>();
        operationStack = new Stack<>();
        adminList = new ArrayList<>();
        accountBST = new AccountBST();
        initializeDemoData();
    }

    public static BankDatabase getInstance() {
        if (Instance == null) {
            Instance = new BankDatabase();
        }
        return Instance;
    }
//Account operation
public String generateAccountNumber() {
    return "PKB" + (++accountCenter);
}

    public boolean addAccount(Accounts acc) {
        if (accountsMap.containsKey(acc.getAccountNumber()))
            return false;
        accountsMap.put(acc.getAccountNumber(), acc);
        transactionMap.put(acc.getAccountNumber(), new ArrayList<>());
        sortedAccounts.add(acc);
        accountBST.insert(acc);
        operationStack.push("ADD_ACCOUNT:" + acc.getAccountNumber());
        return true;
    }
    // BST is keyed by balance, which changes on every transaction.
    // Rebuild it on-demand whenever a balance-based query is needed.
    public AccountBST getAccountBST() {
        accountBST.rebuild(accountsMap.values());
        return accountBST;
    }

    public Accounts getAccount(String accNo) {
        return accountsMap.get(accNo);
    }

    public boolean deleteAccount(String accNo) {
        Accounts acc = accountsMap.get(accNo);
        if (acc == null) return false;
        acc.setActive(false);
        operationStack.push("DELETE_ACCOUNT:" + accNo);
        return true;
    }
    public Collection<Accounts> getAllAccounts() {
        return accountsMap.values();
    }

    public TreeSet<Accounts> getSortedAccounts() {
        return sortedAccounts;
    }

    public boolean accountExists(String accNo) {
        return accountsMap.containsKey(accNo);
    }
    // ========== TRANSACTION OPERATIONS ==========

    public void addTransaction(Transaction txn) {
        transactionMap.computeIfAbsent(txn.getAccountNumber(), k -> new ArrayList<>()).add(txn);
        operationStack.push("TRANSACTION:" + txn.getTransactionId());
    }

    public ArrayList<Transaction> getTransactions(String accNo) {
        return transactionMap.getOrDefault(accNo, new ArrayList<>());
    }

    public ArrayList<Transaction> getAllTransactions() {
        ArrayList<Transaction> all = new ArrayList<>();
        for (ArrayList<Transaction> list : transactionMap.values()) {
            all.addAll(list);
        }
        Collections.sort(all);
        return all;
    }
// ========== QUEUE OPERATIONS ==========

    public void enqueueToken(QueueToken token) {
        // Priority-based insertion using LinkedList
        if (bankQueue.isEmpty()) {
            bankQueue.add(token);
            return;
        }
        // Find correct position based on priority (higher priority = front)
        int insertIdx = bankQueue.size();
        for (int i = 0; i < bankQueue.size(); i++) {
            if (token.compareTo(bankQueue.get(i)) < 0) {
                insertIdx = i;
                break;
            }
        }
        bankQueue.add(insertIdx, token);
        operationStack.push("QUEUE_ADD:" + token.getTokenNumber());
    }

    public QueueToken dequeueToken() {
        if (bankQueue.isEmpty()) return null;
        QueueToken token = bankQueue.poll();
        token.markServing();
        operationStack.push("QUEUE_SERVE:" + token.getTokenNumber());
        return token;
    }

    public QueueToken peekQueue() {
        return bankQueue.peek();
    }

    public LinkedList<QueueToken> getQueue() {
        return bankQueue;
    }

    public int getQueueSize() {
        return bankQueue.size();
    }

    // ========== LOAN OPERATIONS ==========

    public void addLoan(Loan loan) {
        loanMap.computeIfAbsent(loan.getAccountNumber(), k -> new ArrayList<>()).add(loan);
        operationStack.push("LOAN_APPLY:" + loan.getLoanId());
    }

    public ArrayList<Loan> getLoans(String accNo) {
        return loanMap.getOrDefault(accNo, new ArrayList<>());
    }

    public TreeMap<String, ArrayList<Loan>> getAllLoans() {
        return loanMap;
    }

    public Loan getLoanById(String loanId) {
        for (ArrayList<Loan> loans : loanMap.values()) {
            for (Loan loan : loans) {
                if (loan.getLoanId().equals(loanId)) return loan;
            }
        }
        return null;
    }
// ========== ADMIN OPERATIONS ==========

    public boolean addAdmin(Admin admin) {
        for (Admin a : adminList) {
            if (a.getUsername().equals(admin.getUsername())) return false;
        }
        adminList.add(admin);
        return true;
    }

    public Admin authenticate(String username, String password) {
        for (Admin admin : adminList) {
            if (admin.getUsername().equals(username)
                    && admin.getPassword().equals(password)
                    && admin.isActive()) {
                return admin;
            }
        }
        return null;
    }

    public ArrayList<Admin> getAllAdmins() {
        return adminList;
    }
    // ========== STACK / AUDIT ==========

    public Stack<String> getOperationStack() {
        return operationStack;
    }

    public String undoLastOperation() {
        if (operationStack.isEmpty())
            return null;
        return operationStack.pop();
    }
// ========== REPORTS ==========

    public TreeMap<Accounts.AccountType, Double> getTotalBalanceByType() {
        TreeMap<Accounts.AccountType, Double> report = new TreeMap<>();
        for (Accounts acc : accountsMap.values()) {
            if (acc.isActive()) {
                report.merge(acc.getAccountType(), acc.getBalance(), Double::sum);
            }
        }
        return report;
    }

    public int getActiveAccountCount() {
        int count = 0;
        for (Accounts acc : accountsMap.values()) {
            if (acc.isActive()) count++;
        }
        return count;
    }

    public double getTotalDeposits() {
        double total = 0;
        for (Accounts acc : accountsMap.values()) {
            if (acc.isActive()) total += acc.getBalance();
        }
        return total;
    }

    public double getTotalLoanAmount() {
        double total = 0;
        for (ArrayList<Loan> loans : loanMap.values()) {
            for (Loan loan : loans) {
                if (loan.getStatus() == Loan.LoanStatus.Active) {
                    total += loan.getRemainingAmount();
                }
            }
        }
        return total;
    }

    // ========== DEMO DATA ==========

    private void initializeDemoData() {
        // Admins
        adminList.add(new Admin("A001", "admin", "admin123", "Super Administrator", Admin.AdminRole.Super_Admin));
        adminList.add(new Admin("A002", "manager", "mgr123", "Bank Manager", Admin.AdminRole.Manager));
        adminList.add(new Admin("A003", "teller1", "teller123", "Teller One", Admin.AdminRole.Teller));

        // Sample accounts
        Accounts a1= new Accounts("PKB10001", "Ali Hassan", "35202-1234567-1",
                "0300-1234567", "Lahore", Accounts.AccountType.SAVINGS, 50000, "1234");
        Accounts a2 = new Accounts("PKB10002", "Sara Khan", "35202-9876543-2",
                "0321-9876543", "Karachi", Accounts.AccountType.CURRENT, 150000, "5678");
        Accounts a3 = new Accounts("PKB10003", "Ahmed Raza", "35202-5556677-3",
                "0333-5556677", "Islamabad", Accounts.AccountType.SAVINGS, 75000, "9012");
        Accounts a4 = new Accounts("PKB10004", "Fatima Malik", "35202-1112233-4",
                "0345-1112233", "Rawalpindi", Accounts.AccountType.FIXED_DEPOSIT, 200000, "3456");

        for (Accounts acc : new Accounts[]{a1, a2, a3, a4}) {
            accountsMap.put(acc.getAccountNumber(), acc);
            transactionMap.put(acc.getAccountNumber(), new ArrayList<>());
            sortedAccounts.add(acc);
        }

        // Sample transactions
        Transaction t1 = new Transaction("PKB10001", Transaction.TransactionType.DEPOSIT, 50000, 50000, "Initial deposit");
        Transaction t2 = new Transaction("PKB10002", Transaction.TransactionType.DEPOSIT, 150000, 150000, "Initial deposit");
        transactionMap.get("PKB10001").add(t1);
        transactionMap.get("PKB10002").add(t2);

        // Sample loan
        Loan loan1 = new Loan("PKB10001", Loan.LoanType.Personal, 100000, 12.0, 24);
        loan1.setStatus(Loan.LoanStatus.Active);
        loanMap.put("PKB10001", new ArrayList<>(Collections.singletonList(loan1)));

        accountCenter = 10004;
    }
}
