package Controller;

import Project.Operation;
import Project.database;
import Project.user;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class ReturnCar implements Operation {
    @Override
    public void operation(database database, Scanner s, user user) {
        System.out.println("\n--- Return Car ---");
        System.out.print("Enter Car ID to return: ");
        String carId = s.nextLine().trim();

        try {
            String checkQuery = "SELECT Status FROM `cars` WHERE `ID` = '" + carId + "'";
            ResultSet rs = database.getStatement().executeQuery(checkQuery);
            if (rs.next()) {
                String status = rs.getString("Status");
                if ("Rented".equalsIgnoreCase(status)) {
                    String updateQuery = "UPDATE `cars` SET `Status` = 'Available' WHERE `ID` = '" + carId + "'";
                    int rows = database.getStatement().executeUpdate(updateQuery);
                    if (rows > 0) {
                        System.out.println("Car returned successfully!");
                    }
                } else {
                    System.out.println("Car is not currently rented (Status: " + status + ").");
                }
            } else {
                System.out.println("Car ID not found!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}