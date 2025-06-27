package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RegisterController {
    public boolean register(String username, String email,String password) {
        String checkUsername = "SELECT id FROM users WHERE username = ?";
        String checkMail = "SELECT id FROM users WHERE email = ?";
        String insertSql = "INSERT INTO users (username,email, password) VALUES (?, ?,?)";

        try (Connection conn = DBconnection.getConnection();
             PreparedStatement checkUserPs = conn.prepareStatement(checkUsername);
             PreparedStatement insertPs = conn.prepareStatement(insertSql);
             PreparedStatement checkMailPs = conn.prepareStatement(checkMail)) {

            // Kiểm tra username đã tồn tại
            checkUserPs.setString(1, username);
            ResultSet rs = checkUserPs.executeQuery();
            if (rs.next()) {
                return false; // đã tồn tại
            }

            //kiểm tra mail
            checkMailPs.setString(1, email);
            ResultSet rs2 = checkMailPs.executeQuery();
            if (rs2.next()) {
                return false;
            }


            // Thực hiện chèn mới
            insertPs.setString(1, username);
            insertPs.setString(2, email);
            insertPs.setString(3, password);
            insertPs.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
