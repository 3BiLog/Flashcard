package Write;

import FlashCard.CardRequest;
import OOP.Card;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

import java.util.*;
import java.util.stream.Collectors;

public class write_centerController {
    @FXML private Label vietnameseLabel;
    @FXML private HBox chosenHBox;   // Nơi xếp chữ đã chọn
    @FXML private HBox lettersHBox;  // Nơi chữ ban đầu
    @FXML private Label feedbackLabel; // Nhãn phản hồi
    @FXML private Button speakButton; // Nút Speak

    private List<Card> cards;
    private int currentIndex;
    private write_bottomController bottomController;


    public void setCards(List<Card> cards) {
        this.cards = cards;
        currentIndex = 0;
        showCard();
    }

    private List<Card> allCards;        // toàn bộ deck
    private int batchOffset;            // offset batch hiện tại (0, 5, 10, ...)
    private static final int BATCH_SIZE = 5;
    private boolean skipRating;
    public void initReviewSession(List<Card> allCards, int offset, boolean skipRating) {
        this.allCards = allCards;
        this.batchOffset = offset;
        this.skipRating = skipRating;
    }

    private final Map<Card,Integer> sessionRatings = new LinkedHashMap<>();

    public void setBottomController(write_bottomController ctrl) {
        this.bottomController = ctrl;
        // cập nhật progress lần đầu
        if (cards != null) bottomController.updateProgress(currentIndex + 1, cards.size());
    }

//    private double getProgress() {
//        return cards == null || cards.isEmpty()
//                ? 0
//                : (double) currentIndex / (cards.size() - 1);
//    }

    private void showCard() {
        Card card = cards.get(currentIndex);
        vietnameseLabel.setText(card.getVietnamese_text());
        chosenHBox.getChildren().clear();
        lettersHBox.getChildren().clear();
        feedbackLabel.setText(""); // Xóa thông báo cũ
        feedbackLabel.getStyleClass().removeAll("correct", "incorrect"); // Xóa kiểu cũ

//        xáo trộn chữ
        List<Character> letters = new ArrayList<>();
        for (char c : card.getEnglish_text().toCharArray()) letters.add(c);
        Collections.shuffle(letters);

        for (char c : letters) {
            Button btn = new Button(String.valueOf(c));
            btn.getStyleClass().add("letter-button"); // Áp dụng kiểu CSS

            btn.setOnAction(e -> {
                if (btn.getParent() == lettersHBox) {
                    lettersHBox.getChildren().remove(btn);
                    chosenHBox.getChildren().add(btn);
                } else {
                    chosenHBox.getChildren().remove(btn);
                    lettersHBox.getChildren().add(btn);
                    btn.getStyleClass().remove("letter-button-error"); // Xóa màu đỏ khi đưa về dưới
                }

                // Kiểm tra khi chọn đủ chữ
                if (chosenHBox.getChildren().size() == card.getEnglish_text().length()) {
                    StringBuilder sb = new StringBuilder();
                    chosenHBox.getChildren().forEach(node ->
                            sb.append(((Button)node).getText())
                    );
                    String attempt =     sb.toString();
                    if (attempt.equalsIgnoreCase(card.getEnglish_text())) {
                        feedbackLabel.setText("Chính xác! Sang từ tiếp theo");
                        feedbackLabel.getStyleClass().remove("incorrect");
                        feedbackLabel.getStyleClass().add("correct");
                        // Chờ 3 giây trước khi chuyển sang câu tiếp theo
                        PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
                        pause.setOnFinished(event -> Platform.runLater(this::nextCard));
                        pause.play();
                    } else {
                        feedbackLabel.setText("Sai rồi, thử lại nhé!");
                        feedbackLabel.getStyleClass().remove("correct");
                        feedbackLabel.getStyleClass().add("incorrect");
                        // Tô đỏ các nút sai
                        for (int i = 0; i < chosenHBox.getChildren().size(); i++) {
                            Button btnChosen   = (Button) chosenHBox.getChildren().get(i);
                            if (i < card.getEnglish_text().length() &&
                                    btnChosen.getText().charAt(0) != card.getEnglish_text().charAt(i)) {
                                btnChosen.getStyleClass().add("letter-button-error");
                            } else {
                                btnChosen.getStyleClass().remove("letter-button-error");
                            }
                        }
                    }
                } else {
                    // Xóa thông báo và màu đỏ nếu chưa chọn đủ
                    feedbackLabel.setText("");
                    feedbackLabel.getStyleClass().removeAll("correct", "incorrect");
                    chosenHBox.getChildren().forEach(node ->
                            ((Button)node).getStyleClass().remove("letter-button-error"));
                }

                if (bottomController != null) {
                    bottomController.updateProgress(currentIndex + 1, cards.size());
                }
            });

            lettersHBox.getChildren().add(btn);
        }

        if (bottomController != null) bottomController.updateProgress(currentIndex + 1, cards.size());
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
//            showCard(currentIndex);
//        } else {
//            Alert alert = new Alert(Alert.AlertType.INFORMATION);
//            alert.setTitle("Hoàn thành!");
//            alert.setHeaderText(null);
//            alert.setContentText("Bạn đã hoàn thành tất cả thẻ!");
//            alert.showAndWait();
//        }
//    }
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

    public void prevCard() {
        if (currentIndex > 0) {
            currentIndex--;
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