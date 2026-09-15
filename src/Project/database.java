package Project;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class database {
    private String user = "root"; // root දාගැනීම localhost වලදී පහසුයි
    private String password = ""; // password එකක් නැත්නම් empty "" දෙන්න
    private String url = "jdbc:mysql://localhost:3306/vehiclerentalystem";
    private Connection connection;
    private Statement statement;

    public database(){
        try{
            connection = DriverManager.getConnection(url, user, password);
            statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public Statement getStatement(){
        return statement;
    }
    public Connection getConnection() {
        return connection;
    }

    public Connection getConnection1() {
        return connection;
    }
}