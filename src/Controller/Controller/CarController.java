import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CarController {
    private final Object db;

    public CarController(Object db) {
        this.db = db;
        if (this.db == null || getStatement() == null) {
            throw new IllegalArgumentException("Database connection or statement is not initialized!");
        }
    }

    private Object getStatement() {
        try {
            Method method = db.getClass().getMethod("getStatement");
            return method.invoke(db);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to access the database statement", e);
        }
    }

    private int executeUpdate(String query) {
        try {
            Method method = getStatement().getClass().getMethod("executeUpdate", String.class);
            return (Integer) method.invoke(getStatement(), query);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to execute database update", cause(e));
        }
    }

    private ResultSet executeQuery(String query) {
        try {
            Method method = getStatement().getClass().getMethod("executeQuery", String.class);
            return (ResultSet) method.invoke(getStatement(), query);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to execute database query", cause(e));
        }
    }

    private Throwable cause(ReflectiveOperationException e) {
        return e instanceof InvocationTargetException && ((InvocationTargetException) e).getCause() != null
                ? ((InvocationTargetException) e).getCause() : e;
    }

    // Add new car
    public boolean addCar(String id, String brand, String model, double pricePerDay, String status) {
        String query = String.format("INSERT INTO `cars` (`ID`, `Brand`, `Model`, `PricePerDay`, `Status`) VALUES ('%s', '%s', '%s', %.2f, '%s')",
                id, brand, model, pricePerDay, status);
        return executeUpdate(query) > 0;
    }

    // Remove car by ID
    public boolean removeCar(String id) {
        return executeUpdate("DELETE FROM `cars` WHERE `ID` = '" + id + "'") > 0;
    }

    // Update car status (e.g., Available -> Rented)
    public boolean updateCarStatus(String id, String status) {
        return executeUpdate("UPDATE `cars` SET `Status` = '" + status + "' WHERE `ID` = '" + id + "'") > 0;
    }

    // Get all cars
    public ResultSet getAllCars() {
        return executeQuery("SELECT * FROM `cars`");
    }

    // Get cars by status
    public ResultSet getCarsByStatus(String status) {
        return executeQuery("SELECT * FROM `cars` WHERE `Status` = '" + status + "'");
    }
    // ... පරණ methods ටිකට පහත method එකත් එකතු කරන්න ...

    // Update car details
    public boolean updateCar(String id, String brand, String model, double pricePerDay, String status) {
        try {
            String query = String.format("UPDATE `cars` SET `Brand` = '%s', `Model` = '%s', `PricePerDay` = %.2f, `Status` = '%s' WHERE `ID` = '%s'",
                    brand, model, pricePerDay, status, id);
            return ((ResultSet) db).getStatement().executeUpdate(query) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}