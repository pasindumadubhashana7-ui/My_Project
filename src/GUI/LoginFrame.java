package GUI;

import Project.database;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class LoginFrame extends JFrame {
    private final JTextField emailField;
    private final JPasswordField passField;
    private final database db;

    // Modern Deep Slate & Indigo/Blue Palette
    private final Color COLOR_BG_START = new Color(11, 15, 25);
    private final Color COLOR_BG_END = new Color(30, 41, 59);
    private final Color COLOR_TEXT_PRIMARY = new Color(248, 250, 252);
    private final Color COLOR_TEXT_MUTED = new Color(148, 163, 184);
    private final Color COLOR_BORDER = new Color(51, 65, 85);
    private final Color COLOR_FOCUS_BORDER = new Color(99, 102, 241);
    private final Color COLOR_ACCENT = new Color(79, 70, 229);
    private final Color COLOR_ACCENT_HOVER = new Color(99, 102, 241);

    public LoginFrame() {
        db = new database();
        setTitle("Vehicle Rental System - Login");
        setSize(480, 560);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Gradient Background Root Panel
        JPanel rootPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, COLOR_BG_START, 0, getHeight(), COLOR_BG_END);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        rootPanel.setOpaque(false);
        setContentPane(rootPanel);

        // Center Rounded Card Container
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Outer glow shadow
                g2d.setColor(new Color(255, 255, 255, 12));
                g2d.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 28, 28);

                // Card background
                g2d.setColor(new Color(18, 25, 41, 240));
                g2d.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 26, 26);

                // Card border
                g2d.setColor(new Color(51, 65, 85, 180));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 26, 26);
                g2d.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(40, 40, 40, 40));
        card.setPreferredSize(new Dimension(400, 480));

        // Header Section (Centered)
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel iconLbl = new JLabel("🚗");
        iconLbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 32));
        iconLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        iconLbl.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel title = new JLabel("Welcome Back");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        title.setForeground(COLOR_TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel subtitle = new JLabel("Vehicle Rental System");
        subtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        subtitle.setForeground(COLOR_TEXT_MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);

        headerPanel.add(iconLbl);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        headerPanel.add(title);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        headerPanel.add(subtitle);

        card.add(headerPanel);
        card.add(Box.createRigidArea(new Dimension(0, 30)));

        // Form Section (Centered Labels & Inputs)
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);
        formPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel emailLbl = new JLabel("Email");
        emailLbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        emailLbl.setForeground(COLOR_TEXT_MUTED);
        emailLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        emailLbl.setHorizontalAlignment(SwingConstants.CENTER);

        emailField = createStyledTextField();

        JLabel passLbl = new JLabel("Password");
        passLbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        passLbl.setForeground(COLOR_TEXT_MUTED);
        passLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        passLbl.setHorizontalAlignment(SwingConstants.CENTER);

        passField = createStyledPasswordField();

        formPanel.add(emailLbl);
        formPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        formPanel.add(emailField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 18)));
        formPanel.add(passLbl);
        formPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        formPanel.add(passField);

        card.add(formPanel);
        card.add(Box.createRigidArea(new Dimension(0, 30)));

        // Buttons Section
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.Y_AXIS));
        btnPanel.setOpaque(false);
        btnPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton loginBtn = new JButton("Login") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2d.setColor(COLOR_ACCENT.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(COLOR_ACCENT_HOVER);
                } else {
                    g2d.setColor(COLOR_ACCENT);
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        loginBtn.setFocusPainted(false);
        loginBtn.setContentAreaFilled(false);
        loginBtn.setBorder(new EmptyBorder(12, 0, 12, 0));
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginBtn.addActionListener(e -> authenticate());

        JButton registerBtn = new JButton("Create New Account");
        registerBtn.setForeground(COLOR_TEXT_MUTED);
        registerBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        registerBtn.setFocusPainted(false);
        registerBtn.setContentAreaFilled(false);
        registerBtn.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        registerBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerBtn.addActionListener(e -> new RegisterFrame().setVisible(true));

        btnPanel.add(loginBtn);
        btnPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        btnPanel.add(registerBtn);

        card.add(btnPanel);

        rootPanel.add(card);
    }

    private JTextField createStyledTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        tf.setBackground(new Color(15, 23, 42));
        tf.setForeground(COLOR_TEXT_PRIMARY);
        tf.setCaretColor(Color.WHITE);
        tf.setAlignmentX(Component.CENTER_ALIGNMENT);
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        updateFieldBorder(tf, false);
        tf.addFocusListener(new FocusListener() {
            @Override public void focusGained(FocusEvent e) { updateFieldBorder(tf, true); }
            @Override public void focusLost(FocusEvent e) { updateFieldBorder(tf, false); }
        });
        return tf;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        pf.setBackground(new Color(15, 23, 42));
        pf.setForeground(COLOR_TEXT_PRIMARY);
        pf.setCaretColor(Color.WHITE);
        pf.setAlignmentX(Component.CENTER_ALIGNMENT);
        pf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        updateFieldBorder(pf, false);
        pf.addFocusListener(new FocusListener() {
            @Override public void focusGained(FocusEvent e) { updateFieldBorder(pf, true); }
            @Override public void focusLost(FocusEvent e) { updateFieldBorder(pf, false); }
        });
        return pf;
    }

    private void updateFieldBorder(JComponent c, boolean focused) {
        c.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(focused ? COLOR_FOCUS_BORDER : COLOR_BORDER, focused ? 2 : 1),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
    }

    private void authenticate() {
        String email = emailField.getText().trim();
        String pass = new String(passField.getPassword()).trim();

        if (email.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter email and password!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String query = "SELECT * FROM `users` WHERE `Email` = '" + email + "' AND `Password` = '" + pass + "'";
            ResultSet rs = db.getStatement().executeQuery(query);
            if (rs.next()) {
                int type = rs.getInt("Type");
                JOptionPane.showMessageDialog(this, "Login Successful!");
                dispose();
                if (type == 1) {
                    new AdminDashboard().setVisible(true);
                } else {
                    new ClientDashboard(email).setVisible(true);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid email or password!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}