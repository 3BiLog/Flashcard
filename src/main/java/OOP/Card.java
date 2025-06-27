package OOP;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class Card implements Serializable {
    private static final long serialVersionUID = 1L;

    private String command;
    private int id;
    private int boThe_id;
    private String english_text;
    private String vietnamese_text;

    // Thuộc tính Spaced Repetition (SM-2)
    private int repetitionCount;       // số lần lặp thành công liên tiếp
    private double intervalDays;          // số ngày giữa các lần học
    private double easeFactor;         // hệ số dễ nhớ
    private LocalDateTime lastReviewDate;  // ngày học gần nhất
    private LocalDateTime nextReviewDate;  // ngày cần học tiếp

    private LocalDateTime lastNotifiedAt; // thời điểm gần nhất đã gửi mail


    // Thuộc tính biểu đồ mức độ ghi nhớ
    private int memoryLevel;           // mức độ ghi nhớ (0 đến 5)
    private List<Card> cardList;

    public Card(String command, int id, int boThe_id, String english_text, String vietnamese_text) {
        this.command = command;
        this.id = id;
        this.boThe_id = boThe_id;
        this.english_text = english_text;
        this.vietnamese_text = vietnamese_text;

        // Khởi tạo mặc định cho spaced repetition
        this.repetitionCount = 0;
        this.intervalDays = 0;
        this.easeFactor = 2.5;
        this.lastReviewDate = LocalDateTime.now();
        this.nextReviewDate = LocalDateTime.now();
        this.memoryLevel = 0; // mặc định
        this.lastNotifiedAt = null; // chưa gửi mail
    }

    // Getter & Setter cho các thuộc tính gốc
    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBoThe_id() {
        return boThe_id;
    }

    public void setBoThe_id(int boThe_id) {
        this.boThe_id = boThe_id;
    }

    public String getEnglish_text() {
        return english_text;
    }

    public void setEnglish_text(String english_text) {
        this.english_text = english_text;
    }

    public String getVietnamese_text() {
        return vietnamese_text;
    }

    public void setVietnamese_text(String vietnamese_text) {
        this.vietnamese_text = vietnamese_text;
    }

    // Getter & Setter cho các thuộc tính SM-2
    public int getRepetitionCount() {
        return repetitionCount;
    }

    public void setRepetitionCount(int repetitionCount) {
        this.repetitionCount = repetitionCount;
    }

    public double getIntervalDays() {
        return intervalDays;
    }

    public void setIntervalDays(double intervalDays) {
        this.intervalDays = intervalDays;
    }

    public double getEaseFactor() {
        return easeFactor;
    }

    public void setEaseFactor(double easeFactor) {
        this.easeFactor = easeFactor;
    }

    public LocalDateTime getLastReviewDate() {
        return lastReviewDate;
    }

    public void setLastReviewDate(LocalDateTime lastReviewDate) {
        this.lastReviewDate = lastReviewDate;
    }

    public LocalDateTime getNextReviewDate() {
        return nextReviewDate;
    }

    public void setNextReviewDate(LocalDateTime nextReviewDate) {
        this.nextReviewDate = nextReviewDate;
    }

    // Getter & Setter cho memoryLevel
    public int getMemoryLevel() {
        return memoryLevel;
    }

    public void setMemoryLevel(int memoryLevel) {
        this.memoryLevel = memoryLevel;
    }

    public LocalDateTime getLastNotifiedAt() {
        return lastNotifiedAt;
    }

    public void setLastNotifiedAt(LocalDateTime lastNotifiedAt) {
        this.lastNotifiedAt = lastNotifiedAt;
    }

    public List<Card> getCardList() { return cardList; }
    public void setCardList(List<Card> cardList) { this.cardList = cardList; }


    /**
     * Cập nhật thông tin spaced repetition sau khi người dùng đánh giá (0–5).
     * @param grade điểm người dùng chọn sau khi ôn bài (0–5)
     */
    public void updateAfterReview(int grade) {
        this.memoryLevel = grade;
        this.command = "UPDATE_CARD_AFTER_REVIEW";

        if (grade == 0) {
            // Khi độ nhớ là 0, giữ nguyên như thẻ mới tạo
            this.repetitionCount = 0; // Reset số lần lặp
            this.intervalDays = 0;    // Không tính thời gian học tiếp
            this.lastReviewDate = LocalDateTime.now(); // Cập nhật ngày học gần nhất
            this.nextReviewDate = LocalDateTime.now(); // Giữ nextReviewDate là hiện tại, như thẻ mới
        } else {
            // Chỉ cập nhật spaced repetition nếu grade >= 2
            if (grade < 2) {
                this.repetitionCount = 0; // Reset nếu không nhớ tốt 0.0417
                this.intervalDays = 0.001; // 1 giờ
            } else {
                this.repetitionCount++; // Tăng số lần lặp khi nhớ tốt
                switch (grade) {
                    case 2 -> this.intervalDays = 0.333; // 8 giờ
                    case 3 -> this.intervalDays = 1.0;   // 1 ngày
                    case 4 -> this.intervalDays = 4.0;   // 4 ngày
                    case 5 -> this.intervalDays = 7.0;   // 7 ngày
                    default -> this.intervalDays = 0.0417; // Trường hợp không hợp lệ, mặc định 1 giờ
                }
                // Nếu đã ôn nhiều lần (repetitionCount > 1), tăng interval dựa trên easeFactor
                if (this.repetitionCount > 1) {
                    this.intervalDays = this.intervalDays * this.easeFactor;
                }

                this.easeFactor = this.easeFactor + (0.1 - (5 - grade) * (0.08 + (5 - grade) * 0.02));
                if (this.easeFactor < 1.3) this.easeFactor = 1.3;
            }

            this.lastReviewDate = LocalDateTime.now(); // Cập nhật ngày học gần nhất
            this.nextReviewDate = this.lastReviewDate.plusMinutes((long)(this.intervalDays * 1440)); // Tính thời gian học tiếp
        }
    }

}
