package Review;

import FlashCard.CardRequest;
import OOP.Card;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.CacheHint;
import javafx.scene.DepthTest;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import statistical.StatsRequest;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class review_centerController {

    @FXML private Label frontLabel;
    @FXML private Label backLabel;
    @FXML private StackPane cardPane;
    @FXML private HBox backPane;
    @FXML private Button xem;
    @FXML private Button speakButton;
    @FXML private ImageView speakerIcon;

    private int sessionDueCount;

    private List<Card> allCards;        // toàn bộ deck
    private int batchOffset;            // offset batch hiện tại (0, 5, 10, ...)
    private static final int BATCH_SIZE = 5;
    private boolean skipRating;
    public void initReviewSession(List<Card> allCards, int offset, boolean skipRating) {
        this.allCards = allCards;
        this.batchOffset = offset;
        this.skipRating = skipRating;
    }

    private List<Card> cards;
    private int currentIndex;
    private boolean showingFront = true;
    private review_bottomController bottomController;

    private final Map<Card,Integer> sessionRatings = new LinkedHashMap<>();


    public void setCards(List<Card> cards) {
        this.cards = cards;
        this.sessionDueCount = cards.size();
        currentIndex = 0;
        sessionRatings.clear();
        showCard();
    }



    public void setBottomController(review_bottomController ctrl) {
        this.bottomController = ctrl;
    }

    /**
     * Hiển thị thẻ hiện tại (mặt trước)
     */
    private void showCard() {
        Card c = cards.get(currentIndex);
        frontLabel.setText(c.getVietnamese_text());
        backLabel.setText(c.getEnglish_text());
        frontLabel.setVisible(true);
        frontLabel.setManaged(true);
        backPane.setVisible(false);
        backPane.setManaged(false);
        showingFront = true;
        updateProgress();
    }

    private void updateProgress() {
        if (bottomController != null) {
            bottomController.updateProgress(currentIndex + 1, cards.size());
        }
    }


    /**
     * Lật card với hiệu ứng 3D sau và trước.
     */
    @FXML
    public void flipCard() {
        // Vô hiệu hóa nút Xem để tránh nhấn liên tục
        xem.setDisable(true);

        RotateTransition hideFront = new RotateTransition(Duration.millis(300), cardPane);
        hideFront.setAxis(Rotate.Y_AXIS);
        hideFront.setFromAngle(0);
        hideFront.setToAngle(90);
        hideFront.setOnFinished(e -> {
            // Sau khi xoay 90 độ, đổi nội dung
            if (showingFront) {
                frontLabel.setVisible(false);
                frontLabel.setManaged(false);
                backPane.setVisible(true);
                backPane.setManaged(true);
                backLabel.setVisible(true);
                backLabel.setManaged(true);
                speakButton.setVisible(true);
                speakButton.setManaged(true);
            } else {
                frontLabel.setVisible(true);
                frontLabel.setManaged(true);
                backPane.setVisible(false);
                backPane.setManaged(false);
            }
            RotateTransition showBack = new RotateTransition(Duration.millis(300), cardPane);
            showBack.setAxis(Rotate.Y_AXIS);
            showBack.setFromAngle(270);
            showBack.setToAngle(360);
            showBack.setOnFinished(e2 -> {
                // Kích hoạt lại nút Xem
                xem.setDisable(false);
            });
            showBack.play();
            showingFront = !showingFront;
        });
        hideFront.play();
    }


    @FXML
    private void handleSpeak() {
        if (cards != null && currentIndex < cards.size()) {
            speakText(cards.get(currentIndex).getEnglish_text());
        }
    }

    private void speakText(String text) {
        System.setProperty("freetts.voices",
                "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
        VoiceManager vm = VoiceManager.getInstance();
        Voice voice = vm.getVoice("kevin");
        if (voice != null) {
            voice.allocate();
            voice.speak(text);
            voice.deallocate();
        } else {
            System.err.println("Voice 'kevin' not found.");
        }
    }

//    public void nextCard() {
//        if (currentIndex < cards.size() - 1) {
//            currentIndex++;
//            showCard();
//        }else {
//            Alert alert = new Alert(Alert.AlertType.INFORMATION);
//            alert.setTitle("Hoàn thành!");
//            alert.setHeaderText(null);
//            alert.setContentText("Bạn đã hoàn thành tất cả thẻ!");
//            alert.showAndWait();
//        }
//    }



    public void prevCard() {
        if (currentIndex > 0) {
            currentIndex--;
            showCard();
        }
    }

    public void nextCard() {
        // lưu rating mặc định
        Card c = cards.get(currentIndex);
        sessionRatings.putIfAbsent(c, 0);

        if (currentIndex == cards.size() - 1) {
            if (skipRating) {
                // chuyển sang batch tiếp theo
                int nextOffset = batchOffset + BATCH_SIZE;
                if (nextOffset < allCards.size()) {
                    // có batch mới → load tiếp
                    batchOffset = nextOffset;
                    List<Card> nextBatch = allCards.stream()
                            .skip(batchOffset)
                            .limit(BATCH_SIZE)
                            .collect(Collectors.toList());
                    setCards(nextBatch);
                } else {
                    // đã hết deck → quay về home
                    bottomController.finishSession();
                }
            } else {
                // đang review due → hỏi rating bình thường
                showBatchRatingDialog();
            }
        } else {
            // vẫn còn trong batch hiện tại
            currentIndex++;
            showCard();
        }
    }

    private void showBatchRatingDialog() {
        Dialog<Map<Card, Integer>> dialog = new Dialog<>();
        dialog.setTitle("Đánh giá độ nhớ");
        dialog.setHeaderText("Vui lòng đánh giá độ nhớ của từng thẻ (0-5):");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // Tạo danh sách combo box để người dùng chọn độ nhớ
        Map<Card, ComboBox<Integer>> ratingBoxes = new HashMap<>();
        int row = 0;
        for (Card card : sessionRatings.keySet()) {
            Label label = new Label(card.getVietnamese_text() + " - " + card.getEnglish_text());
            ComboBox<Integer> ratingBox = new ComboBox<>(FXCollections.observableArrayList(0, 1, 2, 3, 4, 5));
            ratingBox.setValue(sessionRatings.get(card)); // Giá trị mặc định
            ratingBoxes.put(card, ratingBox);

            grid.add(label, 0, row);
            grid.add(ratingBox, 1, row);
            row++;
        }

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                Map<Card, Integer> ratings = new HashMap<>();
                for (Map.Entry<Card, ComboBox<Integer>> entry : ratingBoxes.entrySet()) {
                    ratings.put(entry.getKey(), entry.getValue().getValue());
                }
                return ratings;
            }
            return null;
        });

        Optional<Map<Card, Integer>> result = dialog.showAndWait();
        result.ifPresent(ratings -> {
            // Cập nhật thông tin spaced repetition và lưu vào cơ sở dữ liệu
            ratings.forEach((card, grade) -> {
                card.updateAfterReview(grade); // Cập nhật SM-2
                // Lưu vào cơ sở dữ liệu
                try {
                    CardRequest cardController = new CardRequest();
                    cardController.updateCardAfterReview(card);
                } catch (Exception e) {
                    e.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Lỗi");
                    alert.setHeaderText(null);
                    alert.setContentText("Không thể lưu đánh giá vào cơ sở dữ liệu!");
                    alert.showAndWait();
                }
            });

            bottomController.finishSession();
        });
    }




}