package GUI;

import Project.database;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.sql.SQLException;
import java.util.UUID;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class RegisterFrame extends JFrame {
    private JTextField fnameField, lnameField, emailField, phoneField;
    private JPasswordField passField;
    private JComboBox<String> typeCombo;
    private database db;

    // Modern Deep Slate & Indigo/Blue Palette
    private final Color COLOR_BG_START = new Color(11, 15, 25);
    private final Color COLOR_BG_END = new Color(30, 41, 59);
    private final Color COLOR_TEXT_PRIMARY = new Color(248, 250, 252);
    private final Color COLOR_TEXT_MUTED = new Color(148, 163, 184);
    private final Color COLOR_BORDER = new Color(51, 65, 85);
    private final Color COLOR_FOCUS_BORDER = new Color(99, 102, 241);
    private final Color COLOR_ACCENT = new Color(79, 70, 229);
    private final Color COLOR_ACCENT_HOVER = new Color(99, 102, 241);

    public RegisterFrame() {
        db = new database();
        setTitle("Register New Account");
        setSize(480, 720);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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
        card.setBorder(new EmptyBorder(35, 40, 35, 40));
        card.setPreferredSize(new Dimension(410, 640));

        // Header Section (Centered)
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel iconLbl = new JLabel("✨");
        iconLbl.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        iconLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        iconLbl.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel title = new JLabel("Create Account");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(COLOR_TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel subtitle = new JLabel("Join the vehicle rental portal");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(COLOR_TEXT_MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);

        headerPanel.add(iconLbl);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        headerPanel.add(title);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        headerPanel.add(subtitle);

        card.add(headerPanel);
        card.add(Box.createRigidArea(new Dimension(0, 20)));

        // Form Section (Centered Labels & Inputs)
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);
        formPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        fnameField = addFormField(formPanel, "First Name");
        lnameField = addFormField(formPanel, "Last Name");
        emailField = addFormField(formPanel, "Email");
        phoneField = addFormField(formPanel, "Phone");
        
        // Password Field
        formPanel.add(createFieldLabel("Password"));
        formPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        passField = createStyledPasswordField();
        formPanel.add(passField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        // Account Type Combo Box
        formPanel.add(createFieldLabel("Account Type"));
        formPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        typeCombo = new JComboBox<>(new String[]{"Client (0)", "Admin (1)"});
        styleComboBox(typeCombo);
        formPanel.add(typeCombo);

        card.add(formPanel);
        card.add(Box.createRigidArea(new Dimension(0, 20)));

        // Buttons Section
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.Y_AXIS));
        btnPanel.setOpaque(false);
        btnPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton saveBtn = new JButton("Register") {
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
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveBtn.setFocusPainted(false);
        saveBtn.setContentAreaFilled(false);
        saveBtn.setBorder(new EmptyBorder(12, 0, 12, 0));
        saveBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.addActionListener(e -> registerUser());

        // Back Button
        JButton backBtn = new JButton("← Back to Login");
        backBtn.setForeground(COLOR_TEXT_MUTED);
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        backBtn.setFocusPainted(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        backBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        btnPanel.add(saveBtn);
        btnPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        btnPanel.add(backBtn);

        card.add(btnPanel);

        rootPanel.add(card);
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(COLOR_TEXT_MUTED);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        return lbl;
    }

    private JTextField addFormField(JPanel parent, String labelText) {
        parent.add(createFieldLabel(labelText));
        parent.add(Box.createRigidArea(new Dimension(0, 4)));
        JTextField tf = createStyledTextField();
        parent.add(tf);
        parent.add(Box.createRigidArea(new Dimension(0, 10)));
        return tf;
    }

    private JTextField createStyledTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tf.setBackground(new Color(15, 23, 42));
        tf.setForeground(COLOR_TEXT_PRIMARY);
        tf.setCaretColor(Color.WHITE);
        tf.setAlignmentX(Component.CENTER_ALIGNMENT);
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        updateFieldBorder(tf, false);
        tf.addFocusListener(new FocusListener() {
            @Override public void focusGained(FocusEvent e) { updateFieldBorder(tf, true); }
            @Override public void focusLost(FocusEvent e) { updateFieldBorder(tf, false); }
        });
        return tf;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pf.setBackground(new Color(15, 23, 42));
        pf.setForeground(COLOR_TEXT_PRIMARY);
        pf.setCaretColor(Color.WHITE);
        pf.setAlignmentX(Component.CENTER_ALIGNMENT);
        pf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        updateFieldBorder(pf, false);
        pf.addFocusListener(new FocusListener() {
            @Override public void focusGained(FocusEvent e) { updateFieldBorder(pf, true); }
            @Override public void focusLost(FocusEvent e) { updateFieldBorder(pf, false); }
        });
        return pf;
    }

    private void styleComboBox(JComboBox<String> cb) {
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cb.setBackground(new Color(15, 23, 42));
        cb.setForeground(COLOR_TEXT_PRIMARY);
        cb.setAlignmentX(Component.CENTER_ALIGNMENT);
        cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cb.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER, 1),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));
    }

    private void updateFieldBorder(JComponent c, boolean focused) {
        c.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(focused ? COLOR_FOCUS_BORDER : COLOR_BORDER, focused ? 2 : 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
    }

    private void registerUser() {
        String fname = fnameField.getText().trim();
        String lname = lnameField.getText().trim();
        String em = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String pass = new String(passField.getPassword()).trim();

        if (fname.isEmpty() || lname.isEmpty() || em.isEmpty() || phone.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String id = UUID.randomUUID().toString().substring(0, 8);
        int type = typeCombo.getSelectedIndex();

        try {
            String query = "INSERT INTO users (ID, FirstName, LastName, Email, PhoneNumber, Password, Type) VALUES ('"
                    + id + "', '" + fname + "', '" + lname + "', '" + em + "', '" + phone + "', '" + pass + "', " + type + ")";
            db.getStatement().executeUpdate(query);
            JOptionPane.showMessageDialog(this, "Account Registered! You can login now.");
            dispose();
            new LoginFrame().setVisible(true);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}