import java.util.*;

public class ReportService {
    private BankDatabase db;

    public ReportService() {
        this.db = BankDatabase.getInstance();
    }

    // TreeMap report: Total balance grouped by account type
    public TreeMap<Accounts.AccountType, Double> getBalanceByAccountType() {
        return db.getTotalBalanceByType();
    }

    public TreeSet<Accounts> getSortedAccounts() {
        return db.getSortedAccounts();
    }

    // Summary report
    public String getBankSummary() {
        StringBuilder sb = new StringBuilder();

        sb.append("\n╔══════════════════════════════════════════════════════════════════╗\n");
        sb.append("║              SMARTBANK - SUMMARY REPORT                          ║\n");
        sb.append("╚══════════════════════════════════════════════════════════════════╝\n");
        sb.append(String.format("  Total Active Accounts   : %d%n", db.getActiveAccountCount()));
        sb.append(String.format("  Total Deposits (Balance): PKR %.2f%n", db.getTotalDeposits()));
        sb.append(String.format("  Total Loans Outstanding  : PKR %.2f%n", db.getTotalLoanAmount()));
        sb.append(String.format("  Customers in Queue       : %d%n", db.getQueueSize()));
        sb.append(String.format("  Operations Logged        : %d%n", db.getOperationStack().size()));

        sb.append("\n  Balance By Account Type (TreeMap - Sorted):\n");
        TreeMap<Accounts.AccountType, Double> byType = db.getTotalBalanceByType();
        for (Map.Entry<Accounts.AccountType, Double> entry : byType.entrySet()) {
            sb.append(String.format("    %-20s : PKR %.2f%n", entry.getKey(), entry.getValue()));
        }

        sb.append("\n  Pending Loan Applications:\n");
        int pending = 0;
        for (ArrayList<Loan> loans : db.getAllLoans().values()) {
            for (Loan loan : loans) {
                if (loan.getStatus() == Loan.LoanStatus.Pending) pending++;
            }
        }
        sb.append(String.format("    Pending: %d%n", pending));
        return sb.toString();
    }
    // Transaction report for an account
    public String getAccountStatement(String accNo) {
        Accounts acc = db.getAccount(accNo);
        if (acc == null) return "ERROR: Account not found!";

        ArrayList<Transaction> txns = db.getTransactions(accNo);
        StringBuilder sb = new StringBuilder();
        sb.append("\n╔═══════════════════════════════════════════════════════════════════════════════════╗\n");
        sb.append(String.format("║  ACCOUNT STATEMENT: %-60s ║%n", accNo));
        sb.append(String.format("║  Owner: %-72s ║%n", acc.getOwnerName()));
        sb.append("╚═══════════════════════════════════════════════════════════════════════════════════╝\n");

        if (txns.isEmpty()) {
            sb.append("  No transactions found.\n");
        } else {
            Collections.sort(txns);
            for (Transaction t : txns) {
                sb.append("  ").append(t).append("\n");
            }
        }
        sb.append(String.format("%n  Current Balance: PKR %.2f%n", acc.getBalance()));
        return sb.toString();
    }

    // Audit trail using Stack
    public String getAuditTrail(int lastN) {
        Stack<String> ops = db.getOperationStack();
        StringBuilder sb = new StringBuilder();
        sb.append("\n  === AUDIT TRAIL (Last Operations) ===\n");
        List<String> list = new ArrayList<>(ops);
        int start = Math.max(0, list.size() - lastN);
        for (int i = list.size() - 1; i >= start; i--) {
            sb.append("  [").append(list.size() - i).append("] ").append(list.get(i)).append("\n");
        }
        return sb.toString();
    }

    // ── Custom Merge Sort: Accounts sorted by balance ──
    public ArrayList<Accounts> getAccountsSortedByBalance(boolean ascending) {
        ArrayList<Accounts> list = new ArrayList<>(db.getAllAccounts());
        SortUtil.mergeSortByBalance(list, ascending);
        return list;
    }

    public String getAccountsSortedByBalanceReport(boolean ascending) {
        ArrayList<Accounts> list = getAccountsSortedByBalance(ascending);
        StringBuilder sb = new StringBuilder();
        sb.append("\n  === ACCOUNTS SORTED BY BALANCE (Merge Sort - ")
                .append(ascending ? "Ascending" : "Descending").append(") ===\n");
        int i = 1;
        for (Accounts a : list) sb.append("  ").append(i++).append(". ").append(a).append("\n");
        return sb.toString();
    }
    // ── Custom BST: search by exact balance ──
    public Accounts findAccountByBalance(double balance) {
        return db.getAccountBST().search(balance);
    }

    // ── Custom BST: range query (e.g. accounts with balance between X and Y) ──
    public ArrayList<Accounts> getAccountsInBalanceRange(double min, double max) {
        return db.getAccountBST().rangeQuery(min, max);
    }

    // ── Custom BST: in-order traversal + min/max/height ──
    public String getBSTReport() {
        AccountBST bst = db.getAccountBST();
        StringBuilder sb = new StringBuilder();
        sb.append("\n  === ACCOUNT BST REPORT (Keyed by Balance) ===\n");
        sb.append(String.format("  Tree Size   : %d nodes%n", bst.size()));
        sb.append(String.format("  Tree Height : %d%n", bst.height()));
        Accounts min = bst.findMin(), max = bst.findMax();
        if (min != null) sb.append(String.format("  Lowest Balance  : %s (PKR %.2f)%n", min.getAccountNumber(), min.getBalance()));
        if (max != null) sb.append(String.format("  Highest Balance : %s (PKR %.2f)%n", max.getAccountNumber(), max.getBalance()));
        sb.append("\n  In-Order Traversal (ascending by balance):\n");
        int i = 1;
        for (Accounts a : bst.inOrder()) sb.append("  ").append(i++).append(". ").append(a).append("\n");
        return sb.toString();
    }

    // ── Custom Merge Sort: Transactions sorted by amount ──
    public ArrayList<Transaction> getTransactionsSortedByAmount(String accNo, boolean ascending) {
        ArrayList<Transaction> list = new ArrayList<>(db.getTransactions(accNo));
        SortUtil.mergeSortByAmount(list, ascending);
        return list;
    }
    public String allTransactions() {
        StringBuilder sb = new StringBuilder("\n── All Transactions ──\n");
        ArrayList<Transaction> all = BankDatabase.getInstance().getAllTransactions();
        if (all.isEmpty()) {
            return "No transactions found!";
        }
        for (Transaction txn : all) {
            sb.append(txn).append("\n");
        }
        sb.append("\nTotal: ").append(all.size()).append(" transactions");
        return sb.toString();
    }
}

