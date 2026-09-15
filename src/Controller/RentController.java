package Controller;

import Project.database;
import java.sql.Connection;
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

    // DB එකේ already තියෙනවාද බලලා, නැති ඊළඟ ID එක auto generate කරයි
    public String generateBookingId() throws SQLException {
        if (db == null || db.getConnection() == null) {
            return "B001";
        }
        
        int counter = 1;
        String candidateId = "B001";
        
        while (true) {
            candidateId = String.format("B%03d", counter);
            String checkSql = "SELECT COUNT(*) FROM rentals WHERE BookingID = ?";
            try (PreparedStatement ps = db.getConnection().prepareStatement(checkSql)) {
                ps.setString(1, candidateId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        break;
                    }
                }
            }
            counter++;
            if (counter > 9999) { // safety limit
                candidateId = "B" + System.currentTimeMillis();
                break;
            }
        }
        return candidateId;
    }

    public ResultSet getAllRentals() throws SQLException {
        if (db == null || db.getConnection() == null) {
            throw new SQLException("Database connection is null");
        }
        String sql = "SELECT * FROM rentals";
        Statement stmt = db.getConnection().createStatement();
        return stmt.executeQuery(sql);
    }

    // Fixed & Transactional status update (cancels/completes free the car)
    public boolean updateRentalStatus(String bookingId, String newStatus) throws SQLException {
        if (db == null || db.getConnection() == null) {
            return false;
        }

        Connection conn = db.getConnection();
        boolean originalAutoCommit = conn.getAutoCommit();

        try {
            conn.setAutoCommit(false); // Transaction start

            // 1. Booking එකට අදාළ CarID එක ලබා ගැනීම
            String carId = null;
            String getCarSql = "SELECT CarID FROM rentals WHERE BookingID = ?";
            try (PreparedStatement ps = conn.prepareStatement(getCarSql)) {
                ps.setString(1, bookingId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        carId = rs.getString("CarID");
                    }
                }
            }

            // 2. Rentals table එක update කිරීම
            String sql = "UPDATE rentals SET Status = ? WHERE BookingID = ?";
            int rowsAffected = 0;
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, newStatus);
                pstmt.setString(2, bookingId);
                rowsAffected = pstmt.executeUpdate();
            }

            // 3. Car status update කිරීම (Cancelled/Completed නම් Available, වෙනත් නම් Rented)
            if (rowsAffected > 0 && carId != null) {
                String carNewStatus;
                if ("Completed".equalsIgnoreCase(newStatus) || "Cancelled".equalsIgnoreCase(newStatus)) {
                    carNewStatus = "Available";
                } else {
                    carNewStatus = "Rented";
                }

                String updateCarSql = "UPDATE cars SET Status = ? WHERE ID = ?";
                try (PreparedStatement carStmt = conn.prepareStatement(updateCarSql)) {
                    carStmt.setString(1, carNewStatus);
                    carStmt.setString(2, carId);
                    carStmt.executeUpdate();
                }
            }

            conn.commit(); // Commit transaction
            return rowsAffected > 0;

        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback(); // Rollback on error
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(originalAutoCommit);
            }
        }
    }

    public boolean rentCar(String bookingId, String carId, String username, String startDateStr, String endDateStr) throws SQLException {
        if (db == null || db.getConnection() == null) {
            return false;
        }

        Connection conn = db.getConnection();
        boolean originalAutoCommit = conn.getAutoCommit();

        try {
            conn.setAutoCommit(false);

            // 1. Car එකේ PricePerDay එක ලබා ගැනීම
            double pricePerDay = 0.0;
            String priceSql = "SELECT PricePerDay FROM cars WHERE ID = ?";
            try (PreparedStatement ps = conn.prepareStatement(priceSql)) {
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

            // 3. Database එකට save කිරීම (Username සමග)
            String insertSql = "INSERT INTO rentals (BookingID, CarID, Username, StartDate, EndDate, TotalPrice, Status) VALUES (?, ?, ?, ?, ?, ?, 'Active')";
            int rows = 0;
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setString(1, bookingId);
                pstmt.setString(2, carId);
                pstmt.setString(3, username);
                pstmt.setString(4, startDateStr);
                pstmt.setString(5, endDateStr);
                pstmt.setDouble(6, totalPrice);
                rows = pstmt.executeUpdate();
            }
            
            if (rows > 0) {
                // 4. Car එකේ status එක Rented ලෙස update කිරීම
                String updateCarSql = "UPDATE cars SET Status = 'Rented' WHERE ID = ?";
                try (PreparedStatement carStmt = conn.prepareStatement(updateCarSql)) {
                    carStmt.setString(1, carId);
                    carStmt.executeUpdate();
                }
                conn.commit();
                return true;
            } else {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.setAutoCommit(originalAutoCommit);
        }
    }
}