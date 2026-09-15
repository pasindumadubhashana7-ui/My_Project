package Controller;

import Project.database;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CarController {
    private final database db;

    public CarController(database db) {
        this.db = db;
        if (this.db == null || this.db.getStatement() == null) {
            throw new IllegalArgumentException("Database connection or statement is not initialized!");
        }
    }

    // Add new car
    public boolean addCar(String id, String brand, String model, double pricePerDay, String status) {
        try {
            String query = String.format("INSERT INTO `cars` (`ID`, `Brand`, `Model`, `PricePerDay`, `Status`) VALUES ('%s', '%s', '%s', %.2f, '%s')",
                    id, brand, model, pricePerDay, status);
            return db.getStatement().executeUpdate(query) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Remove car by ID
    public boolean removeCar(String id) {
        try {
            String query = "DELETE FROM `cars` WHERE `ID` = '" + id + "'";
            return db.getStatement().executeUpdate(query) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update car status (e.g., Available -> Rented)
    public boolean updateCarStatus(String id, String status) {
        try {
            String query = "UPDATE `cars` SET `Status` = '" + status + "' WHERE `ID` = '" + id + "'";
            return db.getStatement().executeUpdate(query) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean updateCar(String id, String brand, String model, double pricePerDay, String status) {
        String cleanId = id != null ? id.trim() : "";
        String query = String.format("UPDATE `cars` SET `Brand` = '%s', `Model` = '%s', `PricePerDay` = %.2f, `Status` = '%s' WHERE `ID` = '%s'",
                brand.trim(), model.trim(), pricePerDay, status.trim(), cleanId);
        
        System.out.println("DEBUG UPDATE QUERY: " + query);
        try {
            int rowsAffected = db.getStatement().executeUpdate(query);
            System.out.println("DEBUG Rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("DEBUG SQL Error: " + e.getMessage());
            return false;
        }
    }

    // Get all cars
    public ResultSet getAllCars() {
        try {
            return db.getStatement().executeQuery("SELECT * FROM `cars`");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Get cars by status
    public ResultSet getCarsByStatus(String status) {
        try {
            return db.getStatement().executeQuery("SELECT * FROM `cars` WHERE `Status` = '" + status + "'");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean deleteCar(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteCar'");
    }

}