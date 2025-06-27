package loginFlashcard;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class RegisterClient extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/loginFlashcard/RegisterClient.fxml"));
        Scene scene = new Scene(loader.load()); // bạn có thể thay đổi kích thước nếu muốn

        // Gắn file style.css
        scene.getStylesheets().add(getClass().getResource("/loginFlashcard/RegisterStyle.css").toExternalForm());

        stage.setTitle("Login Page");
        stage.setScene(scene);
        stage.setResizable(false); // nếu bạn không muốn cho resize cửa sổ
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
