package GUI; // (හෝ GUI)

import Project.database;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class RentController {
    private final database db;

    public RentController(database db) {
        this.db = db;
    }

    RentController() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ResultSet getAllRentals() throws SQLException {
        if (db == null || db.getConnection() == null) {
            throw new SQLException("Database connection is null");
        }
        String sql = "SELECT * FROM rentals";
        Statement stmt = db.getConnection().createStatement();
        return stmt.executeQuery(sql);
    }

    public boolean updateRentalStatus(String bookingId, String newStatus) throws SQLException {
        if (db == null || db.getConnection() == null) {
            return false;
        }
        String sql = "UPDATE rentals SET Status = ? WHERE BookingID = ?";
        try (PreparedStatement pstmt = db.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setString(2, bookingId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public boolean rentCar(String bookingId, String carId, String customerName, String startDateStr, String endDateStr) throws SQLException {
        if (db == null || db.getConnection() == null) {
            return false;
        }
        
        // 1. Car එකේ PricePerDay එක ලබා ගැනීම
        double pricePerDay = 0.0;
        String priceSql = "SELECT PricePerDay FROM cars WHERE ID = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(priceSql)) {
            ps.setString(1, carId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    pricePerDay = rs.getDouble("PricePerDay");
                }
            }
        }

        // 2. දින ගණන සහ Total Price ගණනය කිරීම
        LocalDate start = LocalDate.parse(startDateStr);
        LocalDate end = LocalDate.parse(endDateStr);
        long days = ChronoUnit.DAYS.between(start, end);
        if (days <= 0) days = 1;
        double totalPrice = days * pricePerDay;

        // 3. Database එකට save කිරීම
        String insertSql = "INSERT INTO rentals (BookingID, CarID, CustomerName, StartDate, EndDate, TotalPrice, Status) VALUES (?, ?, ?, ?, ?, ?, 'Active')";
        try (PreparedStatement pstmt = db.getConnection().prepareStatement(insertSql)) {
            pstmt.setString(1, bookingId);
            pstmt.setString(2, carId);
            pstmt.setString(3, customerName);
            pstmt.setString(4, startDateStr);
            pstmt.setString(5, endDateStr);
            pstmt.setDouble(6, totalPrice);
            return pstmt.executeUpdate() > 0;
        }
    }
}