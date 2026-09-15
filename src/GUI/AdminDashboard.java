package GUI;

import Controller.CarController;
import Controller.RentController;
import Project.database;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class AdminDashboard extends JFrame {
    private final CarController carController;
    private final RentController rentController;
    private final database db;
    
    // Vehicles Table components
    private DefaultTableModel carModel;
    private JTable carTable;
    private TableRowSorter<DefaultTableModel> carSorter;
    private JTextField carSearchField;
    
    // Bookings Table components
    private DefaultTableModel bookingModel;
    private JTable bookingTable;
    private TableRowSorter<DefaultTableModel> bookingSorter;
    private JTextField bookingSearchField;
    
    // KPI Labels
    private JLabel totalCountLbl, availableCountLbl, rentedCountLbl, totalRevenueLbl;

    // Full Dark Mode Color Palette Constants
    private final Color COLOR_PRIMARY_DARK = new Color(15, 23, 42);   // Slate 900
    private final Color COLOR_MAIN_BG = new Color(15, 23, 42);        // Main background dark
    private final Color COLOR_CARD_BG = new Color(30, 41, 59);        // Slate 800 (Cards/Tables)
    private final Color COLOR_TEXT_PRIMARY = new Color(241, 245, 249);// Light text
    private final Color COLOR_TEXT_MUTED = new Color(148, 163, 184);   // Muted text
    private final Color COLOR_BORDER = new Color(51, 65, 85);         // Slate 700 border
    private final Color COLOR_SELECTION = new Color(71, 85, 105);     // Selection color
    private final Color COLOR_HEADER_DARK = new Color(22, 32, 54);    // Custom dark header bg

    public AdminDashboard() {
        db = new database();
        if (db == null || db.getConnection() == null) {
            JOptionPane.showMessageDialog(null, "Database connection failed!", "Error", JOptionPane.ERROR_MESSAGE);
        }
        this.carController = new CarController(db);
        this.rentController = new RentController(db);
        
        setTitle("Vehicle Rental System - Admin Dashboard");
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_MAIN_BG);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_PRIMARY_DARK);
        header.setBorder(new EmptyBorder(15, 25, 15, 25));
        
        JLabel title = new JLabel("⚡ Fleet & Booking Admin Dashboard");
        title.setForeground(Color.yellow);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.add(title, BorderLayout.WEST);

        JButton logoutBtn = new JButton("🚪 Logout");
        styleButton(logoutBtn, new Color(239, 68, 68), Color.WHITE);
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
        header.add(logoutBtn, BorderLayout.EAST);

        // Main Content Area (Dark)
        JPanel mainContent = new JPanel(new BorderLayout(15, 15));
        mainContent.setBorder(new EmptyBorder(20, 25, 20, 25));
        mainContent.setBackground(COLOR_MAIN_BG);

        // Top KPI Summary Cards (Dark cards)
        JPanel kpiPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        kpiPanel.setOpaque(false);
        totalCountLbl = createKpiCard(kpiPanel, "Total Vehicles", "0", new Color(59, 130, 246));
        availableCountLbl = createKpiCard(kpiPanel, "Available", "0", new Color(16, 185, 129));
        rentedCountLbl = createKpiCard(kpiPanel, "Rented / Main.", "0", new Color(245, 158, 11));
        totalRevenueLbl = createKpiCard(kpiPanel, "Total Revenue", "Rs. 0.00", new Color(139, 92, 246));

        // Tabbed Pane for Vehicles and Bookings
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBackground(COLOR_CARD_BG);
        tabbedPane.setForeground(COLOR_TEXT_PRIMARY);
        tabbedPane.addTab("🚗 Vehicles Management", createVehiclesTab());
        tabbedPane.addTab("📅 Bookings / Rentals", createBookingsTab());

        mainContent.add(kpiPanel, BorderLayout.NORTH);
        mainContent.add(tabbedPane, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(mainContent, BorderLayout.CENTER);

        loadAllData();
    }

    public void refreshDashboard() {
        loadAllData();
    }
    // Admin Rentals Panel with Approve / Auto-Reject Logic
