package database;

import OOP.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProfileController {
//    public int getUser(String ten, String email) {
//        String sql = "select id from users where username = ? and email = ?";
//        try (Connection conn = DBconnection.getConnection();
//        PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setString(1, ten);
//            pstmt.setString(2, email);
//
//            try (ResultSet rs = pstmt.executeQuery()) {
//                if (rs.next()) {
//                    return rs.getInt("id");
//                }
//            }
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//        return -1;
//    }

    public boolean updateUser(int id,String ten, String email) {
        String sql = "update users set username = ?, email = ? where id = ?";
        try (Connection conn = DBconnection.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ten);
            pstmt.setString(2, email);
            pstmt.setInt(3, id);
            return pstmt.executeUpdate() > 0;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public User getUserById(int userId) {
        String sql = "SELECT username, email FROM users WHERE id = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String username = rs.getString("username");
                    String email = rs.getString("email");
                    return new User("GET_USER", userId, username, email, null);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lấy thông tin người dùng theo ID", e);
        }
        return null;
    }

    public boolean checkUsernameOrEmailExists(String username, String email, int excludeUserId) {
        String sql = "SELECT id FROM users WHERE (username = ? OR email = ?) AND id != ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setInt(3, excludeUserId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi kiểm tra username hoặc email: " + e.getMessage());
            throw new RuntimeException("Lỗi khi kiểm tra username hoặc email", e);
        }
    }


}
