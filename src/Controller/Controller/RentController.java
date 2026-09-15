import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RentController {
    private final Database db;

    public RentController(Database db) {
        this.db = db;
    }

    private Connection getConnection() throws SQLException {
        if (db == null) return null;
        try {
            for (Method method : db.getClass().getMethods()) {
                if (method.getParameterCount() == 0
                        && Connection.class.isAssignableFrom(method.getReturnType())) {
                    return (Connection) method.invoke(db);
                }
            }
            for (Field field : db.getClass().getDeclaredFields()) {
                if (Connection.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    return (Connection) field.get(db);
                }
            }
            return null;
        } catch (ReflectiveOperationException | SecurityException e) {
            throw new SQLException("Unable to obtain database connection", e);
        }
    }

    public ResultSet getAllRentals() throws SQLException {
        Connection connection = getConnection();
        if (connection == null) return null;
        String sql = "SELECT * FROM `rentals`";
        PreparedStatement pst = connection.prepareStatement(sql);
        return pst.executeQuery();
    }
    
    public boolean updateRentalStatus(String bookingId, String newStatus) throws SQLException {
        Connection connection = getConnection();
        if (connection == null) return false;
        String sql = "UPDATE `rentals` SET Status = ? WHERE BookingID = ?";
        PreparedStatement pst = connection.prepareStatement(sql);
        pst.setString(1, newStatus);
        pst.setString(2, bookingId);
        int rows = pst.executeUpdate();
        return rows > 0;
    }
}