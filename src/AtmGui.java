import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileInputStream;
import java.sql.*;
import java.util.Properties;

public class AtmGui extends JFrame implements ActionListener {

    JPanel loginPanel, menuPanel;
    JLabel lblBank, lblPin, lblMessage;
    JComboBox<String> cbBank;
    JPasswordField txtPin;
    JButton btnLogin, btnDeposit, btnWithdraw, btnCheckBalance, btnExit, btnHistory;

    Connection con;
    int accountId = -1;
    double currentBalance = 0;

    public AtmGui() {
        setTitle("ATM Simulator");
        setSize(500, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new CardLayout());

        try {
            connectDB();
        } catch (Exception e) {
            e.printStackTrace();
        }

        initLoginPanel();
        initMenuPanel();

        add(loginPanel, "Login");
        add(menuPanel, "Menu");

        setVisible(true);
    }

    public void connectDB() {
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

    private void initLoginPanel() {
        loginPanel = new JPanel();
        loginPanel.setLayout(null);
        loginPanel.setBackground(Color.LIGHT_GRAY);

        JLabel lblTitle = new JLabel("Welcome to ATM Simulator");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitle.setBounds(80, 20, 350, 30);
        loginPanel.add(lblTitle);

        lblBank = new JLabel("Select Bank:");
        lblBank.setBounds(50, 80, 100, 25);
        loginPanel.add(lblBank);

        cbBank = new JComboBox<>(new String[]{"SBI", "PNB", "Union"});
        cbBank.setBounds(160, 80, 150, 25);
        loginPanel.add(cbBank);

        lblPin = new JLabel("Enter 4-digit PIN:");
        lblPin.setBounds(50, 130, 120, 25);
        loginPanel.add(lblPin);

        txtPin = new JPasswordField();
        txtPin.setBounds(180, 130, 130, 25);
        loginPanel.add(txtPin);

        btnLogin = new JButton("Login");
        btnLogin.setBounds(180, 180, 100, 30);
        btnLogin.addActionListener(this);
        loginPanel.add(btnLogin);

        lblMessage = new JLabel("");
        lblMessage.setBounds(50, 230, 400, 25);
        lblMessage.setForeground(Color.RED);
        loginPanel.add(lblMessage);
    }

    private void initMenuPanel() {
        menuPanel = new JPanel();
        menuPanel.setLayout(null);
        menuPanel.setBackground(Color.WHITE);

        JLabel lblMenu = new JLabel("Select Operation");
        lblMenu.setFont(new Font("Arial", Font.BOLD, 18));
        lblMenu.setBounds(150, 30, 200, 30);
        menuPanel.add(lblMenu);

        btnDeposit = new JButton("Deposit");
        btnDeposit.setBounds(50, 80, 150, 40);
        btnDeposit.addActionListener(this);
        menuPanel.add(btnDeposit);

        btnWithdraw = new JButton("Withdraw");
        btnWithdraw.setBounds(250, 80, 150, 40);
        btnWithdraw.addActionListener(this);
        menuPanel.add(btnWithdraw);

        btnCheckBalance = new JButton("Check Balance");
        btnCheckBalance.setBounds(150, 150, 150, 40);
        btnCheckBalance.addActionListener(this);
        menuPanel.add(btnCheckBalance);

        btnHistory = new JButton("Transaction History");
        btnHistory.setBounds(150, 220, 150, 40);
        btnHistory.addActionListener(this);
        menuPanel.add(btnHistory);

        btnExit = new JButton("Exit");
        btnExit.setBounds(150, 290, 150, 40);
        btnExit.addActionListener(this);
        menuPanel.add(btnExit);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnLogin) {
            performLogin();
        } else if (e.getSource() == btnDeposit) {
            performDeposit();
        } else if (e.getSource() == btnWithdraw) {
            performWithdraw();
        } else if (e.getSource() == btnCheckBalance) {
            showBalance();
        } else if (e.getSource() == btnHistory) {
            showTransactionHistory();
        } else if (e.getSource() == btnExit) {
            JOptionPane.showMessageDialog(this, "Thank you for using ATM Simulator!");
            System.exit(0);
        }
    }

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

    private void performDeposit() {
        String input = JOptionPane.showInputDialog(this, "Enter amount to deposit:");
        if (input != null && !input.isEmpty()) {
            try {
                double amount = Double.parseDouble(input);
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(this, "❌ Amount must be positive!");
                    return;
                }
                currentBalance += amount;

                PreparedStatement ps = con.prepareStatement(
                        "UPDATE accounts SET balance=? WHERE id=?");
                ps.setDouble(1, currentBalance);
                ps.setInt(2, accountId);
                ps.executeUpdate();

                // Save transaction
                double newBalance = currentBalance;
                PreparedStatement ts = con.prepareStatement(
                    "INSERT INTO transactions (account_id, type, amount, balance_after) VALUES (?, ?, ?, ?)");
                ts.setInt(1, accountId);
                ts.setString(2, "Deposit");
                ts.setDouble(3, amount);
                ts.setDouble(4, newBalance);
                ts.executeUpdate();

                JOptionPane.showMessageDialog(this, "✅ Deposited: ₹" + amount +
                        "\nUpdated Balance: ₹" + currentBalance);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ Invalid amount!");
            }
        }
    }

    private void performWithdraw() {
        String input = JOptionPane.showInputDialog(this, "Enter amount to withdraw:");
        if (input != null && !input.isEmpty()) {
            try {
                double amount = Double.parseDouble(input);
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(this, "❌ Amount must be positive!");
                    return;
                }
                if (amount > currentBalance) {
                    JOptionPane.showMessageDialog(this, "❌ Insufficient balance!");
                    return;
                }
                currentBalance -= amount;

                PreparedStatement ps = con.prepareStatement(
                        "UPDATE accounts SET balance=? WHERE id=?");
                ps.setDouble(1, currentBalance);
                ps.setInt(2, accountId);
                ps.executeUpdate();

                // Save transaction
                PreparedStatement ts = con.prepareStatement(
                        "INSERT INTO transactions (account_id, type, amount) VALUES (?, ?, ?)");
                ts.setInt(1, accountId);
                ts.setString(2, "Withdraw");
                ts.setDouble(3, amount);
                ts.executeUpdate();

                JOptionPane.showMessageDialog(this, "✅ Withdrawn: ₹" + amount +
                        "\nUpdated Balance: ₹" + currentBalance);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ Invalid amount!");
            }
        }
    }

    private void showBalance() {
        JOptionPane.showMessageDialog(this, "💰 Current Balance: ₹" + currentBalance);
    }

    private void showTransactionHistory() {
        try {
            PreparedStatement ps = con.prepareStatement(
                "SELECT type, amount, date_time FROM transactions WHERE account_id=? ORDER BY date_time DESC LIMIT 5");
            ps.setInt(1, accountId);
            ResultSet rs = ps.executeQuery();

            StringBuilder history = new StringBuilder("<html><b>Last 5 Transactions:</b><br><br>");
            boolean hasRecords = false;
            while (rs.next()) {
                hasRecords = true;
                history.append(rs.getString("type"))
                       .append(" - ₹")
                       .append(rs.getDouble("amount"))
                       .append(" on ")
                       .append(rs.getTimestamp("date_time"))
                       .append("<br>");
            }
            if (!hasRecords) {
                history.append("No recent transactions found.<br>");
            }
            history.append("</html>");

            JOptionPane.showMessageDialog(this, new JLabel(history.toString()));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error fetching history: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AtmGui());
    }
}