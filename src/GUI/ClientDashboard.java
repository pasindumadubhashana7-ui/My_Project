package GUI;

import Controller.CarController;
import Controller.RentController;
import Project.database;
import java.awt.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class ClientDashboard extends JFrame {
    private final CarController carController;
    private final RentController rentController;
    private final database db;
    private final String currentUsername;

    private JPanel contentPanel;
    private CardLayout cardLayout;

    // Colors (Dark Theme)
    private final Color COLOR_PRIMARY_DARK = new Color(15, 23, 42);
    private final Color COLOR_MAIN_BG = new Color(15, 23, 42);
    private final Color COLOR_CARD_BG = new Color(30, 41, 59);
    private final Color COLOR_TEXT_PRIMARY = new Color(241, 245, 249);
    private final Color COLOR_BORDER = new Color(51, 65, 85);
    private final Color COLOR_SELECTION = new Color(71, 85, 105);

    // Selected car tracking for quick rent & live table reference
    private String selectedCarId = "";
    private double selectedPricePerDay = 0.0;
    private JTextField bookingIdTxt; // Class-level variable for auto-refreshing ID
    private DefaultTableModel carTableModel; // Class-level for live table refresh

    public ClientDashboard(String username) {
        this.currentUsername = username != null ? username : "Customer";
        db = new database();
        this.carController = new CarController(db);
        this.rentController = new RentController(db);

        setTitle("Vehicle Rental System - Client Dashboard (" + this.currentUsername + ")");
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_MAIN_BG);
        setLayout(new BorderLayout());

        // Left Sidebar Navigation
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(COLOR_PRIMARY_DARK);
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setBorder(new EmptyBorder(20, 15, 20, 15));

        JLabel brandLbl = new JLabel("🚗 Rent-a-Car");
        brandLbl.setForeground(Color.YELLOW);
        brandLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        brandLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(brandLbl);
        sidebar.add(Box.createRigidArea(new Dimension(0, 30)));

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(COLOR_MAIN_BG);

        JPanel browseAndRentPanel = createBrowseAndRentPanel();
        JPanel returnCarPanel = createReturnCarPanel();
        JPanel historyPanel = createHistoryPanel();
        JPanel profilePanel = createProfilePanel();

        contentPanel.add(browseAndRentPanel, "BROWSE_RENT");
        contentPanel.add(returnCarPanel, "RETURN");
        contentPanel.add(historyPanel, "HISTORY");
        contentPanel.add(profilePanel, "PROFILE");

        sidebar.add(createNavButton("🚗 Browse & Rent Cars", e -> {
            cardLayout.show(contentPanel, "BROWSE_RENT");
            refreshBookingId(); 
            refreshCarTable();
        }));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(createNavButton("🔄 Return Car", e -> cardLayout.show(contentPanel, "RETURN")));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(createNavButton("📜 History & Cancel", e -> cardLayout.show(contentPanel, "HISTORY")));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(createNavButton("👤 Update Profile", e -> cardLayout.show(contentPanel, "PROFILE")));
        
        sidebar.add(Box.createVerticalGlue());
        JButton logoutBtn = new JButton("🚪 Logout");
        styleButton(logoutBtn, new Color(239, 68, 68), Color.WHITE);
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
        sidebar.add(logoutBtn);

        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        cardLayout.show(contentPanel, "BROWSE_RENT");
        refreshBookingId();
        refreshCarTable();
    }

    private void refreshBookingId() {
        if (bookingIdTxt != null) {
            try {
                bookingIdTxt.setText(rentController.generateBookingId());
            } catch (Exception ex) {
                bookingIdTxt.setText("B001");
            }
        }
    }

    private void refreshCarTable() {
        if (carTableModel != null) {
            carTableModel.setRowCount(0);
            loadCarsToTable(carTableModel);
        }
    }

    private JButton createNavButton(String text, java.awt.event.ActionListener l) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(200, 42));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        styleButton(btn, COLOR_CARD_BG, COLOR_TEXT_PRIMARY);
        btn.addActionListener(l);
        return btn;
    }

    // 1. Unified Browse Table + Selected Car Rent & Date Calc Panel
    private JPanel createBrowseAndRentPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(COLOR_MAIN_BG);
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel title = new JLabel("Select a Car from Table & Rent");
        title.setForeground(COLOR_TEXT_PRIMARY);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        panel.add(title, BorderLayout.NORTH);

        carTableModel = new DefaultTableModel(new String[]{"ID", "Brand", "Model", "Price/Day (Rs.)", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 3) return Double.class;
                return Object.class;
            }
        };
        JTable table = new JTable(carTableModel);
        styleTable(table);
        loadCarsToTable(carTableModel);

        // Bottom Action & Price Calculation Card for Selected Car
        JPanel bottomActionCard = new JPanel(new GridBagLayout());
        bottomActionCard.setBackground(COLOR_CARD_BG);
        bottomActionCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        bookingIdTxt = new JTextField(12);
        bookingIdTxt.setEditable(false);
        refreshBookingId();

        JTextField selectedCarIdTxt = new JTextField(12);
        selectedCarIdTxt.setEditable(false);
        JTextField pricePerDayTxt = new JTextField("0.00", 12);
        pricePerDayTxt.setEditable(false);

        JTextField startDateTxt = new JTextField(LocalDate.now().toString(), 12);
        JTextField endDateTxt = new JTextField(LocalDate.now().plusDays(3).toString(), 12);
        
        JLabel calculatedPriceLbl = new JLabel("Total Price: Rs. 0.00 | Days: 0");
        calculatedPriceLbl.setForeground(new Color(74, 222, 128));
        calculatedPriceLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));

        JButton calcBtn = new JButton("🧮 Calculate Price");
        JButton submitRentBtn = new JButton("✅ Confirm Rent");
        styleButton(calcBtn, new Color(59, 130, 246), Color.WHITE);
        styleButton(submitRentBtn, new Color(16, 185, 129), Color.WHITE);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int viewRow = table.getSelectedRow();
                int modelRow = table.convertRowIndexToModel(viewRow);
                
                Object idObj = carTableModel.getValueAt(modelRow, 0);
                selectedCarId = idObj != null ? idObj.toString().trim() : "";
                
                Object rawPriceObj = carTableModel.getValueAt(modelRow, 3);
                if (rawPriceObj instanceof Number) {
                    selectedPricePerDay = ((Number) rawPriceObj).doubleValue();
                } else {
                    String clean = rawPriceObj != null ? rawPriceObj.toString().replaceAll("[^\\d.]", "") : "0";
                    try { selectedPricePerDay = Double.parseDouble(clean); } catch (Exception ex) { selectedPricePerDay = 0.0; }
                }

                selectedCarIdTxt.setText(selectedCarId);
                pricePerDayTxt.setText(String.format("%.2f", selectedPricePerDay));
            }
        });

        Runnable doCalculate = () -> {
            try {
                if (selectedCarId == null || selectedCarId.trim().isEmpty()) {
                    calculatedPriceLbl.setText("⚠️ Select a car from table first!");
                    JOptionPane.showMessageDialog(panel, "দয়া කරලා ටේබල් එකෙන් Car එකක් click කර select කරන්න!", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                double priceToUse = selectedPricePerDay;
                if (priceToUse <= 0) {
                    try {
                        priceToUse = Double.parseDouble(pricePerDayTxt.getText().trim().replaceAll("[^\\d.]", ""));
                    } catch (Exception ignored) {}
                }

                if (priceToUse <= 0) {
                    calculatedPriceLbl.setText("⚠️ Price/Day is 0!");
                    return;
                }

                LocalDate start = LocalDate.parse(startDateTxt.getText().trim());
                LocalDate end = LocalDate.parse(endDateTxt.getText().trim());
                long days = ChronoUnit.DAYS.between(start, end);
                if (days <= 0) {
                    calculatedPriceLbl.setText("⚠️ End Date must be after Start Date!");
                    return;
                }
                
                double total = days * priceToUse;
                calculatedPriceLbl.setText(String.format("Total Price: Rs. %.2f | Days: %d", total, days));
            } catch (Exception ex) {
                calculatedPriceLbl.setText("❌ Check dates (YYYY-MM-DD) or price!");
            }
        };

        calcBtn.addActionListener(e -> doCalculate.run());

        submitRentBtn.addActionListener(e -> {
            if (selectedCarId == null || selectedCarId.trim().isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Please select a car from the table first!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                String currentId = bookingIdTxt.getText().trim();
                boolean ok = rentController.rentCar(
                    currentId,
                    selectedCarId,
                    currentUsername,
                    startDateTxt.getText().trim(),
                    endDateTxt.getText().trim()
                );
                if (ok) {
                    JOptionPane.showMessageDialog(panel, "Car rented successfully with ID: " + currentId);
                    refreshCarTable();
                    refreshBookingId();
                    selectedCarIdTxt.setText("");
                    pricePerDayTxt.setText("0.00");
                    selectedCarId = "";
                    calculatedPriceLbl.setText("Total Price: Rs. 0.00 | Days: 0");
                } else {
                    JOptionPane.showMessageDialog(panel, "Failed to rent car.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        int r = 0;
        addFormRow(bottomActionCard, gbc, r++, "Booking ID:", bookingIdTxt, "Selected Car ID:", selectedCarIdTxt);
        addFormRow(bottomActionCard, gbc, r++, "Price/Day (Rs.):", pricePerDayTxt, "Start Date (YYYY-MM-DD):", startDateTxt);
        addFormRow(bottomActionCard, gbc, r++, "End Date (YYYY-MM-DD):", endDateTxt, "", new JLabel(""));
        
        gbc.gridx = 0; gbc.gridy = r; bottomActionCard.add(calcBtn, gbc);
        gbc.gridx = 1; bottomActionCard.add(calculatedPriceLbl, gbc);
        gbc.gridx = 3; bottomActionCard.add(submitRentBtn, gbc);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(table), bottomActionCard);
        splitPane.setDividerLocation(320);
        splitPane.setBackground(COLOR_MAIN_BG);
        splitPane.setOpaque(true);

        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    // 2. Return Car Panel (Fixed to update both rentals & cars status to Available)
    private JPanel createReturnCarPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        panel.setBackground(COLOR_MAIN_BG);
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));

        JTextField returnBookingIdTxt = new JTextField(12);
        JButton returnBtn = new JButton("Mark Completed / Returned");
        styleButton(returnBtn, new Color(245, 158, 11), Color.WHITE);

        returnBtn.addActionListener(e -> {
            try {
                String bId = returnBookingIdTxt.getText().trim();
                if (bId.isEmpty()) {
                    JOptionPane.showMessageDialog(panel, "Please enter a Booking ID!");
                    return;
                }
                
                // Fetch associated CarID first
                String carId = null;
                try (PreparedStatement pstSel = db.getConnection().prepareStatement("SELECT CarID FROM rentals WHERE BookingID = ?")) {
                    pstSel.setString(1, bId);
                    ResultSet rs = pstSel.executeQuery();
                    if (rs.next()) {
                        carId = rs.getString("CarID");
                    }
                }

                // Update rental status
                boolean ok = rentController.updateRentalStatus(bId, "Completed");
                if (ok) {
                    // Update car status back to Available
                    if (carId != null && !carId.isEmpty()) {
                        try (PreparedStatement pstCar = db.getConnection().prepareStatement("UPDATE cars SET Status = 'Available' WHERE ID = ?")) {
                            pstCar.setString(1, carId);
                            pstCar.executeUpdate();
                        }
                    }
                    refreshCarTable();
                    JOptionPane.showMessageDialog(panel, "Car returned successfully! Status updated to Available.");
                    returnBookingIdTxt.setText("");
                } else {
                    JOptionPane.showMessageDialog(panel, "Return failed!");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage());
            }
        });

        JLabel lbl = new JLabel("Booking ID to Return:");
        lbl.setForeground(COLOR_TEXT_PRIMARY);
        panel.add(lbl);
        panel.add(returnBookingIdTxt);
        panel.add(returnBtn);
        return panel;
    }

    // 3. Rental History & Cancel/Remove Panel
    // 3. Rental History & Cancel/Remove/Return Panel
private JPanel createHistoryPanel() {
    JPanel panel = new JPanel(new BorderLayout(15, 15));
    panel.setBackground(COLOR_MAIN_BG);
    panel.setBorder(new EmptyBorder(20, 25, 20, 25));

    JLabel title = new JLabel("My Rental History & Management");
    title.setForeground(COLOR_TEXT_PRIMARY);
    title.setFont(new Font("Segoe UI", Font.BOLD, 20));
    panel.add(title, BorderLayout.NORTH);

    DefaultTableModel model = new DefaultTableModel(new String[]{"Booking ID", "Car ID", "Start Date", "End Date", "Status"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    JTable table = new JTable(model);
    styleTable(table);
    loadHistoryToTable(model);

    JPanel bottomBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
    bottomBtnPanel.setBackground(COLOR_MAIN_BG);
    
    JButton refreshBtn = new JButton("🔄 Refresh");
    JButton returnSelectedBtn = new JButton("🔄 Return Car");
    JButton cancelBtn = new JButton("❌ Cancel Booking");
    JButton deleteBtn = new JButton("🗑️ Remove Record");
    
    styleButton(refreshBtn, new Color(59, 130, 246), Color.WHITE);
    styleButton(returnSelectedBtn, new Color(245, 158, 11), Color.WHITE);
    styleButton(cancelBtn, new Color(245, 158, 11), Color.WHITE);
    styleButton(deleteBtn, new Color(239, 68, 68), Color.WHITE);

    refreshBtn.addActionListener(e -> {
        model.setRowCount(0);
        loadHistoryToTable(model);
    });

    // Return Car from History logic
    returnSelectedBtn.addActionListener(e -> {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(panel, "Please select a booking from the table to return!");
            return;
        }
        String bId = table.getValueAt(selectedRow, 0).toString();
        String carId = table.getValueAt(selectedRow, 1).toString();
        String status = table.getValueAt(selectedRow, 4).toString();

        if ("Completed".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(panel, "This car is already returned/completed!");
            return;
        }
        if ("Cancelled".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(panel, "Cannot return a cancelled booking!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(panel, 
            "Mark Booking ID " + bId + " (Car ID: " + carId + ") as returned?", 
            "Confirm Return", JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean ok = rentController.updateRentalStatus(bId, "Completed");
                if (ok) {
                    if (carId != null && !carId.isEmpty()) {
                        try (PreparedStatement pstCar = db.getConnection().prepareStatement("UPDATE cars SET Status = 'Available' WHERE ID = ?")) {
                            pstCar.setString(1, carId);
                            pstCar.executeUpdate();
                        }
                    }
                    refreshCarTable();
                    JOptionPane.showMessageDialog(panel, "Car returned successfully! Status updated to Available.");
                    model.setRowCount(0);
                    loadHistoryToTable(model);
                } else {
                    JOptionPane.showMessageDialog(panel, "Return failed!");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage());
            }
        }
    });

    cancelBtn.addActionListener(e -> {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(panel, "Please select a booking from the table to cancel!");
            return;
        }
        String bId = table.getValueAt(selectedRow, 0).toString();
        String status = table.getValueAt(selectedRow, 4).toString();
        if ("Completed".equalsIgnoreCase(status) || "Cancelled".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(panel, "Cannot cancel a booking that is already " + status + "!");
            return;
        }
        try {
            boolean ok = rentController.updateRentalStatus(bId, "Cancelled");
            if (ok) {
                JOptionPane.showMessageDialog(panel, "Booking cancelled successfully!");
                model.setRowCount(0);
                loadHistoryToTable(model);
            } else {
                JOptionPane.showMessageDialog(panel, "Cancellation failed!");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage());
        }
    });

    deleteBtn.addActionListener(e -> {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(panel, "Please select a record from the table to remove!");
            return;
        }
        String bId = table.getValueAt(selectedRow, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(panel, "Are you sure you want to delete this history record from DB?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                PreparedStatement pst = db.getConnection().prepareStatement("DELETE FROM rentals WHERE BookingID = ?");
                pst.setString(1, bId);
                int rows = pst.executeUpdate();
                if (rows > 0) {
                    JOptionPane.showMessageDialog(panel, "History record removed successfully!");
                    model.setRowCount(0);
                    loadHistoryToTable(model);
                } else {
                    JOptionPane.showMessageDialog(panel, "Record not found or delete failed.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Error deleting: " + ex.getMessage());
            }
        }
    });

    bottomBtnPanel.add(refreshBtn);
    bottomBtnPanel.add(returnSelectedBtn);
    bottomBtnPanel.add(cancelBtn);
    bottomBtnPanel.add(deleteBtn);

    panel.add(new JScrollPane(table), BorderLayout.CENTER);
    panel.add(bottomBtnPanel, BorderLayout.SOUTH);
    return panel;
}

    // 4. Update Profile Panel
    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        panel.setBackground(COLOR_MAIN_BG);
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));

        JTextField userTxt = new JTextField(currentUsername, 15);
        userTxt.setEditable(false);
        JTextField passTxt = new JPasswordField(15);
        JButton updateBtn = new JButton("Update Profile");
        styleButton(updateBtn, new Color(59, 130, 246), Color.WHITE);

        updateBtn.addActionListener(e -> JOptionPane.showMessageDialog(panel, "Profile feature placeholder ready."));

        JLabel l1 = new JLabel("Username:"); l1.setForeground(COLOR_TEXT_PRIMARY);
        JLabel l2 = new JLabel("New Password:"); l2.setForeground(COLOR_TEXT_PRIMARY);
        panel.add(l1); panel.add(userTxt);
        panel.add(l2); panel.add(passTxt);
        panel.add(updateBtn);
        return panel;
    }

    private void addFormRow(JPanel p, GridBagConstraints gbc, int row, String lbl1, JComponent c1, String lbl2, JComponent c2) {
        gbc.gridy = row;
        gbc.gridx = 0; JLabel l1 = new JLabel(lbl1); l1.setForeground(COLOR_TEXT_PRIMARY); p.add(l1, gbc);
        gbc.gridx = 1; p.add(c1, gbc);
        gbc.gridx = 2; JLabel l2 = new JLabel(lbl2); l2.setForeground(COLOR_TEXT_PRIMARY); p.add(l2, gbc);
        gbc.gridx = 3; p.add(c2, gbc);
    }

    private void loadCarsToTable(DefaultTableModel model) {
        try {
            ResultSet rs = carController.getAllCars();
            if (rs != null) {
                while (rs.next()) {
                    double priceVal = rs.getDouble("PricePerDay");
                    model.addRow(new Object[]{
                        rs.getString("ID"),
                        rs.getString("Brand"),
                        rs.getString("Model"),
                        priceVal,
                        rs.getString("Status")
                    });
                }
            }
        } catch (SQLException ignored) {}
    }

    private void loadHistoryToTable(DefaultTableModel model) {
        try {
            PreparedStatement pst = db.getConnection().prepareStatement("SELECT BookingID, CarID, StartDate, EndDate, Status FROM rentals WHERE Username = ?");
            pst.setString(1, currentUsername);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("BookingID"),
                    rs.getString("CarID"),
                    rs.getString("StartDate"),
                    rs.getString("EndDate"),
                    rs.getString("Status")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setBackground(COLOR_CARD_BG);
        table.setForeground(COLOR_TEXT_PRIMARY);
        table.setGridColor(COLOR_BORDER);
        table.setRowHeight(40);
        table.getTableHeader().setBackground(new Color(22, 32, 54));
        table.getTableHeader().setForeground(COLOR_TEXT_PRIMARY);
        table.setSelectionBackground(COLOR_SELECTION);
        table.setSelectionForeground(Color.WHITE);
        
        table.setDefaultRenderer(Double.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSelected, boolean hasFocus, int row, int col) {
                if (val instanceof Number) {
                    val = "Rs. " + String.format("%.2f", ((Number) val).doubleValue());
                }
                return super.getTableCellRendererComponent(t, val, isSelected, hasFocus, row, col);
            }
        });
    }

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}