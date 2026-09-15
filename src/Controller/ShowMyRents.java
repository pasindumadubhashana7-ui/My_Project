package Controller;

import Project.Operation;
import Project.database;
import Project.user;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class ShowMyRents implements Operation {
    @Override
    public void operation(database database, Scanner s, user user) {
        System.out.println("\n--- Show Rented Cars ---");
        try {
            String query = "SELECT * FROM `cars` WHERE `Status` = 'Rented'";
            ResultSet rs = database.getStatement().executeQuery(query);
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("ID: " + rs.getString("ID") + 
                    " | Brand: " + rs.getString("Brand") + 
                    " | Model: " + rs.getString("Model") + 
                    " | Price/Day: $" + rs.getDouble("PricePerDay") + 
                    " | Status: " + rs.getString("Status"));
            }
            if (!found) {
                System.out.println("No currently rented cars found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}