private JPanel createAdminRentalsPanel() {
    JPanel panel = new JPanel(new BorderLayout(15, 15));
    panel.setBackground(COLOR_MAIN_BG);
    panel.setBorder(new EmptyBorder(20, 25, 20, 25));

    JLabel title = new JLabel("Client Rent Bookings Approval Management");
    title.setForeground(COLOR_TEXT_PRIMARY);
    title.setFont(new Font("Segoe UI", Font.BOLD, 20));
    panel.add(title, BorderLayout.NORTH);

    DefaultTableModel model = new DefaultTableModel(new String[]{"Booking ID", "Car ID", "Username", "Start Date", "End Date", "Status"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    JTable table = new JTable(model);
    styleBookingTable(table);
    loadAllRentalsToTable(model);

    JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
    btnPanel.setBackground(COLOR_MAIN_BG);

    JButton approveBtn = new JButton("✅ Approve");
    JButton rejectBtn = new JButton("❌ Reject");
    JButton refreshBtn = new JButton("🔄 Refresh");

    styleButton(approveBtn, new Color(16, 185, 129), Color.WHITE);
    styleButton(rejectBtn, new Color(239, 68, 68), Color.WHITE);
    styleButton(refreshBtn, new Color(59, 130, 246), Color.WHITE);

    refreshBtn.addActionListener(e -> {
        model.setRowCount(0);
        loadAllRentalsToTable(model);
    });

    approveBtn.addActionListener(e -> {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(panel, "Please select a booking from the table!");
            return;
        }
        String bookingId = table.getValueAt(selectedRow, 0).toString();
        String carId = table.getValueAt(selectedRow, 1).toString();
        String currentStatus = table.getValueAt(selectedRow, 5).toString();

        if ("Approved".equalsIgnoreCase(currentStatus) || "Rejected".equalsIgnoreCase(currentStatus) || "Completed".equalsIgnoreCase(currentStatus)) {
            JOptionPane.showMessageDialog(panel, "Booking is already processed (" + currentStatus + ")!");
            return;
        }

        try (java.sql.Connection conn = db.getConnection()) {
            // Check current car status from database
            String checkCarSql = "SELECT Status FROM cars WHERE ID = ?";
            String carStatus = "";
            try (java.sql.PreparedStatement pstCheck = conn.prepareStatement(checkCarSql)) {
                pstCheck.setString(1, carId);
                try (java.sql.ResultSet rs = pstCheck.executeQuery()) {
                    if (rs.next()) {
                        carStatus = rs.getString("Status");
                    }
                }
            }

            // AUTO-REJECT check if car is already rented or unavailable
            if ("Rented".equalsIgnoreCase(carStatus) || "Unavailable".equalsIgnoreCase(carStatus)) {
                try (java.sql.PreparedStatement pstReject = conn.prepareStatement("UPDATE rentals SET Status = 'Rejected' WHERE BookingID = ?")) {
                    pstReject.setString(1, bookingId);
                    pstReject.executeUpdate();
                }
                JOptionPane.showMessageDialog(panel, 
                    "⚠️ Car ID " + carId + " is already '" + carStatus + "'!\nBooking " + bookingId + " has been AUTO-REJECTED.", 
                    "Auto-Rejected", JOptionPane.WARNING_MESSAGE);
            } else {
                // Normal Approve flow (Transaction-safe)
                conn.setAutoCommit(false);
                try {
                    try (java.sql.PreparedStatement pstApprove = conn.prepareStatement("UPDATE rentals SET Status = 'Approved' WHERE BookingID = ?")) {
                        pstApprove.setString(1, bookingId);
                        pstApprove.executeUpdate();
                    }
                    try (java.sql.PreparedStatement pstUpdateCar = conn.prepareStatement("UPDATE cars SET Status = 'Rented' WHERE ID = ?")) {
                        pstUpdateCar.setString(1, carId);
                        pstUpdateCar.executeUpdate();
                    }
                    conn.commit();
                    JOptionPane.showMessageDialog(panel, "✅ Booking approved and Car ID " + carId + " marked as 'Rented'!");
                } catch (Exception ex) {
                    conn.rollback();
                    throw ex;
                } finally {
                    conn.setAutoCommit(true);
                }
            }
            model.setRowCount(0);
            loadAllRentalsToTable(model);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    });

    rejectBtn.addActionListener(e -> {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(panel, "Please select a booking from the table!");
            return;
        }
        String bookingId = table.getValueAt(selectedRow, 0).toString();
        try (java.sql.PreparedStatement pst = db.getConnection().prepareStatement("UPDATE rentals SET Status = 'Rejected' WHERE BookingID = ?")) {
            pst.setString(1, bookingId);
            int rows = pst.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(panel, "Booking manually rejected.");
                model.setRowCount(0);
                loadAllRentalsToTable(model);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage());
        }
    });

    btnPanel.add(refreshBtn);
    btnPanel.add(approveBtn);
    btnPanel.add(rejectBtn);

    panel.add(new JScrollPane(table), BorderLayout.CENTER);
    panel.add(btnPanel, BorderLayout.SOUTH);
    return panel;
}

