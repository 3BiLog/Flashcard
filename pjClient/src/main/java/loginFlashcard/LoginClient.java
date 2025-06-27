package loginFlashcard;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LoginClient extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/loginFlashcard/LoginClient.fxml"));
        Scene scene = new Scene(loader.load()); // bạn có thể thay đổi kích thước nếu muốn

        // Gắn file style.css
        scene.getStylesheets().add(getClass().getResource("/loginFlashcard/LoginStyle.css").toExternalForm());

        stage.setTitle("Login Page");
        stage.setScene(scene);
        stage.setResizable(false); // nếu bạn không muốn cho resize cửa sổ
        stage.show();
    }


    public static void main(String[] args) {
        // 👇 Đặt khởi tạo AWT Toolkit ở đây nếu dùng FreeTTS
        java.awt.Toolkit.getDefaultToolkit();

        // 👇 Gọi JavaFX Application
        launch(args);
    }
}
