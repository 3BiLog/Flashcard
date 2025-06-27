package Mail;

/**
 * EmailService là lớp hỗ trợ gửi các loại email có sẵn nội dung.
 */
public class EmailService {
    private final EmailSender sender;

    public EmailService() {
        this.sender = new EmailSender();
    }

//    public void sendReviewReminder(String toEmail, long dueCount) {
//        String subject = "🔔 Nhắc nhở ôn tập từ vựng";
//        String body = "Bạn có " + dueCount + " từ vựng cần ôn tập hôm nay. Hãy đăng nhập ứng dụng Flashcard để luyện tập nhé!";
//
//        try {
//            sender.sendEmail(toEmail, subject, body);
//        } catch (Exception e) {
//            System.err.println("❌ Gửi email thất bại đến " + toEmail + ": " + e.getMessage());
//        }
//    }

    public void sendCustomEmail(String toEmail, String subject, String body) {
        try {
            sender.sendEmail(toEmail, subject, body);
        } catch (Exception e) {
            System.err.println("❌ Gửi email thất bại đến " + toEmail + ": " + e.getMessage());
        }
    }

}
