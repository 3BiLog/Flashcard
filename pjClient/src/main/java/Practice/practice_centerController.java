package Practice;

import FlashCard.CardRequest;
import OOP.Card;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.*;
import java.util.stream.Collectors;

public class practice_centerController {

    @FXML private Label questionLabel;
    @FXML private Button answer1;
    @FXML private Button answer2;
    @FXML private Button answer3;
    @FXML private Button answer4;
    @FXML private Button speakerButton;

    private List<Card> cards;
    private int currentIndex;
    private String selectedLanguage;
    private practice_bottomController bottomController;
    private int correctAnswerIndex;


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


    public void setCards(List<Card> cards, String selectedLanguage) {
        if (cards == null || cards.isEmpty()) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi");
                alert.setHeaderText(null);
                alert.setContentText("Danh sách thẻ không được rỗng hoặc null");
                alert.showAndWait();
            });
            throw new IllegalArgumentException("Danh sách thẻ không được rỗng hoặc null");
        }
        this.cards = cards;
        this.selectedLanguage = selectedLanguage;
        this.currentIndex = 0;

        // Log card data for debugging
        System.out.println("setCards: Card list size = " + cards.size());
        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            System.out.println("Card " + i + ": English=" + card.getEnglish_text() +
                    ", Vietnamese=" + card.getVietnamese_text());
        }
        showCard();
    }

    public void setBottomController(practice_bottomController ctrl) {
        this.bottomController = ctrl;
    }


    private void showCard() {
        if (cards == null || currentIndex >= cards.size()) {
            return;
        }

        Card currentCard = cards.get(currentIndex);
        String questionText = selectedLanguage.equals("Tiếng Anh")
                ? currentCard.getEnglish_text()
                : currentCard.getVietnamese_text();
        if (questionText == null || questionText.isEmpty()) {
            return;
        }

        // Đáp án đúng
        String correctAnswer = selectedLanguage.equals("Tiếng Anh")
                ? currentCard.getVietnamese_text()
                : currentCard.getEnglish_text();
        if (correctAnswer == null || correctAnswer.isEmpty()) {
            return;
        }

        // Tạo danh sách đáp án sai (distractors)
        List<String> distractors = new ArrayList<>();
        for (Card c : cards) {
            if (c != currentCard) {
                String text = selectedLanguage.equals("Tiếng Anh")
                        ? c.getVietnamese_text()
                        : c.getEnglish_text();
                if (text != null && !text.isEmpty() && !distractors.contains(text)) {
                    distractors.add(text);
                }
            }
        }
        Collections.shuffle(distractors);

        int totalOptions = Math.min(Math.max(cards.size(), 1), 4);

        // Gom đáp án
        List<String> answers = new ArrayList<>();
        answers.add(correctAnswer);
        for (int i = 0; i < totalOptions - 1 && i < distractors.size(); i++) {
            answers.add(distractors.get(i));
        }
        if (answers.size() < totalOptions) {
            System.err.println("showCard: Only " + answers.size() + " answers, expected " + totalOptions);
        }

        // Trộn đáp án
        Collections.shuffle(answers);
        correctAnswerIndex = answers.indexOf(correctAnswer);

        // Cập nhật UI
        Platform.runLater(() -> {
            questionLabel.setText(questionText);
            Button[] btns = {answer1, answer2, answer3, answer4};
            for (int i = 0; i < btns.length; i++) {
                if (i < totalOptions && i < answers.size()) {
                    btns[i].setVisible(true);
                    btns[i].setDisable(false);
                    btns[i].setText(answers.get(i));
                    btns[i].getStyleClass().removeAll("correct", "incorrect");
                    final int idx = i;
                    btns[i].setOnAction(evt -> checkAnswer(evt));
                } else {
                    btns[i].setVisible(false);
                    btns[i].setDisable(true);
                    btns[i].setOnAction(null);
                }
            }
            if (bottomController != null) {
                bottomController.updateProgress(currentIndex + 1, cards.size());
            }
        });
    }

    @FXML
    private void checkAnswer(javafx.event.ActionEvent event) {
        Button clicked = (Button) event.getSource();
        Button[] btns = {answer1, answer2, answer3, answer4};
        int clickedIdx = -1;
        for (int i = 0; i < btns.length; i++) {
            if (btns[i] == clicked) {
                clickedIdx = i;
                break;
            }
        }
        boolean isCorrect = (clickedIdx == correctAnswerIndex);
        Platform.runLater(() -> {
            clicked.getStyleClass().add(isCorrect ? "correct" : "incorrect");
            if (!isCorrect && correctAnswerIndex >= 0) {
                btns[correctAnswerIndex].getStyleClass().add("correct");
            }
            Alert alert = new Alert(isCorrect ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
            alert.setTitle(isCorrect ? "Đúng!" : "Sai!");
            alert.setHeaderText(null);
            alert.setContentText(isCorrect ? "Chúc mừng, bạn đã chọn đúng!" : "Đáp án chưa đúng, thử lại nhé!");
            alert.showAndWait();
            if (isCorrect) nextCard();
        });
    }

    @FXML
    private void handleSpeak() {
        if (cards != null && currentIndex < cards.size()) {
            String text = cards.get(currentIndex).getEnglish_text();
            new Thread(() -> speakText(text), "TTS-Thread").start();
        }
    }


    private void speakText(String text) {
        try {
            System.setProperty("freetts.voices",
                    "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
            VoiceManager vm = VoiceManager.getInstance();
            Voice voice = vm.getVoice("kevin");

            if (voice == null) {
                throw new IllegalStateException("Voice 'kevin' not found");
            }

            voice.allocate();

            try {
                voice.speak(text);
            } catch (Exception e) {
                e.printStackTrace();
            }

            voice.deallocate();
        } catch (Exception ex) {
            ex.printStackTrace();
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("TTS Error");
                alert.setHeaderText("Cannot speak");
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
            });
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
                    setCards(nextBatch,selectedLanguage);
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




//    private void showCard() {
//        if (cards == null || currentIndex >= cards.size()) {
//            return;
//        }
//        Card currentCard = cards.get(currentIndex);
//
//        if (selectedLanguage.equals("Tiếng Anh")) {
//            questionLabel.setText(currentCard.getEnglish_text());
//        } else {
//            questionLabel.setText(currentCard.getVietnamese_text());
//        }
//
//        List<String> answers = new ArrayList<>();
//        String correctAnswer = selectedLanguage.equals("Tiếng Anh") ? currentCard.getVietnamese_text() : currentCard.getEnglish_text();
//        answers.add(correctAnswer);
//
//        int totalCards = cards.size();
//        int totalOptions = Math.min(Math.max(totalCards,1),4);
//
//        Random random = new Random();
//        while(answers.size() < totalOptions){
//            int idx = random.nextInt(totalCards);
//            Card randomCard = cards.get(idx);
//            String text = selectedLanguage.equals("Tiếng Anh") ? randomCard.getVietnamese_text() : randomCard.getEnglish_text();
//            if(!answers.contains(text)){
//                answers.add(text);
//            }
//        }
////        while (answers.size() < 4) {
////            int randomIndex = random.nextInt(cards.size());
////            Card randomCard = cards.get(randomIndex);
////            String randomAnswer = selectedLanguage.equals("Tiếng Anh") ? randomCard.getVietnamese_text() : randomCard.getEnglish_text();
////            if (!answers.contains(randomAnswer) && !randomAnswer.equals(correctAnswer)) {
////                answers.add(randomAnswer);
////            }
////        }
//
//        Collections.shuffle(answers);
//
//        Button[] answerButtons = {answer1, answer2, answer3, answer4};
//        correctAnswerIndex =-1;
//        for (int i = 0; i < answerButtons.length; i++) {
//            if(i<totalOptions){
//                answerButtons[i].setVisible(true);
//                answerButtons[i].setDisable(false);
//                answerButtons[i].setText(answers.get(i));
//                answerButtons[i].getStyleClass().removeAll("correct","incorrect");
//                if(answers.get(i).equals(correctAnswer)){
//                    correctAnswerIndex = i;
//                }
//            }else {
//                answerButtons[i].setVisible(false);
//                answerButtons[i].setDisable(true);
//            }
//        }
//
//
//        if (bottomController != null) {
//            bottomController.updateProgress(currentIndex + 1, cards.size());
//        }
//    }



//    @FXML
//    private void checkAnswer(javafx.event.ActionEvent event) {
//        Button clickedButton = (Button) event.getSource();
//        Button[] answerButtons = {answer1, answer2, answer3, answer4};
//
//        boolean isCorrect = false;
//        for (int i = 0; i < answerButtons.length; i++) {
//            if (clickedButton == answerButtons[i]) {
//                if (i == correctAnswerIndex) {
//                    isCorrect = true;
//                    clickedButton.getStyleClass().add("correct");
//                } else {
//                    clickedButton.getStyleClass().add("incorrect");
//                    answerButtons[correctAnswerIndex].getStyleClass().add("correct");
//                }
//            }
//        }
//
//        Alert alert = new Alert(isCorrect ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
//        alert.setTitle(isCorrect ? "Đúng!" : "Sai!");
//        alert.setHeaderText(null);
//        alert.setContentText(isCorrect ? "Chúc mừng, bạn đã chọn đúng!" : "Đáp án chưa đúng, thử lại nhé!");
//        alert.getDialogPane().getStylesheets().add(getClass().getResource("/Practice/alert.css").toExternalForm());
//        alert.showAndWait();
//
//        if (isCorrect) {
//            nextCard();
//        }
//    }
