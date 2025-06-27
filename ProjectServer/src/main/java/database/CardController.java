package database;

import Mail.SchedulerService;
import OOP.Card;
import OOP.User;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CardController {
//    public List<Card> getCards(int boThe_id) {
//        String sql = "select id,boThe_id,english_text,vietnamese_text from card where boThe_id=?";
//        try (Connection conn = DBconnection.getConnection();
//        PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setInt(1, boThe_id);
//            try (ResultSet rs = pstmt.executeQuery()) {
//                List<Card> cards = new ArrayList<>();
//                while (rs.next()) {
//                    Card c = new Card("GET_CARD",
//                            rs.getInt("id"),
//                            rs.getInt("boThe_id"),
//                            rs.getString("english_text"),
//                            rs.getString("vietnamese_text"));
//                    cards.add(c);
//                }
//                return cards;
//            }
//
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
public List<Card> getCards(int boThe_id) {
    String sql = "SELECT id, boThe_id, english_text, vietnamese_text, repetition_count, interval_days, ease_factor, last_review_date, next_review_date, memory_level FROM card WHERE boThe_id=?";
    try (Connection conn = DBconnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, boThe_id);
        try (ResultSet rs = pstmt.executeQuery()) {
            List<Card> cards = new ArrayList<>();
            while (rs.next()) {
                Card c = new Card("GET_CARD",
                        rs.getInt("id"),
                        rs.getInt("boThe_id"),
                        rs.getString("english_text"),
                        rs.getString("vietnamese_text"));
                c.setRepetitionCount(rs.getInt("repetition_count"));
                c.setIntervalDays(rs.getDouble("interval_days"));
                c.setEaseFactor(rs.getDouble("ease_factor"));
                c.setLastReviewDate(rs.getTimestamp("last_review_date") != null ? rs.getTimestamp("last_review_date").toLocalDateTime() : null);
                c.setNextReviewDate(rs.getTimestamp("next_review_date") != null ? rs.getTimestamp("next_review_date").toLocalDateTime() : null);
                c.setMemoryLevel(rs.getInt("memory_level"));
                cards.add(c);
            }
            return cards;
        }
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
}

//    public int insertCard(Card card) {
//        String sql = "insert into card (boThe_id,english_text,vietnamese_text) values(?,?,?)";
//        try (Connection conn = DBconnection.getConnection();
//        PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
//            pstmt.setInt(1, card.getBoThe_id());
//            pstmt.setString(2, card.getEnglish_text());
//            pstmt.setString(3, card.getVietnamese_text());
//            pstmt.executeUpdate();
//            try (ResultSet rs = pstmt.getGeneratedKeys()) {
//                if (rs.next()) {
//                    return rs.getInt(1);
//                }else{
//                    throw new SQLException();
//                }
//
//            }
//
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

    public int insertCard(Card card) {
        String sql = "INSERT INTO card (boThe_id, english_text, vietnamese_text, repetition_count, interval_days, ease_factor, last_review_date, next_review_date, memory_level) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, card.getBoThe_id());
            pstmt.setString(2, card.getEnglish_text());
            pstmt.setString(3, card.getVietnamese_text());
            pstmt.setInt(4, card.getRepetitionCount());
            pstmt.setDouble(5, card.getIntervalDays());
            pstmt.setDouble(6, card.getEaseFactor());
            pstmt.setTimestamp(7, card.getLastReviewDate() != null ? Timestamp.valueOf(card.getLastReviewDate()) : null);
            pstmt.setTimestamp(8, card.getNextReviewDate() != null ? Timestamp.valueOf(card.getNextReviewDate()) : null);
            pstmt.setInt(9, card.getMemoryLevel());
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new SQLException();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean updateCard(int id ,String english_text, String vietnamese_text) throws SQLException {
        String sql = "update card set english_text=?,vietnamese_text=? where id=?";
        try (Connection conn = DBconnection.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, english_text);
            pstmt.setString(2, vietnamese_text);
            pstmt.setInt(3, id);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean deleteCard(int id) throws SQLException {
        String sql = "delete from card where id=?";
        try (Connection conn = DBconnection.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int countCardsInDeck(int deckId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM card WHERE boThe_id = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, deckId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }


    public void updateCardAfterReview(Card card) throws SQLException {
        String sql = "UPDATE card SET english_text = ?, vietnamese_text = ?, repetition_count = ?, interval_days = ?, ease_factor = ?, last_review_date = ?, next_review_date = ?, memory_level = ? WHERE id = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, card.getEnglish_text());
            pstmt.setString(2, card.getVietnamese_text());
            pstmt.setInt(3, card.getRepetitionCount());
            pstmt.setDouble(4, card.getIntervalDays());
            pstmt.setDouble(5, card.getEaseFactor());
            pstmt.setTimestamp(6, card.getLastReviewDate() != null ? Timestamp.valueOf(card.getLastReviewDate()) : null);
            pstmt.setTimestamp(7, card.getNextReviewDate() != null ? Timestamp.valueOf(card.getNextReviewDate()) : null);
            pstmt.setInt(8, card.getMemoryLevel());
            pstmt.setInt(9, card.getId());

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                System.err.println("No rows updated for card ID=" + card.getId());
                throw new SQLException("Failed to update card: No rows affected for ID=" + card.getId());
            }
            System.out.println("Card updated in DB: ID=" + card.getId() +
                    ", MemoryLevel=" + card.getMemoryLevel() +
                    ", NextReview=" + card.getNextReviewDate());


            LocalDateTime next = card.getNextReviewDate();
            int userId = fetchUserIdByDeckId(card.getBoThe_id());
            Mail.SchedulerService.scheduleEmailGrouped(userId, next, card);
        } catch (SQLException e) {
            System.err.println("SQLException in updateCardAfterReview: " + e.getMessage());
            throw e;
        }
    }


    public static Map<Integer, Long> fetchDueCounts() throws SQLException {
        String sql = """
        SELECT u.id AS userId, COUNT(c.id) AS dueCount
        FROM users u
        JOIN bo_the d ON u.id = d.userId
        JOIN card c ON d.id = c.boThe_id
        WHERE c.next_review_date <= NOW()
          AND c.last_notified_at IS NULL
        GROUP BY u.id
        """;

        Map<Integer, Long> dueMap = new HashMap<>();
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int userId = rs.getInt("userId");
                long count = rs.getLong("dueCount");
                dueMap.put(userId, count);
            }
        }
        return dueMap;
    }

    /**
     * Đánh dấu đã gửi thông báo cho tất cả card của các user trong map để tránh gửi lại
     */
    public static void markNotified(Map<Integer, Long> dueMap) throws SQLException {
        String sql = """
        UPDATE card c
        JOIN bo_the d ON c.boThe_id = d.id
        SET c.last_notified_at = NOW()
        WHERE d.userId = ?
          AND c.next_review_date <= NOW()
          AND c.last_notified_at IS NULL
        """;
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Integer userId : dueMap.keySet()) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }
        }
    }


    /**
     * Lấy email của user theo ID
     */
    public static String fetchUserEmail(int userId) throws SQLException {
        String sql = "SELECT email FROM users WHERE id = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("email");
                }
            }
        }
        return null;
    }

    public static void markNotifiedSingleUser(int userId) throws SQLException {
        String sql = """
        UPDATE card c
        JOIN bo_the d ON c.boThe_id = d.id
        SET c.last_notified_at = NOW()
        WHERE d.userId = ?
          AND c.next_review_date <= NOW()
          AND c.last_notified_at IS NULL
        """;
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    /**
     * Trả về userId của deck (bo_the) theo deckId.
     */
    public static int fetchUserIdByDeckId(int deckId) throws SQLException {
        String sql = "SELECT userId FROM bo_the WHERE id = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, deckId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("userId");
                }
            }
        }
        throw new SQLException("Không tìm thấy bo_the với id=" + deckId);
    }

    public static List<Card> fetchDueCardsByUserId(int userId) throws SQLException {
        String sql = """
        SELECT c.*
        FROM card c
        JOIN bo_the d ON c.boThe_id = d.id
        WHERE d.userId = ?
          AND c.next_review_date <= NOW()
          AND c.last_notified_at IS NULL
    """;
        List<Card> cards = new ArrayList<>();
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Card c = new Card("GET_CARD",
                            rs.getInt("id"),
                            rs.getInt("boThe_id"),
                            rs.getString("english_text"),
                            rs.getString("vietnamese_text"));
                    // Thiết lập các thuộc tính khác nếu cần
                    cards.add(c);
                }
            }
        }
        return cards;
    }




}
