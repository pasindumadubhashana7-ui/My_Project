package GUI;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class RentCarDialog extends JDialog {
    private JTextField carIdField, customerNameField, startDateField, endDateField, pricePerDayField;
    private final Object db;
    private Runnable onRentSuccess;

    public RentCarDialog(Frame parent, Object db, String carId, double pricePerDay, Runnable onRentSuccess) {
        super(parent, "🚗 Book / Rent Car", true);
        this.db = db;
        this.onRentSuccess = onRentSuccess;

        setSize(420, 380);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(15, 23, 42));

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 12));
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        form.add(createLabel("Car ID:"));
        carIdField = createTextField(carId);
        carIdField.setEditable(false);
        form.add(carIdField);

        form.add(createLabel("Customer Name:"));
        customerNameField = createTextField("");
        form.add(customerNameField);

        form.add(createLabel("Start Date (YYYY-MM-DD):"));
        startDateField = createTextField("2026-09-20");
        form.add(startDateField);

        form.add(createLabel("End Date (YYYY-MM-DD):"));
        endDateField = createTextField("2026-09-25");
        form.add(endDateField);

        form.add(createLabel("Price/Day (Rs.):"));
        pricePerDayField = createTextField(String.valueOf(pricePerDay));
        pricePerDayField.setEditable(false);
        form.add(pricePerDayField);

        JButton bookBtn = new JButton("✔️ Confirm Booking");
        bookBtn.setBackground(new Color(16, 185, 129));
        bookBtn.setForeground(Color.WHITE);
        bookBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        bookBtn.setFocusPainted(false);
        bookBtn.addActionListener(e -> processBooking());

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(0, 0, 15, 0));
        bottom.add(bookBtn);

        add(form, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(new Color(241, 245, 249));
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return l;
    }

    private JTextField createTextField(String val) {
        JTextField tf = new JTextField(val);
        tf.setBackground(new Color(30, 41, 59));
        tf.setForeground(Color.WHITE);
        tf.setCaretColor(Color.WHITE);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(51, 65, 85)),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return tf;
    }

    private Connection getConnection() throws SQLException {
        try {
            Object connection = db.getClass().getMethod("getConnection").invoke(db);
            if (!(connection instanceof Connection)) {
                throw new SQLException("Database getConnection() did not return a JDBC connection.");
            }
            return (Connection) connection;
        } catch (NoSuchMethodException | IllegalAccessException ex) {
            throw new SQLException("Unable to access the database connection.", ex);
        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof SQLException) {
                throw (SQLException) cause;
            }
            throw new SQLException("Unable to obtain the database connection.", cause);
        }
    }

    private void processBooking() {
        String cid = carIdField.getText().trim();
        String cname = customerNameField.getText().trim();
        String sdate = startDateField.getText().trim();
        String edate = endDateField.getText().trim();

        if (cname.isEmpty() || sdate.isEmpty() || edate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double pricePerDay = Double.parseDouble(pricePerDayField.getText());
            double total = pricePerDay * 5; // Calculate the exact duration here if needed.

            Connection conn = getConnection();
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Database connection is null!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String bookingId = "B" + (System.currentTimeMillis() % 100000);
            String sql = "INSERT INTO rentals (BookingID, CarID, CustomerName, StartDate, EndDate, TotalPrice, Status) VALUES (?, ?, ?, ?, ?, ?, 'Active')";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, bookingId);
            pst.setString(2, cid);
            pst.setString(3, cname);
            pst.setString(4, sdate);
            pst.setString(5, edate);
            pst.setDouble(6, total);
            pst.executeUpdate();

            String updateCar = "UPDATE cars SET Status = 'Rented' WHERE ID = ?";
            PreparedStatement updatePst = conn.prepareStatement(updateCar);
            updatePst.setString(1, cid);
            updatePst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Booking Successful! Booking ID: " + bookingId);
            if (onRentSuccess != null) onRentSuccess.run();
            dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "DB Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}