package loginFlashcard;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SceneSwitcher {

    public static void slideOutAndSwitchScene(Stage stage, Node animatedNode, String fxmlPath, String cssPath, String title, boolean toLeft) {
        double slideDistance = toLeft ? -stage.getWidth() : stage.getWidth();

        TranslateTransition slide = new TranslateTransition(Duration.millis(600), animatedNode);
        slide.setByX(slideDistance);
        slide.setOnFinished(event -> {
            try {
                switchScene(stage, fxmlPath, cssPath, title);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        slide.play();
    }

    public static void switchScene(Stage stage, String fxmlPath, String cssPath, String title) throws Exception {
        FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource(fxmlPath));
        Parent newRoot = loader.load();

        if (cssPath != null && !cssPath.isEmpty()) {
            newRoot.getStylesheets().add(SceneSwitcher.class.getResource(cssPath).toExternalForm());
        }

        Scene newScene = new Scene(newRoot, stage.getScene().getWidth(), stage.getScene().getHeight());
        stage.setScene(newScene);
        stage.setTitle(title);
    }
}
