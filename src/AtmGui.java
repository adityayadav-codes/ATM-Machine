
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;
import javax.swing.Timer;

public class AtmGui extends JFrame {

    // panels
    private JPanel rootPanel;               
    private CardLayout cardLayout;
    private SlidePanel loginPanel, menuPanel, depositPanel, withdrawPanel, balancePanel;

    // login components
    private JComboBox<String> cbBank;
    private JPasswordField txtPin;
    private FancyButton btnLogin;

    // menu buttons
    private FancyButton btnDeposit, btnWithdraw, btnCheckBalance, btnHistory, btnExit, btnLogout;

    // DB
    private Connection con;

    // session
    private int accountId = -1;
    private double currentBalance = 0.0;

    // animation stiffness
    private static final int SLIDE_ANIM_MS = 14;
    private static final int SLIDE_STEP = 40;

    public AtmGui() {
        loadLookAndFeel();
        loadDBConnection();

        initUI();
        setFullScreen();
        setVisible(true);
    }

    private void loadLookAndFeel() {
        try {
            // optional: platform LAF
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
    }

    
// db connection loader
public void loadDBConnection() {
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

//
    private void initUI() {
        // root
        cardLayout = new CardLayout();
        rootPanel = new JPanel(cardLayout);
        rootPanel.setBackground(darkColor());

        // panels
        loginPanel = createLoginPanel();
        menuPanel = createMenuPanel();
        depositPanel = createDepositPanel();
        withdrawPanel = createWithdrawPanel();
        balancePanel = createBalancePanel();

        rootPanel.add(loginPanel, "login");
        rootPanel.add(menuPanel, "menu");
        rootPanel.add(depositPanel, "deposit");
        rootPanel.add(withdrawPanel, "withdraw");
        rootPanel.add(balancePanel, "balance");

        add(rootPanel);
    }

    // ---------- Panel creators ----------

    private SlidePanel createLoginPanel() {
        SlidePanel p = new SlidePanel();
        p.setLayout(new GridBagLayout());
        p.setBackground(darkColor());

        JPanel card = new JPanel();
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(30, 30, 30, 30));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("ATM");
        title.setFont(new Font("Dialog", Font.BOLD, 40));
        title.setForeground(orangeGlow());
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(new EmptyBorder(0,0,12,0));
        card.add(title);

        JLabel sub = new JLabel("Welcome — Please login");
        sub.setForeground(Color.LIGHT_GRAY);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        sub.setBorder(new EmptyBorder(0,0,20,0));
        card.add(sub);

        // bank selector
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        row1.setOpaque(false);
        JLabel lb = new JLabel("Bank:");
        lb.setForeground(Color.WHITE);
        cbBank = new JComboBox<>(new String[]{"SBI", "PNB", "Union"});
        cbBank.setPreferredSize(new Dimension(240, 36));
        cbBank.setBackground(Color.DARK_GRAY);
        cbBank.setForeground(Color.WHITE);
        row1.add(lb); row1.add(cbBank);
        card.add(row1);
        card.add(Box.createVerticalStrut(12));

        // pin
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        row2.setOpaque(false);
        JLabel lp = new JLabel("PIN:");
        lp.setForeground(Color.WHITE);
        txtPin = new JPasswordField();
        txtPin.setPreferredSize(new Dimension(240, 36));
        row2.add(lp); row2.add(txtPin);
        card.add(row2);
        card.add(Box.createVerticalStrut(18));

        // login button
        btnLogin = new FancyButton("Insert Card / Login");
        btnLogin.setPreferredSize(new Dimension(320, 48));
        btnLogin.addActionListener(ae -> {
            performLogin();
        });
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(btnLogin);

        // footer hint
        JLabel tip = new JLabel("<html><center>Use your 4-digit PIN<br>Or use test accounts in DB</center></html>");
        tip.setForeground(Color.GRAY);
        tip.setBorder(new EmptyBorder(18,0,0,0));
        tip.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(tip);

        p.add(card);
        return p;
    }

    private SlidePanel createMenuPanel() {
        SlidePanel p = new SlidePanel();
        p.setLayout(new BorderLayout());
        p.setBackground(darkColor());

        // header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Account Menu");
        title.setFont(new Font("Dialog", Font.BOLD, 28));
        title.setForeground(orangeGlow());
        title.setBorder(new EmptyBorder(24,24,12,24));
        header.add(title, BorderLayout.WEST);

        btnLogout = new FancyButton("Logout");
        btnLogout.setPreferredSize(new Dimension(120,40));
        btnLogout.addActionListener(ae -> {
            accountId = -1;
            currentBalance = 0;
            slideTo("login", SlideDirection.RIGHT);
        });
        JPanel hdrRight = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        hdrRight.setOpaque(false);
        hdrRight.add(btnLogout);
        header.add(hdrRight, BorderLayout.EAST);

        p.add(header, BorderLayout.NORTH);

        // center grid of big buttons
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(14, 14, 14, 14);
        gbc.ipadx = 20; gbc.ipady = 20;
        gbc.gridx = 0; gbc.gridy = 0;

        btnDeposit = new FancyButton("Deposit");
        btnDeposit.setPreferredSize(new Dimension(260, 70));
        btnDeposit.addActionListener(ae -> slideTo("deposit", SlideDirection.LEFT));
        center.add(btnDeposit, gbc);

        gbc.gridx = 1;
        btnWithdraw = new FancyButton("Withdraw");
        btnWithdraw.setPreferredSize(new Dimension(260, 70));
        btnWithdraw.addActionListener(ae -> slideTo("withdraw", SlideDirection.LEFT));
        center.add(btnWithdraw, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        btnCheckBalance = new FancyButton("Check Balance");
        btnCheckBalance.setPreferredSize(new Dimension(260, 70));
        btnCheckBalance.addActionListener(ae -> {
            showBalance();
            slideTo("balance", SlideDirection.LEFT);
        });
        center.add(btnCheckBalance, gbc);

        gbc.gridx = 1;
        btnHistory = new FancyButton("Transaction History");
        btnHistory.setPreferredSize(new Dimension(260, 70));
        btnHistory.addActionListener(ae -> {
            showHistory();
        });
        center.add(btnHistory, gbc);

        p.add(center, BorderLayout.CENTER);

        // footer exit
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        btnExit = new FancyButton("Exit");
        btnExit.setPreferredSize(new Dimension(160, 48));
        btnExit.addActionListener(ae -> {
            System.exit(0);
        });
        footer.add(btnExit);
        p.add(footer, BorderLayout.SOUTH);

        return p;
    }

    private SlidePanel createDepositPanel() {
        SlidePanel p = new SlidePanel();
        p.setLayout(new GridBagLayout());
        p.setBackground(darkColor());

        JPanel card = new JPanel();
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(18,18,18,18));

        JLabel lbl = new JLabel("Deposit Amount");
        lbl.setForeground(orangeGlow());
        lbl.setFont(new Font("Dialog", Font.BOLD, 26));
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lbl);
        card.add(Box.createVerticalStrut(14));

        JTextField tfAmount = new JTextField();
        tfAmount.setMaximumSize(new Dimension(360, 44));
        card.add(tfAmount);
        card.add(Box.createVerticalStrut(12));

        FancyButton ok = new FancyButton("Confirm Deposit");
        ok.setAlignmentX(Component.CENTER_ALIGNMENT);
        ok.addActionListener(ae -> {
            try {
                double amount = Double.parseDouble(tfAmount.getText().trim());
                if (amount <= 0) throw new NumberFormatException();
                performDeposit(amount);
                tfAmount.setText("");
                slideTo("menu", SlideDirection.RIGHT);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter a valid positive amount.", "Invalid", JOptionPane.ERROR_MESSAGE);
            }
        });
        card.add(ok);
        card.add(Box.createVerticalStrut(8));

        FancyButton back = new FancyButton("Back");
        back.setAlignmentX(Component.CENTER_ALIGNMENT);
        back.addActionListener(ae -> slideTo("menu", SlideDirection.RIGHT));
        card.add(back);

        p.add(card);
        return p;
    }

