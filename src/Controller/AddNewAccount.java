package Controller;

import Project.Operation;
import Project.admin;
import Project.client;
import Project.database;
import Project.user;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class AddNewAccount implements Operation {
    private final int accType;

    public AddNewAccount(int accType) {
        this.accType = accType;
    }
    
    @Override
    public void operation(database database, Scanner s, user userParam) {
        System.out.print("Enter First Name: ");
        String firstName = s.nextLine();
        System.out.print("Enter Last Name: ");
        String lastName = s.nextLine();
        System.out.print("Enter Email: ");
        String email = s.nextLine();
        System.out.print("Enter Phone Number: ");
        String phoneNumber = s.nextLine();
        System.out.print("Enter Password: ");
        String password = s.nextLine();
        System.out.print("Confirm Password: ");
        String confirmPassword = s.nextLine();
        
        while (!password.equals(confirmPassword)) {
            System.out.println("Passwords do not match. Please try again.");
            System.out.print("Enter Password: ");
            password = s.nextLine();
            System.out.print("Confirm Password: ");
            confirmPassword = s.nextLine();
        }
        
        try {
            // Count total rows to generate ID safely
            ResultSet rs = database.getStatement().executeQuery("SELECT COUNT(*) AS total FROM `users`");
            int total = 0;
            if (rs.next()) {
                total = rs.getInt("total");
            }
            int ID = total + 1;

            String insert = "INSERT INTO `users`(`ID`, `FirstName`, `LastName`, `Email`, `PhoneNumber`, `Password`, `Type`) VALUES ('" 
                + ID + "','" + firstName + "','" + lastName + "','" + email + "','" + phoneNumber + "','" + password + "'," + accType + ")";
            
            int rows = database.getStatement().executeUpdate(insert);
            System.out.println("Insert affected rows: " + rows);
            System.out.println("Account Created successfully.");
            
            // AccType අනුව user object එක නිවැරදිව create කර data set කිරීම
            user newUser = switch (accType) {
                case 1 -> new admin();
                default -> new client();
            };
            newUser.setId(String.valueOf(ID));
            newUser.setfname(firstName);
            newUser.setlname(lastName);
            newUser.setemail(email);
            newUser.setphone(phoneNumber);
            newUser.setpassword(password);
            
            // අවශ්‍ය නම් account එක හැදුනු ගමන් menu එක দেখවීමට
            newUser.showList(database, s);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}