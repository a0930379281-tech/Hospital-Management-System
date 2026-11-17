package ca.learncodegap.hospital;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public final class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/hospital_db";
    private static final String USER = "root";
    // Set MYSQL_PASSWORD environment variable to your local MySQL password.
    private static final String PASSWORD = System.getenv("MYSQL_PASSWORD");
    private DatabaseConnection() {}
    public static Connection open() throws SQLException {
        if (PASSWORD == null || PASSWORD.isBlank()) throw new SQLException("Set MYSQL_PASSWORD environment variable before running.");
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
