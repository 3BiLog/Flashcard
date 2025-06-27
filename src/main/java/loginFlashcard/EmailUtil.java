package loginFlashcard;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

public class EmailUtil {
    // Thông tin cấu hình SMTP Gmail (hoặc dịch vụ khác nếu cần)
    private static final String SMTP_HOST     = "smtp.gmail.com";
    private static final int    SMTP_PORT     = 587;
    private static final String SMTP_USER     = "hackco297@gmail.com"; // Thay bằng email của bạn
    private static final String SMTP_PASSWORD = "kkfx layb krpl ouib";    // Thay bằng mật khẩu email của bạn

    private static Session createSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host",            SMTP_HOST);
        props.put("mail.smtp.port",            String.valueOf(SMTP_PORT));
        props.put("mail.debug",                "true");  // Bật log để dễ debug

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USER, SMTP_PASSWORD);
            }
        });
    }

    public static boolean sendEmail(String toEmail, String subject, String body) {
        try {
            Message msg = new MimeMessage(createSession());
            msg.setFrom(new InternetAddress(SMTP_USER, "App Flashcard")); // Tên người gửi tùy chỉnh
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            msg.setSubject(subject);
            msg.setText(body);
            Transport.send(msg);
            return true;
        } catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static boolean sendOTP(String toEmail, String otp) {
        String subject = "Mã xác thực đặt lại mật khẩu";
        String body = "Mã OTP của bạn là: " + otp + "\n\nKhông chia sẻ mã này với bất kỳ ai.";
        return sendEmail(toEmail, subject, body);
    }


}