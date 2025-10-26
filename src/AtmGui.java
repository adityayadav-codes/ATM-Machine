import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class AtmGui extends JFrame implements ActionListener {

    // Panels
    JPanel loginPanel, menuPanel;
    JLabel lblBank, lblPin, lblMessage;
    JComboBox<String> cbBank;
    JPasswordField txtPin;
    JButton btnLogin, btnDeposit, btnWithdraw, btnCheckBalance, btnExit;

    // JDBC variables
    Connection con;
    int accountId = -1;
    double currentBalance = 0;

    public AtmGui() {
        setTitle("ATM Simulator");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new CardLayout());

        // Connect to DB
        connectDB();

        // Initialize panels
        initLoginPanel();
        initMenuPanel();

        // Show login panel first
        add(loginPanel, "Login");
        add(menuPanel, "Menu");

        setVisible(true);
    }

    private void connectDB() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");  // Driver load ho raha hai
Connection con = DriverManager.getConnection(
    "jdbc:mysql://localhost:3306/atm_db", "root", "Ady@1234");
          
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database Connection Failed!\n" + e.getMessage());
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

        btnExit = new JButton("Exit");
        btnExit.setBounds(150, 220, 150, 40);
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

                // Update DB
                PreparedStatement ps = con.prepareStatement(
                        "UPDATE accounts SET balance=? WHERE id=?");
                ps.setDouble(1, currentBalance);
                ps.setInt(2, accountId);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "✅ Deposited: " + amount +
                        "\nUpdated Balance: " + currentBalance);
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

                // Update DB
                PreparedStatement ps = con.prepareStatement(
                        "UPDATE accounts SET balance=? WHERE id=?");
                ps.setDouble(1, currentBalance);
                ps.setInt(2, accountId);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "✅ Withdrawn: " + amount +
                        "\nUpdated Balance: " + currentBalance);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ Invalid amount!");
            }
        }
    }

    private void showBalance() {
        JOptionPane.showMessageDialog(this, "💰 Current Balance: " + currentBalance);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AtmGui());
    }
}