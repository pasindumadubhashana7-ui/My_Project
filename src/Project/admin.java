package Project;

import Controller.AddNewAccount;
import java.util.Scanner;

public class admin extends user {
    private final Operation[] operations = {
        null, // 1. Add New Car
        null, // 2. View Cars
        null, // 3. Update Car
        null, // 4. Delete Car
        new AddNewAccount(1), // 5. Add New Admin
        null  // 6. Show Rents
    };

    public admin() {
        super();
    }

    @Override
    public void showList(database databse, Scanner s) {
        System.out.println("\n1. Add New Car");      
        System.out.println("2. View Cars");
        System.out.println("3. Update Car");
        System.out.println("4. Delete Car");
        System.out.println("5. Add New Admin");
        System.out.println("6. Show Rents");
        System.out.println("7. Quit\n");
        System.out.print("Select an option: ");

        String input = s.nextLine().trim();
        int i;
        try {
            i = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input! Please enter a number (1-7).");
            showList(databse, s);
            return;
        }

        if (i == 7) {
            return;
        }

        if (i >= 1 && i <= 6) {
            if (operations[i - 1] != null) {
                operations[i - 1].operation(databse, s, this);
            } else {
                System.out.println("This feature is not implemented yet!");
            }
        } else {
            System.out.println("Invalid option! Please select between 1 and 7.");
        }

        showList(databse, s);
    }

    @Override
    protected void setId1(String iD2) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}