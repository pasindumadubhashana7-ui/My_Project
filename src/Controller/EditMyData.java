package Controller;

import Project.Operation;
import Project.database;
import Project.user;
import java.sql.SQLException;
import java.util.Scanner;

public class EditMyData implements Operation {
    @Override
    public void operation(database database, Scanner s, user user) {
        System.out.println("\n--- Edit My Data ---");
        System.out.print("Enter New First Name (current: " + user.getfname() + "): ");
        String fname = s.nextLine().trim();
        System.out.print("Enter New Last Name (current: " + user.getlname() + "): ");
        String lname = s.nextLine().trim();
        System.out.print("Enter New Phone Number (current: " + user.getphone() + "): ");
        String phone = s.nextLine().trim();

        try {
            String query = "UPDATE `users` SET `FirstName` = '" + fname + "', `LastName` = '" + lname + "', `PhoneNumber` = '" + phone + "' WHERE `ID` = '" + user.getId() + "'";
            int rows = database.getStatement().executeUpdate(query);
            if (rows > 0) {
                user.setfname(fname);
                user.setlname(lname);
                user.setphone(phone);
                System.out.println("Profile updated successfully!");
            } else {
                System.out.println("Failed to update profile.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}