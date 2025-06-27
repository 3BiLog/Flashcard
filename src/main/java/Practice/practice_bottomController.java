package Practice;

import FlashCard.card_bottomController;
import FlashCard.card_centerController;
import FlashCard.trangChuController;
import OOP.boThe;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class practice_bottomController {
    @FXML private Button backBtn;
    @FXML private Button nextBtn;
    @FXML private ProgressBar progressBar;
    @FXML private Label progressLabel;
    @FXML private Button thoat;

    private practice_centerController centerController;
    private BorderPane mainRoot;
    private trangChuController parentController;
    private boThe deck;

    public void setContext(BorderPane mainRoot, boThe deck, trangChuController parentController) {
        this.mainRoot = mainRoot;
        this.deck = deck;
        this.parentController = parentController;
    }

    public void setCenterController(practice_centerController centerController) {
        this.centerController = centerController;
    }

    @FXML
    private void handleBack() {
        if (centerController != null) centerController.prevCard();
    }

    @FXML
    private void handleNext() {
        if (centerController != null) centerController.nextCard();
    }

    @FXML
    private void handleView() {
        // Not used in practice mode, can be removed or implemented if needed
    }

    public void updateProgress(int current, int total) {
        double frac = total <= 0 ? 0 : (double) current / total;
        progressBar.setProgress(frac);
        progressLabel.setText(current + "/" + total);
    }

    @FXML
    private void handleQuayLai() {
        if (mainRoot == null || deck == null || parentController == null) {
            System.err.println("practice_bottomController: context chưa được thiết lập trước khi gọi handleQuayLai");
            return;
        }
        try {
            FXMLLoader centerLoader = new FXMLLoader(getClass().getResource("/FlashCard/card_center.fxml"));
            Parent centerPane = centerLoader.load();
            card_centerController centerCtrl = centerLoader.getController();
            centerCtrl.setBoThe_id(deck.getDeckId());
            centerCtrl.setController(parentController);

            FXMLLoader bottomLoader = new FXMLLoader(getClass().getResource("/FlashCard/card_bottom.fxml"));
            Parent bottomPane = bottomLoader.load();
            card_bottomController bottomCtrl = bottomLoader.getController();
            bottomCtrl.setContext(mainRoot, deck, parentController);

            mainRoot.setCenter(centerPane);
            mainRoot.setBottom(bottomPane);
        } catch (IOException e) {
            e.printStackTrace();
            // TODO: Hiển thị Alert cho người dùng
        }
    }

    public void finishSession() {
        parentController.restoreHome();
        parentController.loadDecksFromServer();
    }
}