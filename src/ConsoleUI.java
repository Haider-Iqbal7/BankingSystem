import java.util.*;

public class ConsoleUI {
    private Scanner obj = new Scanner(System.in);
    private AccountService accountService = new AccountService();
    private LoanService loanService = new LoanService();
    private QueueService queueService = new QueueService();
    private ReportService reportService = new ReportService();
    private Admin loggedInAdmin = null;

    public void start() {
        printBanner();
        if (!login()) {
            System.out.println("Login failed. Exiting...");
            return;
        }
        mainMenu();
    }

    private void printBanner() {
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║         SMARTBANK MANAGEMENT SYSTEM                     ║");
        System.out.println("║         DSA Project - Banking System                    ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println();
    }

    private boolean login() {
        System.out.println("═══════════════ ADMIN LOGIN ═══════════════");
        for (int attempts = 0; attempts < 3; attempts++) {
            System.out.print("Username: ");
            String user = obj.nextLine().trim();
            System.out.print("Password: ");
            String pass = obj.nextLine().trim();
            com.smartbank.dao.BankDatabase db = com.smartbank.dao.BankDatabase.getInstance();
            loggedInAdmin = db.authenticate(user, pass);
            if (loggedInAdmin != null) {
                System.out.println("\n✔ Welcome, " + loggedInAdmin.getFullName() + " [" + loggedInAdmin.getRole() + "]");
                return true;
            }
            System.out.println("✘ Invalid credentials! Attempts left: " + (2 - attempts));
        }
        return false;
    }

    private void mainMenu() {
        while (true) {
            System.out.println("\n╔══════════════════════════════════════╗");
            System.out.println("║           MAIN MENU                  ║");
            System.out.println("╠══════════════════════════════════════╣");
            System.out.println("║  1. Account Management               ║");
            System.out.println("║  2. Transactions                     ║");
            System.out.println("║  3. Queue Management                 ║");
            System.out.println("║  4. Loan Management                  ║");
            System.out.println("║  5. Reports                          ║");
            if (loggedInAdmin.getRole() == Admin.AdminRole.Super_Admin ||
                    loggedInAdmin.getRole() == Admin.AdminRole.Manager) {
                System.out.println("║  6. Admin Panel                      ║");
            }
            System.out.println("║  0. Exit                             ║");
            System.out.println("╚══════════════════════════════════════╝");
            System.out.print("Choice: ");
            String choice = obj.nextLine().trim();
            switch (choice) {
                case "1":
                    accountMenu();
                    break;
                case "2":
                    transactionMenu();
                    break;
                case "3":
                    queueMenu();
                    break;
                case "4":
                    loanMenu();
                    break;
                case "5":
                    reportsMenu();
                    break;
                case "6":
                    if (loggedInAdmin.getRole() == Admin.AdminRole.Super_Admin ||
                            loggedInAdmin.getRole() == Admin.AdminRole.Manager) {
                        adminPanelMenu();
                    } else {
                        System.out.println("Access denied!");
                    }
                    break;
                case "0":
                    System.out.println("Goodbye, " + loggedInAdmin.getFullName() + "!");
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    // ─── ACCOUNT MENU ───
    private void accountMenu() {
        while (true) {
            System.out.println("\n── Account Management ──");
            System.out.println("1. Create New Account");
            System.out.println("2. View Account");
            System.out.println("3. Update Account");
            System.out.println("4. Close Account");
            System.out.println("5. List All Accounts (Sorted)");
            System.out.println("6. Change PIN");
            System.out.println("0. Back");
            System.out.print("Choice: ");
            switch (obj.nextLine().trim()) {
                case "1":
                    createAccount();
                    break;
                case "2":
                    viewAccount();
                    break;
                case "3":
                    updateAccount();
                    break;
                case "4":
                    closeAccount();
                    break;
                case "5":
                    listAllAccounts();
                    break;
                case "6":
                    changePin();
                    break;
                case "0":
                    return;
            }
        }
    }

    private void createAccount() {
        System.out.println("\n── Create New Account ──");
        System.out.print("Full Name       : ");
        String name = obj.nextLine().trim();
        System.out.print("CNIC (XXXXX-XXXXXXX-X): ");
        String cnic = obj.nextLine().trim();
        System.out.print("Phone           : ");
        String phone = obj.nextLine().trim();
        System.out.print("Address         : ");
        String address = obj.nextLine().trim();
        System.out.println("Account Type: 1=SAVINGS  2=CURRENT  3=FIXED_DEPOSIT");
        System.out.print("Choice          : ");
        String typeChoice = obj.nextLine().trim();
        Accounts.AccountType type = switch (typeChoice) {
            case "2" -> Accounts.AccountType.CURRENT;
            case "3" -> Accounts.AccountType.FIXED_DEPOSIT;
            default -> Accounts.AccountType.SAVINGS;
        };
        System.out.print("Initial Deposit : PKR ");
        double deposit = Double.parseDouble(obj.nextLine().trim());
        System.out.print("Set 4-digit PIN : ");
        String pin = obj.nextLine().trim();

        String result = accountService.createAccount(name, cnic, phone, address, type, deposit, pin);
        if (result.startsWith("SUCCESS")) {
            System.out.println("✔ Account created! Account Number: " + result.split(":")[1]);
        } else {
            System.out.println("✘ " + result);
        }
    }

    private void viewAccount() {
        System.out.print("Enter Account Number: ");
        String accNo = obj.nextLine().trim();
        Accounts acc = accountService.getAccount(accNo);
        if (acc == null) {
            System.out.println("✘ Account not found!");
            return;
        }
        System.out.println("\n── Account Details ──");
        System.out.println("Account No  : " + acc.getAccountNumber());
        System.out.println("Owner       : " + acc.getOwnerName());
        System.out.println("CNIC        : " + acc.getCnic());
        System.out.println("Phone       : " + acc.getPhone_no());
        System.out.println("Address     : " + acc.getAddress());
        System.out.println("Type        : " + acc.getAccountType());
        System.out.printf("Balance     : PKR %.2f%n", acc.getBalance());
        System.out.println("Status      : " + (acc.isActive() ? "ACTIVE" : "INACTIVE"));
        System.out.println("Opened      : " + acc.getCreatedAt());
    }

    private void updateAccount() {
        System.out.print("Enter Account Number: ");
        String accNo = obj.nextLine().trim();
        System.out.print("New Phone (leave blank to skip): ");
        String phone = obj.nextLine().trim();
        System.out.print("New Address (leave blank to skip): ");
        String addr = obj.nextLine().trim();
        System.out.println(accountService.updateAccount(accNo, phone, addr));
    }

    private void closeAccount() {
        System.out.print("Enter Account Number: ");
        String accNo = obj.nextLine().trim();
        System.out.print("Enter PIN: ");
        String pin = obj.nextLine().trim();
        System.out.println(accountService.closeAccount(accNo, pin));
    }

    private void listAllAccounts() {
        System.out.println("\n── All Accounts (Sorted by Account Number - TreeSet) ──");
        TreeSet<Accounts> sorted = accountService.getSortedAccounts();
        if (sorted.isEmpty()) {
            System.out.println("No accounts found!");
            return;
        }
        int i = 1;
        for (Accounts acc : sorted) {
            System.out.println(i++ + ". " + acc);
        }
        System.out.println("\nTotal: " + sorted.size() + " accounts");
    }

    private void changePin() {
        System.out.print("Account Number : ");
        String accNo = obj.nextLine().trim();
        System.out.print("Old PIN        : ");
        String oldPin = obj.nextLine().trim();
        System.out.print("New PIN        : ");
        String newPin = obj.nextLine().trim();
        System.out.println(accountService.changePin(accNo, oldPin, newPin));
    }

    // ─── TRANSACTION MENU ───
    private void transactionMenu() {
        while (true) {
            System.out.println("\n── Transactions ──");
            System.out.println("1. Deposit");
            System.out.println("2. Withdraw");
            System.out.println("3. Transfer");
            System.out.println("4. View Transaction History");
            System.out.println("0. Back");
            System.out.print("Choice: ");
            switch (obj.nextLine().trim()) {
                case "1":
                    deposit();
                    break;
                case "2":
                    withdraw();
                    break;
                case "3":
                    transfer();
                    break;
                case "4":
                    viewHistory();
                    break;
                case "0":
                    return;
            }
        }
    }

    private void deposit() {
        System.out.print("Account Number : ");
        String accNo = obj.nextLine().trim();
        System.out.print("Amount         : PKR ");
        double amt = Double.parseDouble(obj.nextLine().trim());
        System.out.print("Description    : ");
        String desc = obj.nextLine().trim();
        System.out.println(accountService.deposit(accNo, amt, desc));
    }

    private void withdraw() {
        System.out.print("Account Number : ");
        String accNo = obj.nextLine().trim();
        System.out.print("PIN            : ");
        String pin = obj.nextLine().trim();
        System.out.print("Amount         : PKR ");
        double amt = Double.parseDouble(obj.nextLine().trim());
        System.out.println(accountService.withdraw(accNo, amt, pin));
    }

    private void transfer() {
        System.out.print("From Account   : ");
        String from = obj.nextLine().trim();
        System.out.print("To Account     : ");
        String to = obj.nextLine().trim();
        System.out.print("Amount         : PKR ");
        double amt = Double.parseDouble(obj.nextLine().trim());
        System.out.print("PIN            : ");
        String pin = obj.nextLine().trim();
        System.out.println(accountService.transfer(from, to, amt, pin));
    }

    private void viewHistory() {
        System.out.print("Account Number: ");
        String accNo = obj.nextLine().trim();
        System.out.println(reportService.getAccountStatement(accNo));
    }

    // ─── QUEUE MENU ───
    private void queueMenu() {
        while (true) {
            System.out.println("\n── Queue Management (LinkedList) ──");
            System.out.println("1. Issue Token");
            System.out.println("2. Serve Next Customer");
            System.out.println("3. View Queue");
            System.out.println("4. Cancel Token");
            System.out.println("0. Back");
            System.out.print("Choice: ");
            switch (obj.nextLine().trim()) {
                case "1":
                    issueToken();
                    break;
                case "2":
                    serveNext();
                    break;
                case "3":
                    viewQueue();
                    break;
                case "4":
                    cancelToken();
                    break;
                case "0":
                    return;
            }
        }
    }

    private void issueToken() {
        System.out.print("Customer Name (Enter for Walk-in): ");
        String name = obj.nextLine().trim();
        System.out.print("Account No (Enter if N/A)        : ");
        String accNo = obj.nextLine().trim();
        System.out.println("Service: 1=DEPOSIT  2=WITHDRAWAL  3=ACCOUNT_OPENING  4=LOAN_INQUIRY  5=TRANSFER  6=GENERAL_INQUIRY");
        System.out.print("Choice: ");
        String svc = obj.nextLine().trim();
        QueueToken.ServiceType serviceType = switch (svc) {
            case "2" -> QueueToken.ServiceType.Withdrawal;
            case "3" -> QueueToken.ServiceType.Account_Opening;
            case "4" -> QueueToken.ServiceType.Loan_Inquiry;
            case "5" -> QueueToken.ServiceType.Transfer;
            case "6" -> QueueToken.ServiceType.General_Inquiry;
            default -> QueueToken.ServiceType.Deposit;

        };
        System.out.println("Priority: 1=NORMAL  2=SENIOR_CITIZEN  3=VIP  4=EMERGENCY");
        System.out.print("Choice: ");
        String pri = obj.nextLine().trim();
        QueueToken.Priority priority = switch (pri) {
            case "2" -> QueueToken.Priority.Senior_Citizen;
            case "3" -> QueueToken.Priority.VIP;
            case "4" -> QueueToken.Priority.Emergency;
            default -> QueueToken.Priority.Normal;
        };

        QueueToken token = queueService.issueToken(name, accNo, serviceType, priority);
        System.out.println("\n✔ Token Issued!");
        System.out.println(token);
        System.out.println("Position in Queue: " + queueService.getQueueSize());
        System.out.println("Estimated Wait: ~" + queueService.getEstimatedWaitTime(token.getTokenNumber()) + " minutes");
    }

    private void serveNext() {
        QueueToken token = queueService.serveNext();
        if (token == null) {
            System.out.println("Queue is empty!");
            return;
        }
        System.out.println("\n✔ Now Serving:");
        System.out.println(token);
        System.out.println("Remaining in queue: " + queueService.getQueueSize());
    }

    private void viewQueue() {
        System.out.println("\n── Current Queue ── [Total: " + queueService.getQueueSize() + "]");
        LinkedList<QueueToken> queue = queueService.getCurrentQueue();
        if (queue.isEmpty()) {
            System.out.println("Queue is empty!");
            return;
        }
        int i = 1;
        for (QueueToken t : queue) {
            System.out.println(i++ + ". " + t);
        }
    }

    private void cancelToken() {
        System.out.print("Token Number: ");
        String tokenNo = obj.nextLine().trim();
        System.out.println(queueService.cancelToken(tokenNo));
    }

    // ─── LOAN MENU ───
    private void loanMenu() {
        while (true) {
            System.out.println("\n── Loan Management (TreeMap) ──");
            System.out.println("1. Apply for Loan");
            System.out.println("2. View My Loans");
            System.out.println("3. Make EMI Payment");
            System.out.println("4. View Pending Loans (Admin)");
            System.out.println("5. Approve Loan (Admin)");
            System.out.println("6. Reject Loan (Admin)");
            System.out.println("0. Back");
            System.out.print("Choice: ");
            switch (obj.nextLine().trim()) {
                case "1":
                    applyLoan();
                    break;
                case "2":
                    viewLoans();
                    break;
                case "3":
                    payEmi();
                    break;
                case "4":
                    viewPendingLoans();
                    break;
                case "5":
                    approveLoan();
                    break;
                case "6":
                    rejectLoan();
                    break;
                case "0":
                    return;
            }
        }
    }

    private void applyLoan() {
        System.out.print("Account Number  : ");
        String accNo = obj.nextLine().trim();
        System.out.println("Loan Type: 1=PERSONAL  2=HOME  3=AUTO  4=BUSINESS  5=EDUCATION");
        System.out.print("Choice          : ");
        String lt = obj.nextLine().trim();
        Loan.LoanType loanType = switch (lt) {
            case "2" -> Loan.LoanType.Home;
            case "3" -> Loan.LoanType.Auto;
            case "4" -> Loan.LoanType.Business;
            case "5" -> Loan.LoanType.Education;
            default -> Loan.LoanType.Personal;
        };
        System.out.print("Loan Amount     : PKR ");
        double amt = Double.parseDouble(obj.nextLine().trim());
        System.out.print("Interest Rate % : ");
        double rate = Double.parseDouble(obj.nextLine().trim());
        System.out.print("Tenure (months) : ");
        int tenure = Integer.parseInt(obj.nextLine().trim());
        System.out.println(loanService.applyLoanString(accNo, loanType, amt, rate, tenure));
    }

    private void viewLoans() {
        System.out.print("Account Number: ");
        String accNo = obj.nextLine().trim();
        List<Loan> loans = loanService.getAccountLoans(accNo);
        if (loans.isEmpty()) {
            System.out.println("No loans found!");
            return;
        }
        System.out.println("\n── Loans for Account " + accNo + " ──");
        for (Loan l : loans) System.out.println(l);
    }

    private void payEmi() {
        System.out.print("Loan ID        : ");
        String loanId = obj.nextLine().trim();
        System.out.print("Account Number : ");
        String accNo = obj.nextLine().trim();
        System.out.print("PIN            : ");
        String pin = obj.nextLine().trim();
        System.out.println(loanService.makePayment(loanId, accNo, pin));
    }

    private void viewPendingLoans() {
        List<Loan> pending = loanService.getPendingLoans();
        if (pending.isEmpty()) {
            System.out.println("No pending loans!");
            return;
        }
        System.out.println("\n── Pending Loans ──");
        for (Loan l : pending) System.out.println(l);
    }

    private void approveLoan() {
        System.out.print("Loan ID: ");
        String loanId = obj.nextLine().trim();
        System.out.println(loanService.approveLoan(loanId));
    }

    private void rejectLoan() {
        System.out.print("Loan ID: ");
        String loanId = obj.nextLine().trim();
        System.out.println(loanService.rejectLoan(loanId));
    }

    // ─── REPORTS MENU ───
    private void reportsMenu() {
        while (true) {
            System.out.println("\n── Reports ──");
            System.out.println("1. Bank Summary");
            System.out.println("2. Account Statement");
            System.out.println("3. All Accounts (Sorted - TreeSet)");
            System.out.println("4. Balance by Account Type (TreeMap)");
            System.out.println("5. Audit Trail (Stack - Last 20 ops)");
            System.out.println("6. Accounts Sorted by Balance (Merge Sort)");
            System.out.println("7. Account BST Report (Custom BST)");
            System.out.println("8. Search Account by Balance (BST)");
            System.out.println("9. Balance Range Query (BST)");
            System.out.println("0. Back");
            System.out.print("Choice: ");
            switch (obj.nextLine().trim()) {
                case "1":
                    System.out.println(reportService.getBankSummary());
                    break;
                case "2": {
                    System.out.print("Account Number: ");
                    System.out.println(reportService.getAccountStatement(sc.nextLine().trim()));
                    break;
                }
                case "3":
                    listAllAccounts();
                    break;
                case "4":
                    showBalanceByType();
                    break;
                case "5":
                    System.out.println(reportService.getAuditTrail(20));
                    break;
                case "6":
                    sortAccountsByBalance();
                    break;
                case "7":
                    System.out.println(reportService.getBSTReport());
                    break;
                case "8":
                    searchAccountByBalance();
                    break;
                case "9":
                    balanceRangeQuery();
                    break;
                case "0":
                    return;
            }
        }
    }

    private void showBalanceByType() {
        System.out.println("\n── Balance By Account Type (TreeMap) ──");
        TreeMap<Accounts.AccountType, Double> report = reportService.getBalanceByAccountType();
        for (Map.Entry<Accounts.AccountType, Double> entry : report.entrySet()) {
            System.out.printf("  %-20s : PKR %.2f%n", entry.getKey(), entry.getValue());
        }
    }

    private void sortAccountsByBalance() {
        System.out.print("Order (1=Ascending, 2=Descending): ");
        boolean asc = !obj.nextLine().trim().equals("2");
        System.out.println(reportService.getAccountsSortedByBalanceReport(asc));
    }

    private void searchAccountByBalance() {
        System.out.print("Enter exact balance to search: ");
        double bal = Double.parseDouble(obj.nextLine().trim());
        Accounts found = reportService.findAccountByBalance(bal);
        System.out.println(found != null ? "✔ Found: " + found : "✘ No account with that exact balance!");
    }

    private void balanceRangeQuery() {
        System.out.print("Min balance: ");
        double min = Double.parseDouble(obj.nextLine().trim());
        System.out.print("Max balance: ");
        double max = Double.parseDouble(obj.nextLine().trim());
        java.util.ArrayList<Accounts> results = reportService.getAccountsInBalanceRange(min, max);
        if (results.isEmpty()) {
            System.out.println("No accounts in this range!");
            return;
        }
        int i = 1;
        for (Accounts a : results)
            System.out.println(i++ + ". " + a);
    }

    // ─── ADMIN PANEL ───
    private void adminPanelMenu() {
        while (true) {
            System.out.println("\n── Admin Panel ──");
            System.out.println("1. List All Admins");
            System.out.println("2. Add New Admin");
            System.out.println("3. View All Transactions");
            System.out.println("0. Back");
            System.out.print("Choice: ");
            switch (obj.nextLine().trim()) {
                case "1":
                    listAdmins();
                    break;
                case "2":
                    addAdmin();
                    break;
                case "3":
                    allTransactions();
                    break;
                case "0":
                    return;
            }
        }

    }
}