private void loadAllRentalsToTable(DefaultTableModel model) {
    try (java.sql.PreparedStatement pst = db.getConnection().prepareStatement("SELECT BookingID, CarID, Username, StartDate, EndDate, Status FROM rentals")) {
        try (java.sql.ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("BookingID"),
                    rs.getString("CarID"),
                    rs.getString("Username"),
                    rs.getString("StartDate"),
                    rs.getString("EndDate"),
                    rs.getString("Status")
                });
            }
        }
    } catch (java.sql.SQLException ex) {
        ex.printStackTrace();
    }
}

    private JPanel createVehiclesTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(COLOR_CARD_BG);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel topActionPanel = new JPanel(new BorderLayout(15, 0));
        topActionPanel.setOpaque(false);
        
        JPanel searchBoxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchBoxPanel.setOpaque(false);
        JLabel searchLbl = new JLabel("🔍 Search Vehicle:");
        searchLbl.setForeground(COLOR_TEXT_PRIMARY);
        searchLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        carSearchField = new JTextField(18);
        carSearchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        carSearchField.setBackground(COLOR_PRIMARY_DARK);
        carSearchField.setForeground(COLOR_TEXT_PRIMARY);
        carSearchField.setCaretColor(Color.WHITE);
        carSearchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        carSearchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterCars(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterCars(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterCars(); }
        });
        searchBoxPanel.add(searchLbl);
        searchBoxPanel.add(carSearchField);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        toolbar.setOpaque(false);
        
        JButton addCarBtn = new JButton("➕ Add Car");
        JButton editCarBtn = new JButton("✏️ Edit Selected");
        JButton removeCarBtn = new JButton("🗑️ Remove");
        JButton refreshBtn = new JButton("🔄 Refresh");

        styleButton(addCarBtn, new Color(16, 185, 129), Color.WHITE);
        styleButton(editCarBtn, new Color(245, 158, 11), Color.WHITE);
        styleButton(removeCarBtn, new Color(239, 68, 68), Color.WHITE);
        styleButton(refreshBtn, new Color(100, 116, 139), Color.WHITE);

        toolbar.add(addCarBtn);
        toolbar.add(editCarBtn);
        toolbar.add(removeCarBtn);
        toolbar.add(refreshBtn);

        topActionPanel.add(searchBoxPanel, BorderLayout.WEST);
        topActionPanel.add(toolbar, BorderLayout.EAST);

        carModel = new DefaultTableModel(new String[]{"ID", "Brand", "Model", "Price/Day (Rs.)", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
            @Override
            public Class<?> getColumnClass(int col) { return col == 3 ? Double.class : Object.class; }
        };
        carTable = new JTable(carModel);
        styleCarTable(carTable);
        carSorter = new TableRowSorter<>(carModel);
        carTable.setRowSorter(carSorter);

        JScrollPane scrollPane = new JScrollPane(carTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        scrollPane.getViewport().setBackground(COLOR_CARD_BG);

        panel.add(topActionPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        addCarBtn.addActionListener(e -> new AddCarDialog(this, carController, this::loadAllData).setVisible(true));
        editCarBtn.addActionListener(e -> editSelectedCar());
        removeCarBtn.addActionListener(e -> new RemoveCarDialog(this, carController, this::loadAllData).setVisible(true));
        refreshBtn.addActionListener(e -> loadAllData());

        return panel;
    }

    private JPanel createBookingsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(COLOR_CARD_BG);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel topContainer = new JPanel(new BorderLayout(10, 10));
        topContainer.setOpaque(false);

        JPanel topActionPanel = new JPanel(new BorderLayout(15, 0));
        topActionPanel.setOpaque(false);
        
        JPanel searchBoxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchBoxPanel.setOpaque(false);
        JLabel searchLbl = new JLabel("🔍 Search Booking:");
        searchLbl.setForeground(COLOR_TEXT_PRIMARY);
        searchLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        bookingSearchField = new JTextField(18);
        bookingSearchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        bookingSearchField.setBackground(COLOR_PRIMARY_DARK);
        bookingSearchField.setForeground(COLOR_TEXT_PRIMARY);
        bookingSearchField.setCaretColor(Color.WHITE);
        bookingSearchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        bookingSearchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterBookings(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterBookings(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterBookings(); }
        });
        searchBoxPanel.add(searchLbl);
        searchBoxPanel.add(bookingSearchField);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        toolbar.setOpaque(false);
        
        JButton completeBtn = new JButton("✔️ Mark Completed");
        JButton cancelBtn = new JButton("❌ Cancel Booking");
        JButton refreshBookingsBtn = new JButton("🔄 Refresh Bookings");

        styleButton(completeBtn, new Color(16, 185, 129), Color.WHITE);
        styleButton(cancelBtn, new Color(239, 68, 68), Color.WHITE);
        styleButton(refreshBookingsBtn, new Color(59, 130, 246), Color.WHITE);

        toolbar.add(completeBtn);
        toolbar.add(cancelBtn);
        toolbar.add(refreshBookingsBtn);

        topActionPanel.add(searchBoxPanel, BorderLayout.WEST);
        topActionPanel.add(toolbar, BorderLayout.EAST);
        topContainer.add(topActionPanel, BorderLayout.NORTH);

        bookingModel = new DefaultTableModel(new String[]{"Booking ID", "Car ID", "Customer", "Start Date", "End Date", "Total Price (Rs.)", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        bookingTable = new JTable(bookingModel);
        styleBookingTable(bookingTable);
        bookingSorter = new TableRowSorter<>(bookingModel);
        bookingTable.setRowSorter(bookingSorter);

        JScrollPane scrollPane = new JScrollPane(bookingTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        scrollPane.getViewport().setBackground(COLOR_CARD_BG);

        panel.add(topContainer, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        completeBtn.addActionListener(e -> {
            try {
                updateSelectedBookingStatus("Completed");
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        });
        cancelBtn.addActionListener(e -> {
            try {
                updateSelectedBookingStatus("Cancelled");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        refreshBookingsBtn.addActionListener(e -> loadBookingsData());

        return panel;
    }

    private void styleCarTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setBackground(COLOR_CARD_BG);
        table.setForeground(COLOR_TEXT_PRIMARY);
        table.setGridColor(COLOR_BORDER);
        table.setRowHeight(46);
        table.setShowVerticalLines(false);
        
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSel, boolean hasFocus, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(tbl, val, isSel, hasFocus, r, c);
                lbl.setBackground(COLOR_HEADER_DARK);
                lbl.setForeground(COLOR_TEXT_PRIMARY);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lbl.setHorizontalAlignment(JLabel.CENTER);
                return lbl;
            }
        };
        table.getTableHeader().setDefaultRenderer(headerRenderer);
        table.getTableHeader().setOpaque(true);
        table.getTableHeader().setPreferredSize(new Dimension(0, 42));
        
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(COLOR_SELECTION);
        table.setSelectionForeground(Color.WHITE);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setForeground(COLOR_TEXT_PRIMARY);
        centerRenderer.setBackground(COLOR_CARD_BG);

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);
        leftRenderer.setForeground(COLOR_TEXT_PRIMARY);
        leftRenderer.setBackground(COLOR_CARD_BG);

        if (table.getColumnCount() >= 5) {
            table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); // ID
            table.getColumnModel().getColumn(1).setCellRenderer(leftRenderer);  // Brand
            table.getColumnModel().getColumn(2).setCellRenderer(leftRenderer);  // Model
            
            table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSel, boolean hasFocus, int r, int c) {
                    if (val instanceof Number) {
                        val = "Rs. " + String.format("%.2f", ((Number) val).doubleValue());
                    }
                    setHorizontalAlignment(JLabel.CENTER);
                    setForeground(COLOR_TEXT_PRIMARY);
                    setBackground(COLOR_CARD_BG);
                    return super.getTableCellRendererComponent(tbl, val, isSel, hasFocus, r, c);
                }
            });
            
            table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                    if (c instanceof JLabel) {
                        JLabel lbl = (JLabel) c;
                        lbl.setHorizontalAlignment(JLabel.CENTER);
                        String val = value != null ? value.toString() : "";
                        if (!isSelected) {
                            if ("Available".equalsIgnoreCase(val)) {
                                lbl.setForeground(new Color(74, 222, 128));
                                lbl.setBackground(new Color(20, 83, 45, 140));
                            } else {
                                lbl.setForeground(new Color(248, 113, 113));
                                lbl.setBackground(new Color(127, 29, 29, 140));
                            }
                        } else {
                            lbl.setForeground(Color.WHITE);
                            lbl.setBackground(COLOR_SELECTION);
                        }
                        lbl.setOpaque(true);
                        lbl.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
                    }
                    return c;
                }
            });
        }
    }

    private void styleBookingTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setBackground(COLOR_CARD_BG);
        table.setForeground(COLOR_TEXT_PRIMARY);
        table.setGridColor(COLOR_BORDER);
        table.setRowHeight(44);
        table.setShowVerticalLines(false);
        
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSel, boolean hasFocus, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(tbl, val, isSel, hasFocus, r, c);
                lbl.setBackground(COLOR_HEADER_DARK);
                lbl.setForeground(COLOR_TEXT_PRIMARY);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lbl.setHorizontalAlignment(JLabel.CENTER);
                return lbl;
            }
        };
        table.getTableHeader().setDefaultRenderer(headerRenderer);
        table.getTableHeader().setOpaque(true);
        table.getTableHeader().setPreferredSize(new Dimension(0, 42));
        
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(COLOR_SELECTION);
        table.setSelectionForeground(Color.WHITE);

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);
        leftRenderer.setForeground(COLOR_TEXT_PRIMARY);
        leftRenderer.setBackground(COLOR_CARD_BG);
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setForeground(COLOR_TEXT_PRIMARY);
        centerRenderer.setBackground(COLOR_CARD_BG);
        
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        rightRenderer.setForeground(COLOR_TEXT_PRIMARY);
        rightRenderer.setBackground(COLOR_CARD_BG);

        if (table.getColumnCount() >= 7) {
            table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
            table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
            table.getColumnModel().getColumn(2).setCellRenderer(leftRenderer);
            table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
            table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
            table.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
            
            table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                    if (c instanceof JLabel) {
                        JLabel lbl = (JLabel) c;
                        lbl.setHorizontalAlignment(JLabel.CENTER);
                        String val = value != null ? value.toString() : "";
                        if (!isSelected) {
                            if ("Completed".equalsIgnoreCase(val)) {
                                lbl.setForeground(new Color(74, 222, 128));
                                lbl.setBackground(new Color(20, 83, 45, 140));
                            } else if ("Cancelled".equalsIgnoreCase(val)) {
                                lbl.setForeground(new Color(248, 113, 113));
                                lbl.setBackground(new Color(127, 29, 29, 140));
                            } else {
                                lbl.setForeground(new Color(251, 191, 36));
                                lbl.setBackground(new Color(120, 53, 15, 140));
                            }
                        } else {
                            lbl.setForeground(Color.WHITE);
                            lbl.setBackground(COLOR_SELECTION);
                        }
                        lbl.setOpaque(true);
                        lbl.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
                    }
                    return c;
                }
            });
        }
    }

    private JLabel createKpiCard(JPanel parent, String title, String initVal, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(5, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(COLOR_CARD_BG);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2d.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        JPanel accentBar = new JPanel();
        accentBar.setBackground(accentColor);
        accentBar.setPreferredSize(new Dimension(5, 0));
        card.add(accentBar, BorderLayout.WEST);

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        textPanel.setOpaque(false);
        textPanel.setBorder(new EmptyBorder(0, 10, 0, 0));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLbl.setForeground(COLOR_TEXT_MUTED);

        JLabel valLbl = new JLabel(initVal);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valLbl.setForeground(COLOR_TEXT_PRIMARY);

        textPanel.add(titleLbl);
        textPanel.add(valLbl);
        card.add(textPanel, BorderLayout.CENTER);

        parent.add(card);
        return valLbl;
    }

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bg.darker(), 1),
            BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void filterCars() {
        String text = carSearchField.getText().trim();
        carSorter.setRowFilter(text.isEmpty() ? null : RowFilter.regexFilter("(?i)" + text));
    }

    private void filterBookings() {
        String text = bookingSearchField.getText().trim();
        bookingSorter.setRowFilter(text.isEmpty() ? null : RowFilter.regexFilter("(?i)" + text));
    }

    private void loadAllData() {
        loadAdminCars();
        loadBookingsData();
    }

    private void loadAdminCars() {
        carModel.setRowCount(0);
        int total = 0, available = 0, rented = 0;
        try {
            ResultSet rs = carController.getAllCars();
            if (rs != null) {
                while (rs.next()) {
                    String status = rs.getString("Status");
                    total++;
                    if ("Available".equalsIgnoreCase(status)) available++;
                    else rented++;

                    carModel.addRow(new Object[]{
                        rs.getString("ID"), 
                        rs.getString("Brand"), 
                        rs.getString("Model"),
                        rs.getDouble("PricePerDay"), 
                        status
                    });
                }
            }
            totalCountLbl.setText(String.valueOf(total));
            availableCountLbl.setText(String.valueOf(available));
            rentedCountLbl.setText(String.valueOf(rented));
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
    }

    private void loadBookingsData() {
        if (bookingModel == null) return;
        bookingModel.setRowCount(0);
        double totalRev = 0.0;
        try {
            if (rentController != null) {
                ResultSet rs = rentController.getAllRentals();
                if (rs != null) {
                    while (rs.next()) {
                        String bid = rs.getString("BookingID");
                        String cid = rs.getString("CarID");
                        String cname = rs.getString("Username");
                        String sdate = rs.getString("StartDate");
                        String edate = rs.getString("EndDate");
                        double price = rs.getDouble("TotalPrice");
                        String status = rs.getString("Status");
                        
                        if (!"Cancelled".equalsIgnoreCase(status)) {
                            totalRev += price;
                        }
                        
                        bookingModel.addRow(new Object[]{
                            bid != null ? bid : "N/A",
                            cid != null ? cid : "N/A",
                            cname != null ? cname : "N/A",
                            sdate != null ? sdate : "-",
                            edate != null ? edate : "-",
                            "Rs. " + String.format("%.2f", price),
                            status != null ? status : "Active"
                        });
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        totalRevenueLbl.setText("Rs. " + String.format("%.2f", totalRev));
    }

    private void editSelectedCar() {
        int selectedRow = carTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a car to edit!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = carTable.convertRowIndexToModel(selectedRow);

        String id = carModel.getValueAt(modelRow, 0).toString();
        String brand = carModel.getValueAt(modelRow, 1).toString();
        String model = carModel.getValueAt(modelRow, 2).toString();
        
        Object valObj = carModel.getValueAt(modelRow, 3);
        double price = 0.0;
        if (valObj instanceof Number) {
            price = ((Number) valObj).doubleValue();
        } else if (valObj != null) {
            try {
                price = Double.parseDouble(valObj.toString().replaceAll("[^0-9.]", ""));
            } catch (Exception ignored) {}
        }

        String status = carModel.getValueAt(modelRow, 4).toString();

        new EditCarDialog(this, carController, id, brand, model, price, status, this::loadAllData).setVisible(true);
    }

    private void updateSelectedBookingStatus(String newStatus) throws SQLException {
        int selectedRow = bookingTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking first!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = bookingTable.convertRowIndexToModel(selectedRow);
        String bookingId = bookingModel.getValueAt(modelRow, 0).toString();

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to mark booking " + bookingId + " as " + newStatus + "?", 
            "Confirm Update", JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            boolean updated = rentController.updateRentalStatus(bookingId, newStatus);
            if (updated) {
                JOptionPane.showMessageDialog(this, "Booking status updated to " + newStatus);
                loadBookingsData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update status.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public RentController getRentController() {
        return rentController;
    }
}

// ==========================================
// Helper Dialog Classes required by AdminDashboard
// ==========================================
class AddCarDialog extends JDialog {
    public AddCarDialog(JFrame parent, CarController carController, Runnable onSuccess) {
        super(parent, "Add New Car", true);
        setSize(400, 300);
        setLocationRelativeTo(parent);
        setLayout(new GridLayout(6, 2, 10, 10));

        JTextField idField = new JTextField();
        JTextField brandField = new JTextField();
        JTextField modelField = new JTextField();
        JTextField priceField = new JTextField();
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Available", "Rented"});

        add(new JLabel("Car ID:")); add(idField);
        add(new JLabel("Brand:")); add(brandField);
        add(new JLabel("Model:")); add(modelField);
        add(new JLabel("Price/Day:")); add(priceField);
        add(new JLabel("Status:")); add(statusCombo);

        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        add(saveBtn); add(cancelBtn);

        saveBtn.addActionListener(e -> {
            try {
                String id = idField.getText().trim();
                String brand = brandField.getText().trim();
                String model = modelField.getText().trim();
                double price = Double.parseDouble(priceField.getText().trim());
                String status = (String) statusCombo.getSelectedItem();

                boolean success = carController.addCar(id, brand, model, price, status);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Car added successfully!");
                    onSuccess.run();
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add car.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        cancelBtn.addActionListener(e -> dispose());
    }
}

class EditCarDialog extends JDialog {
    public EditCarDialog(JFrame parent, CarController carController, String id, String brand, String model, double price, String status, Runnable onSuccess) {
        super(parent, "Edit Car", true);
        setSize(400, 300);
        setLocationRelativeTo(parent);
        setLayout(new GridLayout(6, 2, 10, 10));

        JTextField idField = new JTextField(id); idField.setEditable(false);
        JTextField brandField = new JTextField(brand);
        JTextField modelField = new JTextField(model);
        JTextField priceField = new JTextField(String.valueOf(price));
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Available", "Rented"});
        statusCombo.setSelectedItem(status);

        add(new JLabel("Car ID:")); add(idField);
        add(new JLabel("Brand:")); add(brandField);
        add(new JLabel("Model:")); add(modelField);
        add(new JLabel("Price/Day:")); add(priceField);
        add(new JLabel("Status:")); add(statusCombo);

        JButton updateBtn = new JButton("Update");
        JButton cancelBtn = new JButton("Cancel");
        add(updateBtn); add(cancelBtn);

        updateBtn.addActionListener(e -> {
            try {
                String newBrand = brandField.getText().trim();
                String newModel = modelField.getText().trim();
                double newPrice = Double.parseDouble(priceField.getText().trim());
                String newStatus = (String) statusCombo.getSelectedItem();

                boolean success = carController.updateCar(id, newBrand, newModel, newPrice, newStatus);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Car updated successfully!");
                    onSuccess.run();
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update car.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        cancelBtn.addActionListener(e -> dispose());
    }
}

class RemoveCarDialog extends JDialog {
    public RemoveCarDialog(JFrame parent, CarController carController, Runnable onSuccess) {
        super(parent, "Remove Car", true);
        setSize(350, 150);
        setLocationRelativeTo(parent);
        setLayout(new FlowLayout());

        add(new JLabel("Enter Car ID to Remove:"));
        JTextField idField = new JTextField(15);
        add(idField);

        JButton removeBtn = new JButton("Remove");
        JButton cancelBtn = new JButton("Cancel");
        add(removeBtn); add(cancelBtn);

        removeBtn.addActionListener(e -> {
            String id = idField.getText().trim();
            if (id.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter Car ID.");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, "Remove car " + id + "?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = carController.deleteCar(id);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Car removed!");
                    onSuccess.run();
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to remove car.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        cancelBtn.addActionListener(e -> dispose());
    }
}