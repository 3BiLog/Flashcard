package database;

import OOP.boThe;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class boTheController {
    public List<boThe> getDecksByUser(int userId) throws SQLException {
        String sql = "SELECT id, userId, name,count_card FROM bo_the WHERE userId = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                List<boThe> list = new ArrayList<>();
                while (rs.next()) {
                    boThe deck = new boThe(
                            "GET_DECKS",
                            rs.getInt("id"),
                            rs.getInt("userId"),
                            rs.getString("name"),
                            rs.getInt("count_card")
                    );
                    list.add(deck);
                }
                return list;
            }
        }
    }
    /**
     * Chèn mới một bộ thẻ và trả về ID vừa sinh
     */
    public int insertDeck(boThe deck) throws SQLException {
        String sql = "INSERT INTO bo_the (userId, name,count_card) VALUES (?, ?,?)";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, deck.getUserId());
            ps.setString(2, deck.getName());
            ps.setInt(3, deck.getCount_card());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                } else {
                    throw new SQLException("Insert deck failed, no ID obtained.");
                }
            }
        }
    }
    // database/boTheController.java

    /** Cập nhật tên bộ thẻ */
    public boolean updateDeckName(int deckId, String newName) throws SQLException {
        String sql = "UPDATE bo_the SET name = ? WHERE id = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newName);
            ps.setInt(2, deckId);
            return ps.executeUpdate() > 0;
        }
    }

    /** Xoá một bộ thẻ (và tự động xoá card nếu có ràng buộc FK) */
    public boolean deleteDeck(int deckId) throws SQLException {
        String sql = "DELETE FROM bo_the WHERE id = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, deckId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<boThe> getDeck(int userId) throws SQLException {
        String sql =
                "SELECT b.id, b.userId, b.name, COUNT(c.id) AS card_count\n" +
                        "  FROM bo_the b\n" +
                        "  LEFT JOIN card c ON b.id = c.boThe_id\n" +
                        " WHERE b.userId = ?\n" +
                        " GROUP BY b.id, b.userId, b.name";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                List<boThe> list = new ArrayList<>();
                while (rs.next()) {
                    boThe deck = new boThe(
                            "GET_DECKS",
                            rs.getInt("id"),
                            rs.getInt("userId"),
                            rs.getString("name"),
                            rs.getInt("card_count")   // lấy count từ JOIN
                    );
                    list.add(deck);
                }
                return list;
            }
        }
    }






}
