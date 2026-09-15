package Controller;

import Project.Operation;
import Project.database;
import Project.user;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class RentCar implements Operation {
    @Override
    public void operation(database database, Scanner s, user user) {
        System.out.println("\n--- Rent Car ---");
        System.out.print("Enter Car ID to rent: ");
        String carId = s.nextLine().trim();

        try {
            String checkQuery = "SELECT Status FROM `cars` WHERE `ID` = '" + carId + "'";
            ResultSet rs = database.getStatement().executeQuery(checkQuery);
            if (rs.next()) {
                String status = rs.getString("Status");
                if ("Available".equalsIgnoreCase(status)) {
                    String updateQuery = "UPDATE `cars` SET `Status` = 'Rented' WHERE `ID` = '" + carId + "'";
                    int rows = database.getStatement().executeUpdate(updateQuery);
                    if (rows > 0) {
                        System.out.println("Car rented successfully!");
                    }
                } else {
                    System.out.println("Car is already rented or not available (Status: " + status + ").");
                }
            } else {
                System.out.println("Car ID not found!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}