    private SlidePanel createWithdrawPanel() {
        SlidePanel p = new SlidePanel();
        p.setLayout(new GridBagLayout());
        p.setBackground(darkColor());

        JPanel card = new JPanel();
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(18,18,18,18));

        JLabel lbl = new JLabel("Withdraw Amount");
        lbl.setForeground(orangeGlow());
        lbl.setFont(new Font("Dialog", Font.BOLD, 26));
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lbl);
        card.add(Box.createVerticalStrut(14));

        JTextField tfAmount = new JTextField();
        tfAmount.setMaximumSize(new Dimension(360, 44));
        card.add(tfAmount);
        card.add(Box.createVerticalStrut(12));

        FancyButton ok = new FancyButton("Confirm Withdraw");
        ok.setAlignmentX(Component.CENTER_ALIGNMENT);
        ok.addActionListener(ae -> {
            try {
                double amount = Double.parseDouble(tfAmount.getText().trim());
                if (amount <= 0) throw new NumberFormatException();
                performWithdraw(amount);
                tfAmount.setText("");
                slideTo("menu", SlideDirection.RIGHT);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter a valid positive amount.", "Invalid", JOptionPane.ERROR_MESSAGE);
            }
        });
        card.add(ok);
        card.add(Box.createVerticalStrut(8));

        FancyButton back = new FancyButton("Back");
        back.setAlignmentX(Component.CENTER_ALIGNMENT);
        back.addActionListener(ae -> slideTo("menu", SlideDirection.RIGHT));
        card.add(back);

        p.add(card);
        return p;
    }

    private SlidePanel createBalancePanel() {
        SlidePanel p = new SlidePanel();
        p.setLayout(new GridBagLayout());
        p.setBackground(darkColor());

        JPanel card = new JPanel();
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(18,18,18,18));

        JLabel lbl = new JLabel("Balance");
        lbl.setForeground(orangeGlow());
        lbl.setFont(new Font("Dialog", Font.BOLD, 26));
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lbl);
        card.add(Box.createVerticalStrut(18));

        JLabel bal = new JLabel(); // runtime updated
        bal.setFont(new Font("Dialog", Font.PLAIN, 22));
        bal.setForeground(Color.WHITE);
        bal.setAlignmentX(Component.CENTER_ALIGNMENT);
        bal.setName("balanceLabel");
        card.add(bal);
        card.add(Box.createVerticalStrut(20));

        FancyButton back = new FancyButton("Back");
        back.setAlignmentX(Component.CENTER_ALIGNMENT);
        back.addActionListener(ae -> slideTo("menu", SlideDirection.RIGHT));
        card.add(back);

        p.add(card);
        return p;
    }

    // ---------- Helper UI methods ----------

    private Color darkColor() {
        return new Color(18, 18, 20);
    }

    private Color orangeGlow() {
        return new Color(255, 140, 45);
    }

    // slide to panel with animation
    private enum SlideDirection { LEFT, RIGHT }

    private void slideTo(String name, SlideDirection dir) {
        Component current = getVisibleCard();
        Component next = findCardComponent(name);
        if (current == next) return;

        // set next invisible in layer and add to glasspane as we animate
        Point start = next.getLocation();
        Dimension size = rootPanel.getSize();

        int fromX = (dir == SlideDirection.LEFT) ? size.width : -size.width;
        int toX = 0;

        next.setLocation(fromX, 0);
        next.setVisible(true);

        Timer t = new Timer(SLIDE_ANIM_MS, null);
        t.addActionListener(new ActionListener() {
            int x = fromX;
            public void actionPerformed(ActionEvent e) {
                if (dir == SlideDirection.LEFT) x -= SLIDE_STEP;
                else x += SLIDE_STEP;
                if ((dir == SlideDirection.LEFT && x <= toX) || (dir == SlideDirection.RIGHT && x >= toX)) {
                    next.setLocation(0,0);
                    current.setVisible(false);
                    cardLayout.show(rootPanel, name);
                    t.stop();
                } else {
                    next.setLocation(x, 0);
                    // move current in opposite direction slightly to create parallax
                    int curX = x + ((dir == SlideDirection.LEFT) ? -size.width : size.width);
                    current.setLocation(curX, 0);
                }
            }
        });
        t.start();
        // ensure card layout will show the right component after animation
        cardLayout.show(rootPanel, name);
    }

    private Component getVisibleCard() {
        for (Component c : rootPanel.getComponents()) {
            if (c.isVisible()) return c;
        }
        // fallback
        return rootPanel.getComponent(0);
    }

    private Component findCardComponent(String name) {
        for (Component c : rootPanel.getComponents()) {
            if (name.equals(rootPanel.getComponentZOrder(c) >= 0 ? getNameForComponent(c) : null)) {
                return c;
            }
        }
        // fallback by matching class or panel type
        for (Component c : rootPanel.getComponents()) {
            if (c instanceof SlidePanel) {
                // crude: map by reference
                if (name.equals("login") && c == loginPanel) return c;
                if (name.equals("menu") && c == menuPanel) return c;
                if (name.equals("deposit") && c == depositPanel) return c;
                if (name.equals("withdraw") && c == withdrawPanel) return c;
                if (name.equals("balance") && c == balancePanel) return c;
            }
        }
        return loginPanel;
    }

    private String getNameForComponent(Component c) {
        if (c == loginPanel) return "login";
        if (c == menuPanel) return "menu";
        if (c == depositPanel) return "deposit";
        if (c == withdrawPanel) return "withdraw";
        if (c == balancePanel) return "balance";
        return null;
    }

    private void setFullScreen() {
        setUndecorated(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        if (gd.isFullScreenSupported()) {
            gd.setFullScreenWindow(this);
        } else {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
            setVisible(true);
        }
        // center root panel sizing
        setLayout(new BorderLayout());
        add(rootPanel, BorderLayout.CENTER);
        validate();
    }

    // ---------- Business & DB Logic (kept / adapted) ----------

    private void performLogin() {
        String bank = (String) cbBank.getSelectedItem();
        String pin = new String(txtPin.getPassword()).trim();
        if (pin.length() == 0) {
            JOptionPane.showMessageDialog(this, "Enter PIN", "Login", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            PreparedStatement ps = con.prepareStatement("SELECT id, balance FROM accounts WHERE bank_name=? AND pin=?");
            ps.setString(1, bank);
            ps.setString(2, pin);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                accountId = rs.getInt("id");
                currentBalance = rs.getDouble("balance");
                txtPin.setText("");
                slideTo("menu", SlideDirection.LEFT);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials", "Login failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Login error: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performDeposit(double amount) {
        if (accountId < 0) {
            JOptionPane.showMessageDialog(this, "No account logged in", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            currentBalance += amount;
            PreparedStatement ps = con.prepareStatement("UPDATE accounts SET balance=? WHERE id=?");
            ps.setDouble(1, currentBalance);
            ps.setInt(2, accountId);
            ps.executeUpdate();

            PreparedStatement ts = con.prepareStatement(
                    "INSERT INTO transactions (account_id, type, amount, balance_after) VALUES (?, ?, ?, ?)");
            ts.setInt(1, accountId);
            ts.setString(2, "Deposit");
            ts.setDouble(3, amount);
            ts.setDouble(4, currentBalance);
            ts.executeUpdate();

            JOptionPane.showMessageDialog(this, "Deposited: ₹" + amount + "\nNew balance: ₹" + currentBalance,
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Deposit failed: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performWithdraw(double amount) {
        if (accountId < 0) {
            JOptionPane.showMessageDialog(this, "No account logged in", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            if (amount > currentBalance) {
                JOptionPane.showMessageDialog(this, "Insufficient funds", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            currentBalance -= amount;
            PreparedStatement ps = con.prepareStatement("UPDATE accounts SET balance=? WHERE id=?");
            ps.setDouble(1, currentBalance);
            ps.setInt(2, accountId);
            ps.executeUpdate();

            PreparedStatement ts = con.prepareStatement(
                    "INSERT INTO transactions (account_id, type, amount, balance_after) VALUES (?, ?, ?, ?)");
            ts.setInt(1, accountId);
            ts.setString(2, "Withdraw");
            ts.setDouble(3, amount);
            ts.setDouble(4, currentBalance);
            ts.executeUpdate();

            JOptionPane.showMessageDialog(this, "Withdrawn: ₹" + amount + "\nNew balance: ₹" + currentBalance,
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Withdraw failed: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showBalance() {
        // find the balance label in balancePanel and update text
        JLabel label = findBalanceLabel(balancePanel);
        if (label != null) {
            label.setText(String.format("₹ %.2f", currentBalance));
        }
    }

    private JLabel findBalanceLabel(Container c) {
        for (Component comp : c.getComponents()) {
            if (comp instanceof JLabel && "balanceLabel".equals(comp.getName())) return (JLabel) comp;
            if (comp instanceof Container) {
                JLabel r = findBalanceLabel((Container) comp);
                if (r != null) return r;
            }
        }
        return null;
    }

    private void showHistory() {
        if (accountId < 0) {
            JOptionPane.showMessageDialog(this, "No account logged in", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT type, amount, balance_after, timestamp FROM transactions WHERE account_id=? ORDER BY timestamp DESC LIMIT 20");
            ps.setInt(1, accountId);
            ResultSet rs = ps.executeQuery();
            StringBuilder sb = new StringBuilder("<html><body style='width:360px'>");
            boolean found = false;
            while (rs.next()) {
                found = true;
                String type = rs.getString("type");
                double amount = rs.getDouble("amount");
                double balAfter = rs.getDouble("balance_after");
                Timestamp ts = rs.getTimestamp("timestamp");
                sb.append(String.format("<b>%s</b> ₹%.2f — Bal: ₹%.2f<br><small>%s</small><hr>",
                        type, amount, balAfter, ts == null ? "" : ts.toString()));
            }
            if (!found) sb.append("<i>No recent transactions</i>");
            sb.append("</body></html>");
            JLabel label = new JLabel(sb.toString());
            label.setForeground(Color.WHITE);
            JOptionPane.showMessageDialog(this, label, "Transaction History", JOptionPane.PLAIN_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "History fetch error: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------- Small custom components for style ----------

    /**
     * FancyButton: dark rectangular button with orange glow, hover and pressed effects.
     */
    private class FancyButton extends JButton {
        private Color base = new Color(40, 40, 42);
        private Color glow = orangeGlow();
        private Runnable action;

        FancyButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setFont(new Font("Dialog", Font.BOLD, 16));
            setForeground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { repaint(); }
                public void mouseExited(MouseEvent e) { repaint(); }
                public void mousePressed(MouseEvent e) { repaint(); }
                public void mouseReleased(MouseEvent e) { repaint(); }
            });
            addActionListener(e -> { if (action != null) action.run(); });
        }

        void addActionListener(Runnable r) {
            this.action = r;
        }

        @Override
        protected void paintComponent(Graphics g) {
            int w = getWidth(), h = getHeight();
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            boolean hover = getModel().isRollover();
            boolean pressed = getModel().isPressed();

            // background
            RoundRectangle2D.Float r = new RoundRectangle2D.Float(0, 0, w, h, 14, 14);
            g2.setColor(base);
            g2.fill(r);

            // glow layer on hover
            if (hover) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.18f));
                GradientPaint gp = new GradientPaint(0,0, glow, w, h, new Color(0,0,0,0));
                g2.setPaint(gp);
                g2.fill(r);
            }

            // pressed overlay
            if (pressed) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.12f));
                g2.setColor(Color.BLACK);
                g2.fill(r);
            }

            // text
            g2.setComposite(AlphaComposite.SrcOver);
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(getText());
            g2.setColor(Color.WHITE);
            g2.drawString(getText(), (w - tw) / 2, (h + fm.getAscent() - fm.getDescent()) / 2);

            g2.dispose();
        }
    }

    /**
     * SlidePanel is a JPanel that allows us to move it by setLocation for animation.
     */
    private class SlidePanel extends JPanel {
        SlidePanel() {
            setLayout(new BorderLayout());
            setOpaque(true);
            setBackground(darkColor());
        }
    }

    // ---------- Main ----------

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AtmGui());
    }
}