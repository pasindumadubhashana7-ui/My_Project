package Controller;

import Project.admin;
import Project.client;
import Project.database;
import Project.user;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        database database = new database();
        Scanner s = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== Welcome to Vehicle Rental System ===");
            System.out.println("Enter Your Email (-1 to create new account, 0 to exit):");
            
            if (!s.hasNextLine()) break;
            String email = s.nextLine().trim();

            if (email.equals("0")) {
                System.out.println("Exiting application. Goodbye!");
                break;
            }

            // Handle -1 for new account creation
            if (email.equals("-1")) {
                System.out.println("--- Create New Account ---");
                System.out.print("Enter First Name: ");
                String fname = s.nextLine().trim();
                System.out.print("Enter Last Name: ");
                String lname = s.nextLine().trim();
                System.out.print("Enter Email: ");
                String em = s.nextLine().trim();
                System.out.print("Enter Phone Number: ");
                String phone = s.nextLine().trim();
                System.out.print("Enter Password: ");
                String pass = s.nextLine().trim();
                System.out.print("Confirm Password: ");
                String cpass = s.nextLine().trim();
                
                while (!pass.equals(cpass)) {
                    System.out.println("Passwords do not match. Please try again.");
                    System.out.print("Enter Password: ");
                    pass = s.nextLine().trim();
                    System.out.print("Confirm Password: ");
                    cpass = s.nextLine().trim();
                }
                
                System.out.print("Enter Type (0 for Client, 1 for Admin): ");
                int type = Integer.parseInt(s.nextLine().trim());

                String id = UUID.randomUUID().toString().substring(0, 8);

                try {
                    String insertQuery = "INSERT INTO users (ID, FirstName, LastName, Email, PhoneNumber, Password, Type) VALUES ('"
                            + id + "', '" + fname + "', '" + lname + "', '" + em + "', '" + phone + "', '" + pass + "', " + type + ");";
                    database.getStatement().executeUpdate(insertQuery);
                    System.out.println("Account created successfully! You can now login.");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                continue;
            }

            // Load users from DB
            ArrayList<user> users = new ArrayList<>();
            try {
                String query = "SELECT * FROM `users`;";
                ResultSet rs = database.getStatement().executeQuery(query);
                while (rs.next()) {
                    user userObj;
                    String ID = rs.getString("ID");
                    String firstName = rs.getString("FirstName");
                    String lastName = rs.getString("LastName");
                    String em = rs.getString("Email");
                    String phoneNumber = rs.getString("PhoneNumber");
                    String pass = rs.getString("Password");
                    int type = rs.getInt("Type");
                    userObj = switch (type) {
                        case 0 -> new client();
                        case 1 -> new admin();
                        default -> new client();
                    };
                    userObj.setId(ID);
                    userObj.setfname(firstName);
                    userObj.setlname(lastName);
                    userObj.setemail(em);
                    userObj.setphone(phoneNumber);
                    userObj.setpassword(pass);
                    users.add(userObj);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // Find user by email first
            user foundUser = null;
            for (user u : users) {
                if (u.getemail() != null && u.getemail().trim().equalsIgnoreCase(email)) {
                    foundUser = u;
                    break;
                }
            }

            if (foundUser == null) {
                System.out.println("No account found with this email! Please try again.");
                continue;
            }

            System.out.print("Enter Your Password: ");
            String password = s.nextLine().trim();

            // Password retry loop
            while (true) {
                if (foundUser.getpassword() != null && foundUser.getpassword().trim().equals(password)) {
                    System.out.println("Welcome " + foundUser.getfname() + " " + foundUser.getlname() + "!");
                    foundUser.showList(database, s);
                    break;
                } else {
                    System.out.println("Invalid password!");
                    System.out.print("Do you want to retry password? (1 = Retry, 0 = Back to main menu): ");
                    String choice = s.nextLine().trim();
                    if (choice.equals("1")) {
                        System.out.print("Enter Your Password: ");
                        password = s.nextLine().trim();
                    } else {
                        break;
                    }
                }
            }
        }
    }
}