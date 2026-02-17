package utils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBconnection {
final String URL = "jdbc:mysql://localhost:3306/humadb";
    final String USER = "root";
    final String PASS = "";

    private Connection connection;

    private static DBconnection instance;

    private DBconnection()
    {
        try
        {
            connection = DriverManager.getConnection(URL,USER,PASS);
            System.out.println("Connected to database successfully");
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }
    public static DBconnection getInstance()
    {
        if (instance == null)
        {
            instance = new DBconnection();
        }
        return instance;
    }
    public Connection getConnection()
    {
        return connection;
    }
}