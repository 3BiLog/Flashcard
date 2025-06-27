package FlashCard;

import java.awt.*;
import java.awt.TrayIcon.MessageType;
import java.net.URL;


public class ReviewTrayNotifier {
    private TrayIcon trayIcon;
    private static final String DEFAULT_ICON_PATH = "/user.png";

    public ReviewTrayNotifier() throws AWTException {
        this(DEFAULT_ICON_PATH);
    }


    public ReviewTrayNotifier(String iconPath) throws AWTException {
        if (!SystemTray.isSupported()) {
            throw new UnsupportedOperationException("System tray not supported on this platform");
        }
        SystemTray tray = SystemTray.getSystemTray();

        Image image = loadImage(iconPath);
        trayIcon = new TrayIcon(image, "FlashCard Notifier");
        trayIcon.setImageAutoSize(true);
        tray.add(trayIcon);
    }

    /**
     * Hiển thị thông báo popup báo số từ cần ôn.
     * @param dueCount số từ cần ôn
     */
    public void displayNotification(int dueCount) {
        if (trayIcon != null) {
            String title = "Ôn tập FlashCard";
            String message = dueCount + " từ cần ôn ngay bây giờ!";
            trayIcon.displayMessage(title, message, MessageType.INFO);
        }
    }


    private Image loadImage(String path) {
        URL url = getClass().getResource(path);
        if (url == null) {
            throw new IllegalArgumentException("Icon resource not found: " + path + ". Hãy kiểm tra file ảnh đã nằm trong resources và đường dẫn khớp.");
        }
        return Toolkit.getDefaultToolkit().getImage(url);
    }
}
