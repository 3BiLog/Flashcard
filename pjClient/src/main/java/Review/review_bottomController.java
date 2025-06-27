package Review;

import FlashCard.CardRequest;
import FlashCard.card_bottomController;
import FlashCard.card_centerController;
import FlashCard.trangChuController;
import OOP.Card;
import OOP.boThe;
import Write.write_centerController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.util.List;

public class review_bottomController {
    @FXML private Button backBtn;
    @FXML private Button nextBtn;
    @FXML private ProgressBar progressBar;
    @FXML private Label progressLabel;
    @FXML private Button thoat;

    private review_centerController centerController;
    private BorderPane mainRoot;
    private trangChuController parentController;
    private boThe deck;
    /**
     * Thiết lập trung tâm và danh sách thẻ
     */
    public void setContext(BorderPane mainRoot, boThe deck, trangChuController parentController) {
        this.mainRoot = mainRoot;
        this.deck = deck;
        this.parentController = parentController;
    }

    public void setCenterController(review_centerController centerController) {
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
        if (centerController != null) centerController.flipCard();
    }

    /**
     * Cập nhật thanh tiến độ và nhãn
     */
    public void updateProgress(int current, int total) {
        double frac = total <= 0 ? 0 : (double) current / total;
        progressBar.setProgress(frac);
        progressLabel.setText(current + "/" + total);
    }

    @FXML
    private void handleQuayLai() {
        if (mainRoot == null || deck == null || parentController == null) {
            System.err.println("write_bottomController: context chưa được thiết lập trước khi gọi handleQuayLai");
            return;
        }
        try {
            // Load phần Center của FlashCard
            FXMLLoader centerLoader = new FXMLLoader(getClass().getResource("/FlashCard/card_center.fxml"));
            Parent centerPane = centerLoader.load();
            card_centerController centerCtrl = centerLoader.getController();
            centerCtrl.setBoThe_id(deck.getDeckId());
            centerCtrl.setController(parentController);

            // Load phần Bottom của FlashCard
            FXMLLoader bottomLoader = new FXMLLoader(getClass().getResource("/FlashCard/card_bottom.fxml"));
            Parent bottomPane = bottomLoader.load();
            card_bottomController bottomCtrl = bottomLoader.getController();
            bottomCtrl.setContext(mainRoot, deck, parentController);




            // Gán lại center và bottom
            mainRoot.setCenter(centerPane);
            mainRoot.setBottom(bottomPane);

            parentController.restoreHome();//them sau
        } catch (IOException e) {
            e.printStackTrace();
            // TODO: Hiển thị Alert cho người dùng
        }
    }

    // Khi batch dialog xong, quay về Home
    public void finishSession() {
        parentController.restoreHome();
        parentController.loadDecksFromServer();
    }

}
