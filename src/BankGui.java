import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Map;

public class BankGui extends JFrame {
    // Colors
    private static final Color PRIMARY = new Color(0, 82, 165);
    private static final Color SECONDARY = new Color(0, 133, 202);
    private static final Color ACCENT = new Color(255, 193, 7);
    private static final Color SUCCESS = new Color(40, 167, 69);
    private static final Color DANGER = new Color(220, 53, 69);
    private static final Color BG_DARK = new Color(30, 40, 55);
    private static final Color BG_PANEL = new Color(245, 247, 250);
    private static final Color WHITE = Color.WHITE;

    private Admin loggedInAdmin;
    private AccountService accountService = new AccountService();
    private LoanService loanService = new LoanService();
    private QueueService queueService = new QueueService();
    private ReportService reportService = new ReportService();

    private JTabbedPane mainTabs;
    private JLabel statusBar;

    // ════════════════════ LAUNCH ════════════════════
    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new LoginDialog().setVisible(true);
        });
    }

    public BankGui(Admin admin) {
        this.loggedInAdmin = admin;
        setTitle("SmartBank Management System - " + admin.getFullName() + " [" + admin.getRole() + "]");
        setSize(1100, 720);
        setMinimumSize(new Dimension(900, 600));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        buildUI();
        setVisible(true);
    }

    private void buildUI() {
        setLayout(new  BorderLayout());

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY);
        header.setPreferredSize(new Dimension(0, 60));
        JLabel title = new JLabel("  🏦  SmartBank Management System", JLabel.LEFT);
        title.setForeground(WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        JLabel adminInfo = new JLabel("Logged in: " + loggedInAdmin.getFullName() + "  |  " + loggedInAdmin.getRole() + "  ", JLabel.RIGHT);
        adminInfo.setForeground(ACCENT);
        adminInfo.setFont(new Font("Arial", Font.PLAIN, 13));
        header.add(title, BorderLayout.WEST);
        header.add(adminInfo, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Tabs
        mainTabs = new JTabbedPane(JTabbedPane.TOP);
        mainTabs.setFont(new Font("Arial", Font.BOLD, 13));
        mainTabs.addTab("🏠 Dashboard", createDashboardPanel());
        mainTabs.addTab("👤 Accounts", createAccountPanel());
        mainTabs.addTab("💸 Transactions", createTransactionPanel());
        mainTabs.addTab("🎫 Queue", createQueuePanel());
        mainTabs.addTab("💰 Loans", createLoanPanel());
        mainTabs.addTab("📊 Reports", createReportsPanel());
        if (loggedInAdmin.getRole() == Admin.AdminRole.Super_Admin ||
                loggedInAdmin.getRole() == Admin.AdminRole.Manager) {
            mainTabs.addTab("⚙ Admin", createAdminPanel());
        }
        add(mainTabs, BorderLayout.CENTER);

        // Status bar
        statusBar = new JLabel("  Ready - SmartBank Management System");
        statusBar.setFont(new Font("Arial", Font.PLAIN, 12));
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY),
                new EmptyBorder(4, 10, 4, 10)));
        add(statusBar, BorderLayout.SOUTH);
    }

    private void setStatus(String msg, boolean success) {
        statusBar.setText("  " + (success ? "✔" : "✘") + " " + msg);
        statusBar.setForeground(success ? SUCCESS : DANGER);

    }

    // ════════════════════ DASHBOARD ════════════════════
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 3, 15, 15));
        panel.setBackground(BG_PANEL);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        BankDatabase db = BankDatabase.getInstance();

        panel.add(statCard("Total Accounts", String.valueOf(db.getActiveAccountCount()), PRIMARY));
        panel.add(statCard("Total Balance", String.format("PKR %.0f", db.getTotalDeposits()), SUCCESS));
        panel.add(statCard("Active Loans", String.format("PKR %.0f", db.getTotalLoanAmount()), DANGER));
        panel.add(statCard("Queue Size", String.valueOf(db.getQueueSize()), SECONDARY));
        panel.add(statCard("Operations", String.valueOf(db.getOperationStack().size()), new Color(108, 117, 125)));
        panel.add(statCard("Pending Loans", String.valueOf(loanService.getPendingLoans().size()), new Color(253, 126, 20)));
        return panel;
    }

    private JPanel statCard(String label, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(color, 2, true),
                new EmptyBorder(20, 20, 20, 20)));
        JLabel lbl = new JLabel(label, JLabel.CENTER);
        lbl.setFont(new Font("Arial", Font.PLAIN, 13));
        lbl.setForeground(Color.GRAY);
        JLabel val = new JLabel(value, JLabel.CENTER);
        val.setFont(new Font("Arial", Font.BOLD, 28));
        val.setForeground(color);
        card.add(lbl, BorderLayout.NORTH);
        card.add(val, BorderLayout.CENTER);
        return card;
    }

    // ════════════════════ ACCOUNTS ════════════════════
    private JPanel createAccountPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_PANEL);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Table
        String[] cols = {"Account No", "Owner", "Type", "Balance", "Phone", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable table = new JTable(model);
        styleTable(table);
        refreshAccountTable(model);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnPanel.setBackground(BG_PANEL);
        JButton btnCreate = makeBtn("+ New Account", SUCCESS);
        JButton btnView = makeBtn("View Details", SECONDARY);
        JButton btnUpdate = makeBtn("Update", PRIMARY);
        JButton btnClose = makeBtn("Close Account", DANGER);
        JButton btnRefresh = makeBtn("↻ Refresh", Color.GRAY);
        btnPanel.add(btnCreate);
        btnPanel.add(btnView);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnClose);
        btnPanel.add(btnRefresh);

        btnCreate.addActionListener(e -> showCreateAccountDialog(model));
        btnRefresh.addActionListener(e -> refreshAccountTable(model));
        btnView.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                showMsg("Select an account first!", false);
                return;
            }
            String accNo = (String) model.getValueAt(row, 0);
            showAccountDetails(accNo);
        });
        btnClose.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                showMsg("Select an account first!", false);
                return;
            }
            String accNo = (String) model.getValueAt(row, 0);
            String pin = JOptionPane.showInputDialog(this, "Enter PIN for " + accNo + ":", "Close Account", JOptionPane.WARNING_MESSAGE);
            if (pin != null) {
                String result = accountService.closeAccount(accNo, pin);
                showMsg(result, result.startsWith("SUCCESS"));
                refreshAccountTable(model);
            }
        });

        panel.add(btnPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void refreshAccountTable(DefaultTableModel model) {
        model.setRowCount(0);
        for (Accounts acc : accountService.getSortedAccounts()) {
            model.addRow(new Object[]{
                    acc.getAccountNumber(), acc.getOwnerName(), acc.getAccountType(),
                    String.format("PKR %.2f", acc.getBalance()), acc.getPhone_no(),
                    acc.isActive() ? "ACTIVE" : "INACTIVE"
            });
        }
    }

    private void showCreateAccountDialog(DefaultTableModel model) {
        JDialog dlg = new JDialog(this, "Create New Account", true);
        dlg.setSize(450, 430);
        dlg.setLocationRelativeTo(this);
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(new EmptyBorder(20, 20, 10, 20));

        JTextField fName    = new JTextField(); JTextField fCnic  = new JTextField();
        JTextField fPhone   = new JTextField(); JTextField fAddr  = new JTextField();
        JComboBox<Accounts.AccountType> fType = new JComboBox<>(Accounts.AccountType.values());
        JTextField fDeposit = new JTextField("10000"); JPasswordField fPin = new JPasswordField();

        form.add(new JLabel("Full Name:")); form.add(fName);
        form.add(new JLabel("CNIC:")); form.add(fCnic);
        form.add(new JLabel("Phone:")); form.add(fPhone);
        form.add(new JLabel("Address:")); form.add(fAddr);
        form.add(new JLabel("Account Type:")); form.add(fType);
        form.add(new JLabel("Initial Deposit (PKR):")); form.add(fDeposit);
        form.add(new JLabel("4-digit PIN:")); form.add(fPin);

        JButton btnSave = makeBtn("Create Account", SUCCESS);
        btnSave.addActionListener(ev -> {
            try {
                String result = accountService.createAccount(
                        fName.getText(), fCnic.getText(), fPhone.getText(), fAddr.getText(),
                        (Accounts.AccountType) fType.getSelectedItem(),
                        Double.parseDouble(fDeposit.getText()), new String(fPin.getPassword()));
                if (result.startsWith("SUCCESS")) {
                    JOptionPane.showMessageDialog(dlg, "✔ Account Created!\nAccount No: " + result.split(":")[1]);
                    refreshAccountTable(model);
                    dlg.dispose();
                } else {
                    JOptionPane.showMessageDialog(dlg, "✘ " + result, "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Invalid amount!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        JPanel bottom = new JPanel(); bottom.add(btnSave);
        dlg.add(form, BorderLayout.CENTER);
        dlg.add(bottom, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void showAccountDetails(String accNo) {
        Accounts acc = accountService.getAccount(accNo);
        if (acc == null) return;
        String msg = String.format(
                "Account No  : %s%nOwner       : %s%nCNIC        : %s%n" +
                        "Phone       : %s%nAddress     : %s%nType        : %s%n" +
                        "Balance     : PKR %.2f%nStatus      : %s%nOpened      : %s",
                acc.getAccountNumber(), acc.getOwnerName(), acc.getCnic(),
                acc.getPhone_no(), acc.getAddress(), acc.getAccountType(),
                acc.getBalance(), acc.isActive() ? "ACTIVE" : "INACTIVE", acc.getCreatedAt());
        JOptionPane.showMessageDialog(this, msg, "Account Details - " + accNo, JOptionPane.INFORMATION_MESSAGE);
    }
    // ════════════════════ TRANSACTIONS ════════════════════
    private JPanel createTransactionPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_PANEL);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JTabbedPane txnTabs = new JTabbedPane();
        txnTabs.addTab("Deposit", createDepositPanel());
        txnTabs.addTab("Withdraw", createWithdrawPanel());
        txnTabs.addTab("Transfer", createTransferPanel());
        txnTabs.addTab("History", createHistoryPanel());
        panel.add(txnTabs, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createDepositPanel() {
        JPanel p = formPanel();
        JTextField accNo = new JTextField(); JTextField amt = new JTextField();
        JTextField desc = new JTextField("Cash deposit"); JLabel result = new JLabel();
        addFormRow(p, "Account Number:", accNo);
        addFormRow(p, "Amount (PKR):", amt);
        addFormRow(p, "Description:", desc);
        JButton btn = makeBtn("Deposit ➤", SUCCESS);
        btn.addActionListener(e -> {
            try {
                String r = accountService.deposit(accNo.getText().trim(),
                        Double.parseDouble(amt.getText().trim()), desc.getText().trim());
                result.setText(r); result.setForeground(r.startsWith("SUCCESS") ? SUCCESS : DANGER);
                setStatus(r, r.startsWith("SUCCESS"));
            } catch (Exception ex) { result.setText("Invalid input!"); result.setForeground(DANGER); }
        });
        p.add(btn); p.add(result);
        return wrap(p);
    }

    private JPanel createWithdrawPanel() {
        JPanel p = formPanel();
        JTextField accNo = new JTextField(); JPasswordField pin = new JPasswordField();
        JTextField amt = new JTextField(); JLabel result = new JLabel();
        addFormRow(p, "Account Number:", accNo);
        addFormRow(p, "PIN:", pin);
        addFormRow(p, "Amount (PKR):", amt);
        JButton btn = makeBtn("Withdraw ➤", DANGER);
        btn.addActionListener(e -> {
            try {
                String r = accountService.withdraw(accNo.getText().trim(), Double.parseDouble(amt.getText().trim()), new String(pin.getPassword()));
                result.setText(r); result.setForeground(r.startsWith("SUCCESS") ? SUCCESS : DANGER);
                setStatus(r, r.startsWith("SUCCESS"));
            } catch (Exception ex) { result.setText("Invalid input!"); result.setForeground(DANGER); }
        });
        p.add(btn); p.add(result);
        return wrap(p);
    }

    private JPanel createTransferPanel() {
        JPanel p = formPanel();
        JTextField from = new JTextField(); JTextField to = new JTextField();
        JTextField amt = new JTextField(); JPasswordField pin = new JPasswordField();
        JLabel result = new JLabel();
        addFormRow(p, "From Account:", from);
        addFormRow(p, "To Account:", to);
        addFormRow(p, "Amount (PKR):", amt);
        addFormRow(p, "PIN:", pin);
        JButton btn = makeBtn("Transfer ➤", PRIMARY);
        btn.addActionListener(e -> {
            try {
                String r = accountService.transfer(from.getText().trim(), to.getText().trim(),
                        Double.parseDouble(amt.getText().trim()), new String(pin.getPassword()));
                result.setText(r); result.setForeground(r.startsWith("SUCCESS") ? SUCCESS : DANGER);
                setStatus(r, r.startsWith("SUCCESS"));
            } catch (Exception ex) { result.setText("Invalid input!"); result.setForeground(DANGER); }
        });
        p.add(btn); p.add(result);
        return wrap(p);
    }

    private JPanel createHistoryPanel() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBackground(BG_PANEL);
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBackground(BG_PANEL);
        JTextField accNo = new JTextField(15);
        JButton btn = makeBtn("Load History", PRIMARY);
        top.add(new JLabel("Account Number:")); top.add(accNo); top.add(btn);
        JTextArea area = new JTextArea();
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setEditable(false);
        btn.addActionListener(e -> area.setText(reportService.getAccountStatement(accNo.getText().trim())));
        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(area), BorderLayout.CENTER);
        return p;
    }
    // ════════════════════ QUEUE ════════════════════
    private JPanel createQueuePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_PANEL);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        String[] cols = {"Token", "Customer", "Service", "Priority", "Status", "Issued At"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        styleTable(table);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        top.setBackground(BG_PANEL);
        JButton btnIssue   = makeBtn("Issue Token", SUCCESS);
        JButton btnServe   = makeBtn("▶ Serve Next", PRIMARY);
        JButton btnCancel  = makeBtn("Cancel Token", DANGER);
        JButton btnRefresh = makeBtn("↻ Refresh", Color.GRAY);
        top.add(btnIssue); top.add(btnServe); top.add(btnCancel); top.add(btnRefresh);

        Runnable refresh = () -> {
            model.setRowCount(0);
            for (QueueToken t : queueService.getCurrentQueue()) {
                model.addRow(new Object[]{
                        t.getTokenNumber(), t.getCustomerName(), t.getServiceType(),
                        t.getPriority(), t.getStatus(), t.getIssuedAt().toLocalTime().toString().substring(0, 8)
                });
            }
        };

        btnRefresh.addActionListener(e -> refresh.run());
        btnServe.addActionListener(e -> {
            QueueToken t = queueService.serveNext();
            if (t == null) showMsg("Queue is empty!", false);
            else { showMsg("Now serving: " + t.getTokenNumber() + " - " + t.getCustomerName(), true); refresh.run(); }
        });
        btnIssue.addActionListener(e -> showIssueTokenDialog(refresh));
        btnCancel.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { showMsg("Select a token first!", false); return; }
            String tokenNo = (String) model.getValueAt(row, 0);
            String result = queueService.cancelToken(tokenNo);
            showMsg(result, result.startsWith("SUCCESS")); refresh.run();
        });

        refresh.run();
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JLabel sizeLabel = new JLabel();
        sizeLabel.setFont(new Font("Arial", Font.BOLD, 13));
        sizeLabel.setBorder(new EmptyBorder(5, 0, 0, 0));
        Timer timer = new Timer(2000, e -> {
            sizeLabel.setText("  Customers in Queue: " + queueService.getQueueSize());
            refresh.run();
        });
        timer.start();
        panel.add(sizeLabel, BorderLayout.SOUTH);
        return panel;
    }

    private void showIssueTokenDialog(Runnable refresh) {
        JDialog dlg = new JDialog(this, "Issue Queue Token", true);
        dlg.setSize(380, 300);
        dlg.setLocationRelativeTo(this);
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(new EmptyBorder(20, 20, 10, 20));
        JTextField fName = new JTextField();
        JTextField fAcc  = new JTextField();
        JComboBox<QueueToken.ServiceType> fSvc = new JComboBox<>(QueueToken.ServiceType.values());
        JComboBox<QueueToken.Priority>    fPri = new JComboBox<>(QueueToken.Priority.values());
        form.add(new JLabel("Customer Name:")); form.add(fName);
        form.add(new JLabel("Account No (opt):")); form.add(fAcc);
        form.add(new JLabel("Service Type:")); form.add(fSvc);
        form.add(new JLabel("Priority:")); form.add(fPri);
        JButton btn = makeBtn("Issue Token", SUCCESS);
        btn.addActionListener(ev -> {
            QueueToken t = queueService.issueToken(fName.getText().trim(), fAcc.getText().trim(),
                    (QueueToken.ServiceType) fSvc.getSelectedItem(),
                    (QueueToken.Priority) fPri.getSelectedItem());
            JOptionPane.showMessageDialog(dlg,
                    "Token Issued: " + t.getTokenNumber() + "\nCustomer: " + t.getCustomerName() +
                            "\nService: " + t.getServiceType() + "\nPriority: " + t.getPriority() +
                            "\nEst. Wait: ~" + queueService.getEstimatedWaitTime(t.getTokenNumber()) + " mins");
            refresh.run();
            dlg.dispose();
        });
        JPanel bot = new JPanel(); bot.add(btn);
        dlg.add(form, BorderLayout.CENTER);
        dlg.add(bot, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ════════════════════ LOANS ════════════════════
    private JPanel createLoanPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_PANEL);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JTabbedPane loanTabs = new JTabbedPane();
        loanTabs.addTab("Apply", createApplyLoanPanel());
        loanTabs.addTab("My Loans", createViewLoansPanel());
        loanTabs.addTab("Pay EMI", createPayEmiPanel());
        loanTabs.addTab("Pending (Admin)", createPendingLoansPanel());
        panel.add(loanTabs, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createApplyLoanPanel() {
        JPanel p = formPanel();
        JTextField accNo = new JTextField();
        JComboBox<Loan.LoanType> loanType = new JComboBox<>(Loan.LoanType.values());
        JTextField amount = new JTextField(); JTextField rate = new JTextField("12.0");
        JTextField tenure = new JTextField("12"); JLabel result = new JLabel();
        addFormRow(p, "Account Number:", accNo);
        addFormRow(p, "Loan Type:", loanType);
        addFormRow(p, "Amount (PKR):", amount);
        addFormRow(p, "Interest Rate (%):", rate);
        addFormRow(p, "Tenure (months):", tenure);
        JButton btn = makeBtn("Apply for Loan", PRIMARY);
        btn.addActionListener(e -> {
            try {
                String r = loanService.applyLoanString(accNo.getText().trim(),
                        (Loan.LoanType) loanType.getSelectedItem(),
                        Double.parseDouble(amount.getText()), Double.parseDouble(rate.getText()),
                        Integer.parseInt(tenure.getText()));
                result.setText("<html>" + r.replace("|", "<br>") + "</html>");
                result.setForeground(r.startsWith("SUCCESS") ? SUCCESS : DANGER);
                setStatus(r, r.startsWith("SUCCESS"));
            } catch (Exception ex) { result.setText("Invalid input!"); result.setForeground(DANGER); }
        });
        p.add(btn); p.add(result);
        return wrap(p);
    }

    private JPanel createViewLoansPanel() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBackground(BG_PANEL);
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBackground(BG_PANEL);
        JTextField accNo = new JTextField(15); JButton btn = makeBtn("Load Loans", PRIMARY);
        top.add(new JLabel("Account Number:")); top.add(accNo); top.add(btn);
        JTextArea area = new JTextArea();
        area.setFont(new Font("Monospaced", Font.PLAIN, 12)); area.setEditable(false);
        btn.addActionListener(e -> {
            java.util.List<Loan> loans = loanService.getAccountLoans(accNo.getText().trim());
            if (loans.isEmpty()) { area.setText("No loans found."); return; }
            StringBuilder sb = new StringBuilder();
            for (Loan l : loans) sb.append(l).append("\n");
            area.setText(sb.toString());
        });
        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(area), BorderLayout.CENTER);
        return p;
    }

    private JPanel createPayEmiPanel() {
        JPanel p = formPanel();
        JTextField loanId = new JTextField(); JTextField accNo = new JTextField();
        JPasswordField pin = new JPasswordField(); JLabel result = new JLabel();
        addFormRow(p, "Loan ID:", loanId);
        addFormRow(p, "Account Number:", accNo);
        addFormRow(p, "PIN:", pin);
        JButton btn = makeBtn("Pay EMI", SUCCESS);
        btn.addActionListener(e -> {
            String r = loanService.makePayment(loanId.getText().trim(), accNo.getText().trim(), new String(pin.getPassword()));
            result.setText(r); result.setForeground(r.startsWith("SUCCESS") ? SUCCESS : DANGER);
            setStatus(r, r.startsWith("SUCCESS"));
        });
        p.add(btn); p.add(result);
        return wrap(p);
    }

    private JPanel createPendingLoansPanel() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBackground(BG_PANEL);
        String[] cols = {"Loan ID", "Account", "Type", "Amount", "EMI", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model); styleTable(table);

        Runnable refresh = () -> {
            model.setRowCount(0);
            for (Loan l : loanService.getPendingLoans()) {
                model.addRow(new Object[]{
                        l.getLoanId(), l.getAccountNumber(), l.getLoanType(),
                        String.format("PKR %.2f", l.getPrincipalAmount()),
                        String.format("PKR %.2f", l.getMonthlyInstallment()), l.getStatus()
                });
            }
        };

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        top.setBackground(BG_PANEL);
        JButton btnApprove = makeBtn("✔ Approve", SUCCESS);
        JButton btnReject  = makeBtn("✘ Reject", DANGER);
        JButton btnRefresh = makeBtn("↻ Refresh", Color.GRAY);
        top.add(btnApprove); top.add(btnReject); top.add(btnRefresh);

        btnRefresh.addActionListener(e -> refresh.run());
        btnApprove.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { showMsg("Select a loan!", false); return; }
            String r = loanService.approveLoan((String) model.getValueAt(row, 0));
            showMsg(r, r.startsWith("SUCCESS")); refresh.run();
        });
        btnReject.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { showMsg("Select a loan!", false); return; }
            String r = loanService.rejectLoan((String) model.getValueAt(row, 0));
            showMsg(r, r.startsWith("SUCCESS")); refresh.run();
        });

        refresh.run();
        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    // ════════════════════ REPORTS ════════════════════
    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_PANEL);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        JTextArea area = new JTextArea();
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setEditable(false);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btns.setBackground(BG_PANEL);
        JButton btnSummary = makeBtn("Bank Summary", PRIMARY);
        JButton btnAudit   = makeBtn("Audit Trail", SECONDARY);
        JButton btnBalance = makeBtn("Balance by Type", SUCCESS);
        JButton btnSortAsc  = makeBtn("Sort by Balance ▲ (Merge Sort)", PRIMARY);
        JButton btnSortDesc = makeBtn("Sort by Balance ▼ (Merge Sort)", SECONDARY);
        JButton btnBST      = makeBtn("BST Report", new Color(108, 99, 255));
        JButton btnRange    = makeBtn("Balance Range (BST)", new Color(253, 126, 20));
        btns.add(btnSummary); btns.add(btnAudit); btns.add(btnBalance);
        btns.add(btnSortAsc); btns.add(btnSortDesc); btns.add(btnBST); btns.add(btnRange);
        btnSummary.addActionListener(e -> area.setText(reportService.getBankSummary()));
        btnAudit.addActionListener(e -> area.setText(reportService.getAuditTrail(30)));
        btnBalance.addActionListener(e -> {
            StringBuilder sb = new StringBuilder("\n  Balance By Account Type (TreeMap):\n\n");
            for (Map.Entry<Accounts.AccountType, Double> en : reportService.getBalanceByAccountType().entrySet()) {
                sb.append(String.format("  %-20s : PKR %.2f%n", en.getKey(), en.getValue()));
            }
            area.setText(sb.toString());
        });
        btnSortAsc.addActionListener(e -> area.setText(reportService.getAccountsSortedByBalanceReport(true)));
        btnSortDesc.addActionListener(e -> area.setText(reportService.getAccountsSortedByBalanceReport(false)));
        btnBST.addActionListener(e -> area.setText(reportService.getBSTReport()));
        btnRange.addActionListener(e -> {
            JTextField min = new JTextField(), max = new JTextField();
            JPanel form = new JPanel(new GridLayout(2, 2, 5, 5));
            form.add(new JLabel("Min Balance:")); form.add(min);
            form.add(new JLabel("Max Balance:")); form.add(max);
            int result = JOptionPane.showConfirmDialog(this, form, "Balance Range Query (BST)", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    java.util.ArrayList<Accounts> list = reportService.getAccountsInBalanceRange(
                            Double.parseDouble(min.getText()), Double.parseDouble(max.getText()));
                    StringBuilder sb = new StringBuilder("\n  === ACCOUNTS IN BALANCE RANGE (BST Range Query) ===\n");
                    if (list.isEmpty()) sb.append("  No accounts found in this range.\n");
                    else { int i = 1; for (Accounts a : list) sb.append("  ").append(i++).append(". ").append(a).append("\n"); }
                    area.setText(sb.toString());
                } catch (Exception ex) { area.setText("Invalid input!"); }
            }
        });
        panel.add(btns, BorderLayout.NORTH);
        panel.add(new JScrollPane(area), BorderLayout.CENTER);
        return panel;
    }

    // ════════════════════ ADMIN PANEL ════════════════════
    private JPanel createAdminPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_PANEL);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        String[] cols = {"Admin ID", "Username", "Full Name", "Role", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model); styleTable(table);
        BankDatabase db = BankDatabase.getInstance();
        for (Admin a : db.getAllAdmins()) {
            model.addRow(new Object[]{a.getAdminId(), a.getUsername(), a.getFullName(), a.getRole(), a.isActive() ? "ACTIVE" : "INACTIVE"});
        }
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // ════════════════════ HELPERS ════════════════════
    private void showMsg(String msg, boolean success) {
        setStatus(msg, success);
        if (!success) JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.WARNING_MESSAGE);
    }

    private JButton makeBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg); btn.setForeground(WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(7, 14, 7, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel formPanel() {
        JPanel p = new JPanel(new GridLayout(0, 2, 8, 10));
        p.setBackground(WHITE);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        return p;
    }

    private void addFormRow(JPanel p, String label, JComponent field) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Arial", Font.BOLD, 13));
        p.add(lbl); p.add(field);
    }

    private JPanel wrap(JPanel inner) {
        JPanel outer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 30));
        outer.setBackground(BG_PANEL);
        inner.setPreferredSize(new Dimension(500, inner.getPreferredSize().height + 20));
        outer.add(inner);
        return outer;
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(26);
        table.setSelectionBackground(SECONDARY);
        table.setSelectionForeground(WHITE);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table.getTableHeader().setBackground(PRIMARY);
        table.getTableHeader().setForeground(WHITE);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }

    // ════════════════════ LOGIN DIALOG ════════════════════
    public static class LoginDialog extends JDialog {
        private static final Color PRIMARY = new Color(0, 82, 165);
        private static final Color WHITE   = Color.WHITE;

        public LoginDialog() {
            setTitle("SmartBank - Login");
            setSize(380, 280);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setResizable(false);
            buildUI();
        }

        private void buildUI() {
            JPanel panel = new JPanel(new BorderLayout());

            JPanel header = new JPanel();
            header.setBackground(PRIMARY);
            header.setPreferredSize(new Dimension(0, 70));
            JLabel title = new JLabel("🏦  SmartBank Login", JLabel.CENTER);
            title.setForeground(WHITE);
            title.setFont(new Font("Arial", Font.BOLD, 20));
            header.setLayout(new GridBagLayout());
            header.add(title);

            JPanel form = new JPanel(new GridLayout(3, 2, 10, 12));
            form.setBorder(new EmptyBorder(20, 30, 10, 30));
            JTextField userField = new JTextField();
            JPasswordField passField = new JPasswordField();
            form.add(new JLabel("Username:")); form.add(userField);
            form.add(new JLabel("Password:")); form.add(passField);
            form.add(new JLabel("Default: admin / admin123"));

            JButton btnLogin = new JButton("Login");
            btnLogin.setBackground(PRIMARY); btnLogin.setForeground(WHITE);
            btnLogin.setFont(new Font("Arial", Font.BOLD, 14));
            btnLogin.setPreferredSize(new Dimension(120, 36));

            JLabel errorLbl = new JLabel("", JLabel.CENTER);
            errorLbl.setForeground(Color.RED);

            JPanel bottom = new JPanel(new BorderLayout());
            bottom.setBorder(new EmptyBorder(0, 30, 15, 30));
            bottom.add(btnLogin, BorderLayout.CENTER);
            bottom.add(errorLbl, BorderLayout.SOUTH);

            ActionListener loginAction = e -> {
                BankDatabase db = BankDatabase.getInstance();
                Admin admin = db.authenticate(userField.getText().trim(), new String(passField.getPassword()));
                if (admin != null) {
                    dispose();
                    new BankGui(admin);
                } else {
                    errorLbl.setText("Invalid credentials!");
                    passField.setText("");
                }
            };
            btnLogin.addActionListener(loginAction);
            passField.addActionListener(loginAction);

            panel.add(header, BorderLayout.NORTH);
            panel.add(form, BorderLayout.CENTER);
            panel.add(bottom, BorderLayout.SOUTH);
            add(panel);
        }
    }
}
