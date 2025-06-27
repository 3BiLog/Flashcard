package FlashCard;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Scale;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class trangChu extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // 1. Load FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FlashCard/trangChu.fxml"));
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(getClass().getResource("/FlashCard/trangChu.css").toExternalForm());

        // 6. Thiết lập stage
        stage.setTitle("FlashCard Trang Chủ");
        stage.setScene(scene);

        stage.show();
    }
}
