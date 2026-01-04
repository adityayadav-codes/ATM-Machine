import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileInputStream;
import java.sql.*;
import java.util.Properties;

public class AtmGui extends JFrame implements ActionListener {

    // Panels
    JPanel loginPanel, menuPanel;
    JLabel lblBank, lblPin, lblMessage, lblUserInfo;
    JComboBox<String> cbBank;
    JPasswordField txtPin;
    JButton btnLogin, btnDeposit, btnWithdraw, btnCheck, btnHistory, btnExit;

    // Database and data variables
    Connection con;
    int accountId;
    double currentBalance;
    String currentBank;
    String currentUserName;
    String currentPhone;

    public AtmGui() {
        setTitle("ATM Simulator");
        setSize(450, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new CardLayout());

        connectDatabase();
        createLoginPanel();
        createMenuPanel();

        add(loginPanel, "Login");
        add(menuPanel, "Menu");

        setVisible(true);
    }

    //  DATABASE CONNECTION
    public void connectDatabase() {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream("config.properties"));
            System.out.println("✅ Properties loaded successfully!");

            String url = props.getProperty("db.url");
            String user = props.getProperty("db.user");
            String password = props.getProperty("db.password");

            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(url, user, password);
            System.out.println("✅ Connected to DB successfully!");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Database connection failed!\n" + e.getMessage());
            System.exit(0);
        }
    }

    // 🔹 LOGIN PANEL
    void createLoginPanel() {
        loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        loginPanel.setBackground(Color.white);

        lblBank = new JLabel("Select Bank:");
        lblPin = new JLabel("Enter PIN:");
        lblMessage = new JLabel("", SwingConstants.CENTER);
        lblMessage.setForeground(Color.RED);

        cbBank = new JComboBox<>(new String[]{"SBI", "PNB", "Union"});
        txtPin = new JPasswordField(10);
        btnLogin = new JButton("Login");
        btnLogin.addActionListener(this);

        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0; gbc.gridy = 0; loginPanel.add(lblBank, gbc);
        gbc.gridx = 1; loginPanel.add(cbBank, gbc);
        gbc.gridx = 0; gbc.gridy = 1; loginPanel.add(lblPin, gbc);
        gbc.gridx = 1; loginPanel.add(txtPin, gbc);
        gbc.gridy = 2; gbc.gridwidth = 2; loginPanel.add(btnLogin, gbc);
        gbc.gridy = 3; loginPanel.add(lblMessage, gbc);
    }

    // 🔹 MENU PANEL
    void createMenuPanel() {
        menuPanel = new JPanel(new BorderLayout());
        JPanel btnPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(20, 60, 20, 60));

        lblUserInfo = new JLabel("Welcome!", SwingConstants.CENTER);
        lblUserInfo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblUserInfo.setForeground(Color.DARK_GRAY);

        btnDeposit = new JButton("Deposit");
        btnWithdraw = new JButton("Withdraw");
        btnCheck = new JButton("Check Balance");
        btnHistory = new JButton("Transaction History");
        btnExit = new JButton("Exit");

        for (JButton b : new JButton[]{btnDeposit, btnWithdraw, btnCheck, btnHistory, btnExit}) {
            b.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            b.addActionListener(this);
            btnPanel.add(b);
        }

        menuPanel.add(lblUserInfo, BorderLayout.NORTH);
        menuPanel.add(btnPanel, BorderLayout.CENTER);
    }

    // 🔹 LOGIN LOGIC
    private void performLogin() {
        String selectedBank = cbBank.getSelectedItem().toString();
        String pinText = new String(txtPin.getPassword()).trim();

        if (pinText.length() != 4) {
            lblMessage.setText("❌ PIN must be 4 digits!");
            return;
        }

        try {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM accounts WHERE bank_name=? AND pin=?");
            ps.setString(1, selectedBank);
            ps.setInt(2, Integer.parseInt(pinText));
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                accountId = rs.getInt("id");
                currentBalance = rs.getDouble("balance");
                currentBank = rs.getString("bank_name");

                // Fetch user details
                PreparedStatement ps2 = con.prepareStatement(
                        "SELECT name, phone FROM users WHERE account_id=?");
                ps2.setInt(1, accountId);
                ResultSet rs2 = ps2.executeQuery();

                if (rs2.next()) {
                    currentUserName = rs2.getString("name");
                    currentPhone = rs2.getString("phone");
                } else {
                    currentUserName = "User";
                    currentPhone = "N/A";
                }

                lblUserInfo.setText("Welcome, " + currentUserName +
                        " | Bank: " + currentBank +
                        " | Phone: " + currentPhone +
                        " | Balance: ₹" + currentBalance);

                lblMessage.setText("");
                CardLayout cl = (CardLayout) getContentPane().getLayout();
                cl.show(getContentPane(), "Menu");
            } else {
                lblMessage.setText("❌ Invalid PIN! Try again.");
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Login Error: " + ex.getMessage());
        }
    }

    // 🔹 ACTION HANDLER
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnLogin) {
            performLogin();
        } else if (e.getSource() == btnDeposit) {
            String amountStr = JOptionPane.showInputDialog(this, "Enter amount to deposit:");
            if (amountStr != null && !amountStr.isEmpty()) {
                double amt = Double.parseDouble(amountStr);
                currentBalance += amt;
                updateBalance();
                saveTransaction("Deposit", amt);
                JOptionPane.showMessageDialog(this, "✅ ₹" + amt + " deposited successfully!");
            }
        } else if (e.getSource() == btnWithdraw) {
            String amountStr = JOptionPane.showInputDialog(this, "Enter amount to withdraw:");
            if (amountStr != null && !amountStr.isEmpty()) {
                double amt = Double.parseDouble(amountStr);
                if (amt <= currentBalance) {
                    currentBalance -= amt;
                    updateBalance();
                    saveTransaction("Withdraw", amt);
                    JOptionPane.showMessageDialog(this, "✅ ₹" + amt + " withdrawn successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "❌ Insufficient balance!");
                }
            }
        } else if (e.getSource() == btnCheck) {
            JOptionPane.showMessageDialog(this, "💰 Current Balance: ₹" + currentBalance);
        } else if (e.getSource() == btnHistory) {
            showTransactionHistory();
        } else if (e.getSource() == btnExit) {
            System.exit(0);
        }
    }

    // 🔹 Update balance in database and info bar
    void updateBalance() {
        try {
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE accounts SET balance=? WHERE id=?");
            ps.setDouble(1, currentBalance);
            ps.setInt(2, accountId);
            ps.executeUpdate();

            lblUserInfo.setText("👋 Welcome, " + currentUserName +
                    " | Bank: " + currentBank +
                    " | Phone: " + currentPhone +
                    " | Balance: ₹" + currentBalance);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating balance: " + e.getMessage());
        }
    }

    // 🔹 Save transaction with balance_after
void saveTransaction(String type, double amount) {
    try {
        PreparedStatement ps = con.prepareStatement(
            "INSERT INTO transactions (account_id, type, amount, balance_after, date_time) VALUES (?, ?, ?, ?, NOW())"
        );
        ps.setInt(1, accountId);
        ps.setString(2, type);
        ps.setDouble(3, amount);
        ps.setDouble(4, currentBalance);  // 👈 store updated balance after each transaction
        ps.executeUpdate();
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error saving transaction: " + e.getMessage());
    }
}
    //

    // 🔹 Show last 5 transactions
    void showTransactionHistory() {
        try {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT type, amount, date_time FROM transactions WHERE account_id=? ORDER BY date_time DESC LIMIT 5");
            ps.setInt(1, accountId);
            ResultSet rs = ps.executeQuery();

            StringBuilder sb = new StringBuilder("🧾 Last 5 Transactions:\n\n");
            while (rs.next()) {
                sb.append(rs.getString("date_time")).append(" → ")
                        .append(rs.getString("type")).append(" ₹")
                        .append(rs.getDouble("amount")).append("\n");
            }

            JOptionPane.showMessageDialog(this, sb.toString());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching history: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new AtmGui();
    }
}