package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBconnection {
    private static final String url = "jdbc:mySQL://localhost:3306/flashcard";
    private static final String username = "root";
    private static final String password = "0934649781";
    private static Connection con;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");  // đảm bảo driver được load
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (con == null || con.isClosed()) {
            try {
                con = DriverManager.getConnection(url, username, password);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return con;
    }

    public static void closeConnection() throws SQLException {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
