package Project;

import Controller.EditMyData;
import Controller.RentCar;
import Controller.ReturnCar;
import Controller.ShowMyRents;
import Controller.ViewCars;
import java.util.Scanner;

public class client extends user {
    private final Operation[] operations = {
        new ViewCars(),     // 1. View Cars
        new RentCar(),      // 2. Rent Car
        new ReturnCar(),    // 3. Return Car
        new ShowMyRents(),  // 4. Show My Rents
        new EditMyData()    // 5. Edit My Data
    };

    public client() {
        super();
    }

    @Override
    public void showList(database databse, Scanner s) {
        System.out.println("\n1. View Cars");
        System.out.println("2. Rent Car");
        System.out.println("3. Return Car");
        System.out.println("4. Show My Rents");
        System.out.println("5. Edit My Data");
        System.out.println("6. Quit\n");
        System.out.print("Select an option: ");

        String input = s.nextLine().trim();
        int i;
        try {
            i = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input! Please enter a number (1-6).");
            showList(databse, s);
            return;
        }

        if (i == 6) {
            System.out.println("Quitting client panel...");
            return;
        }

        if (i >= 1 && i <= 5) {
            if (operations[i - 1] != null) {
                operations[i - 1].operation(databse, s, this);
            } else {
                System.out.println("This feature is not implemented yet!");
            }
        } else {
            System.out.println("Invalid option! Please select between 1 and 6.");
        }

        showList(databse, s);
    }

    @Override
    protected void setId1(String iD2) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}