package database;

import OOP.Stat;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class statController {

    public List<Stat> getStatsByUser(int userId) throws SQLException {
        List<Stat> stats = new ArrayList<>();
        String query = "SELECT reviewDate, reviewCount FROM stat WHERE userId = ? ORDER BY reviewDate";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LocalDate date = rs.getDate("reviewDate").toLocalDate();
                    int count = rs.getInt("reviewCount");
                    stats.add(new Stat("GET_STATS", date, count, userId));
                }
            }
        }
        return stats;
    }

    public Stat getStatByDate(int userId, LocalDate date) throws SQLException {
        String query = "SELECT reviewCount FROM stat WHERE userId = ? AND reviewDate = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setDate(2, Date.valueOf(date));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("reviewCount");
                    return new Stat("GET_STAT_BY_DATE", date, count, userId);
                }
            }
        }
        return new Stat("GET_STAT_BY_DATE", date, 0, userId);
    }

    public void updateStat(int userId, LocalDate date, int count) throws SQLException {
        String checkQuery = "SELECT COUNT(*) FROM stat WHERE userId = ? AND reviewDate = ?";
        String updateQuery = "UPDATE stat SET reviewCount = ? WHERE userId = ? AND reviewDate = ?";
        String insertQuery = "INSERT INTO stat (userId, reviewDate, reviewCount, createdAt) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";

        try (Connection conn = DBconnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, userId);
                checkStmt.setDate(2, Date.valueOf(date));
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setInt(1, count);
                        updateStmt.setInt(2, userId);
                        updateStmt.setDate(3, Date.valueOf(date));
                        updateStmt.executeUpdate();
                    }
                } else {
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                        insertStmt.setInt(1, userId);
                        insertStmt.setDate(2, Date.valueOf(date));
                        insertStmt.setInt(3, count);
                        insertStmt.executeUpdate();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void syncStatsFromCards() throws SQLException {
        // Bước 1: Thu thập dữ liệu từ ResultSet trước
        List<Object[]> statDataList = new ArrayList<>();
        String query = "SELECT b.userId, DATE(c.last_review_date) as reviewDate, COUNT(*) as count " +
                "FROM card c " +
                "JOIN bo_the b ON c.boThe_id = b.id " +
                "WHERE c.last_review_date IS NOT NULL " +
                "AND c.memory_level > 0 " +
                "GROUP BY b.userId, DATE(c.last_review_date)";

        try (Connection conn = DBconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int userId = rs.getInt("userId");
                LocalDate date = rs.getDate("reviewDate").toLocalDate();
                int count = rs.getInt("count");
                statDataList.add(new Object[]{userId, date, count});
            }
        } catch (SQLException e) {
            System.err.println("Error in syncStatsFromCards while fetching data: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        // Bước 2: Xóa các bản ghi stat không còn liên quan
        String deleteQuery = "DELETE FROM stat WHERE userId = ? AND reviewDate = ? AND NOT EXISTS (" +
                "SELECT 1 FROM card c JOIN bo_the b ON c.boThe_id = b.id " +
                "WHERE b.userId = ? AND DATE(c.last_review_date) = ? AND c.memory_level > 0)";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {
            for (Object[] statData : statDataList) {
                int userId = (int) statData[0];
                LocalDate date = (LocalDate) statData[1];
                deleteStmt.setInt(1, userId);
                deleteStmt.setDate(2, Date.valueOf(date));
                deleteStmt.setInt(3, userId);
                deleteStmt.setDate(4, Date.valueOf(date));
                deleteStmt.executeUpdate();
            }
            // Xóa các bản ghi stat không còn bất kỳ card nào liên quan
            String cleanupQuery = "DELETE FROM stat WHERE NOT EXISTS (" +
                    "SELECT 1 FROM card c JOIN bo_the b ON c.boThe_id = b.id " +
                    "WHERE b.userId = stat.userId AND DATE(c.last_review_date) = stat.reviewDate AND c.memory_level > 0)";
            try (PreparedStatement cleanupStmt = conn.prepareStatement(cleanupQuery)) {
                cleanupStmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error in syncStatsFromCards while deleting stale stats: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        // Bước 3: Cập nhật hoặc chèn dữ liệu mới
        for (Object[] statData : statDataList) {
            try {
                int userId = (int) statData[0];
                LocalDate date = (LocalDate) statData[1];
                int count = (int) statData[2];
                updateStat(userId, date, count);
                System.out.println("Synced Stat: userId=" + userId + ", date=" + date + ", count=" + count);
            } catch (SQLException e) {
                System.err.println("Error in syncStatsFromCards while updating stat for userId=" + statData[0] + ", date=" + statData[1] + ": " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        }
    }
}