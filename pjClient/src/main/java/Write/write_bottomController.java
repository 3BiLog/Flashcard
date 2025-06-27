package Write;

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

public class write_bottomController {
    @FXML private Button backBtn;
    @FXML private Button nextBtn;
    @FXML private ProgressBar progressBar;
    @FXML private Label progressLabel;
    @FXML private Button quaylai;

    private write_centerController centerController;
    private BorderPane mainRoot;
    private boThe deck;
    private trangChuController parentController;

    public void setContext(BorderPane mainRoot, boThe deck, trangChuController parentController) {
        this.mainRoot = mainRoot;
        this.deck = deck;
        this.parentController = parentController;
    }

    /**
     * Đặt controller trung tâm (write_centerController) để điều khiển prev/next và cập nhật progress
     */
    public void setCenterController(write_centerController centerController) {
        this.centerController = centerController;
    }

    @FXML
    private void handleBack() {
        if (centerController != null) {
            centerController.prevCard();
        }
    }

    @FXML
    private void handleNext() {
        if (centerController != null) {
            centerController.nextCard();
        }
    }

    /**
     * Cập nhật thanh tiến độ và nhãn hiển thị
     */
    public void updateProgress(int current, int total) {
        double fraction = (total <= 1) ? 1 : (double)(current - 1) / (total - 1);
        progressBar.setProgress(fraction);
        progressLabel.setText(current + "/" + total);
    }

    /**
     * Xử lý khi người dùng nhấn nút "Quay lại" để trở về giao diện FlashCard
     */
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
