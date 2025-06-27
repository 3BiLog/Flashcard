package Read;

import FlashCard.CardRequest;
import FlashCard.trangChuController;
import OOP.Card;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;
import org.json.JSONObject;
import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.*;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class Read_centerController {

    @FXML private Label topWordLabel;
    @FXML private Button micButton;
    @FXML private Label feedbackLabel;

    private List<Card> cards;
    private int currentIndex;
    private Read_bottomController bottomController;
    private trangChuController parentController;
    private Model model;
    private Recognizer recognizer;
    private TargetDataLine microphone;
    private boolean isRecording = false;

    public void setCards(List<Card> cards) {
        this.cards = cards;
        this.currentIndex = 0;
        try {
            closeResources();
            initializeVosk();
            showCard();
        } catch (Exception e) {
            debugError("setCards", e);
            showError("Không thể khởi tạo microphone. Vui lòng kiểm tra thiết bị âm thanh hoặc thử lại.");
        }
    }


    private int batchOffset;            // offset batch hiện tại (0, 5, 10, ...)
    private static final int BATCH_SIZE = 5;
    private boolean skipRating;
    public void initReviewSession(List<Card> allCards, int offset, boolean skipRating) {
        this.cards = allCards;
        this.batchOffset = offset;
        this.skipRating = skipRating;
    }

    private final Map<Card,Integer> sessionRatings = new LinkedHashMap<>();

    public void setBottomController(Read_bottomController ctrl) {
        this.bottomController = ctrl;
        if (bottomController != null) {
            bottomController.updateProgress(currentIndex + 1, cards.size());
        }
    }

    public void setParentController(trangChuController parent) {
        this.parentController = parent;
    }

    private void initializeVosk() throws Exception {
        LibVosk.setLogLevel(LogLevel.WARNINGS);
        String modelPath = "model\\vosk-model-small-en-us-0.15";
        File modelDir = new File(modelPath);
        if (!modelDir.exists() || !modelDir.isDirectory()) {
            throw new IllegalStateException("Thư mục mô hình không tồn tại: " + modelPath);
        }

        model = new Model(modelPath);

        // Chuẩn hóa sang 16kHz mono
        AudioFormat targetFormat = new AudioFormat(16000.0f, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, targetFormat);
        if (!AudioSystem.isLineSupported(info)) {
            throw new UnsupportedOperationException("Thiết bị không hỗ trợ định dạng 16kHz mono!");
        }

        microphone = (TargetDataLine) AudioSystem.getLine(info);
        microphone.open(targetFormat);
        recognizer = new Recognizer(model, targetFormat.getSampleRate());
        System.out.println("Microphone khởi tạo: " + targetFormat);
    }

    private void showCard() {
        if (cards == null || currentIndex >= cards.size()) {
            finishSession();
            return;
        }
        String word = cards.get(currentIndex).getEnglish_text();
        topWordLabel.setText(word);
        feedbackLabel.setText("");
        feedbackLabel.getStyleClass().removeAll("correct", "error");
        if (bottomController != null) {
            bottomController.updateProgress(currentIndex + 1, cards.size());
        }
    }

    @FXML
    private void handleMic() {
        if (!isRecording) startRecording(); else stopRecording();
    }

    private void startRecording() {
        if (microphone == null || recognizer == null) {
            showError("Microphone chưa được khởi tạo!");
            return;
        }

        isRecording = true;
        micButton.setText("Stop");

        new Thread(() -> {
            try {
                microphone.start();
                byte[] buffer = new byte[4096];
                long startTime = System.currentTimeMillis();

                // Ghi âm full 3 giây
                while (isRecording && System.currentTimeMillis() - startTime < 3000) {
                    microphone.read(buffer, 0, buffer.length);
                    recognizer.acceptWaveForm(buffer, buffer.length);
                }

                microphone.stop();
                microphone.flush();

                // Lấy kết quả cuối cùng
                String resultJson = recognizer.getFinalResult();
                String spoken = extractText(resultJson);

                Platform.runLater(() -> {
                    micButton.setText("Mic");
                    isRecording = false;
                    if (!spoken.isEmpty()) checkRecognition(spoken);
                    else showFeedbackError();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    debugError("startRecording", e);
                    micButton.setText("Mic");
                    isRecording = false;
                });
            }
        }).start();
    }

    private void stopRecording() {
        isRecording = false;
    }

    /**
     * Trích chính xác trường "text" từ JSON nhờ org.json
     */
    private String extractText(String json) {
        if (json == null || json.isEmpty()) return "";
        try {
            JSONObject o = new JSONObject(json);
            return o.optString("text", "").trim();
        } catch (Exception e) {
            // Fallback loại bỏ ký tự không phải chữ
            return json.replaceAll("[^a-zA-Z ]", "").trim();
        }
    }

    private void checkRecognition(String recognizedText) {
        String expected = cards.get(currentIndex).getEnglish_text().toLowerCase().trim();
        System.out.println("Bạn đã nói: " + recognizedText);
        feedbackLabel.getStyleClass().removeAll("correct", "error");

        if (recognizedText.toLowerCase().equals(expected)) {
            feedbackLabel.setText("Chính xác! Sang từ tiếp theo");
            feedbackLabel.getStyleClass().add("correct");
            PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
            pause.setOnFinished(event -> Platform.runLater(this::nextCard));
            pause.play();
        } else {
            showFeedbackError();
        }
    }

    private void showFeedbackError() {
        feedbackLabel.setText("Sai rồi, thử lại nhé!");
        feedbackLabel.getStyleClass().add("error");
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Lỗi"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    private void debugError(String loc, Exception e) {
        String m = "[" + loc + "] " + e.getMessage();
        System.err.println(m);
        e.printStackTrace();
        showError(m);
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
                if (nextOffset < cards.size()) {
                    // có batch mới → load tiếp
                    batchOffset = nextOffset;
                    List<Card> nextBatch = cards.stream()
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

    public void prevCard() {
        if (currentIndex > 0) {
            currentIndex--;
            showCard();
        }
    }

    private void finishSession() {
        if (parentController != null) {
            parentController.restoreHome();
            parentController.loadDecksFromServer();
        }
        closeResources();
    }

    public void closeResources() {
        try {
            if (microphone != null) { if (microphone.isRunning()) microphone.stop(); if (microphone.isOpen()) microphone.close(); microphone = null; }
            if (recognizer != null) { recognizer.close(); recognizer = null; }
            if (model != null) { model.close(); model = null; }
        } catch (Exception ignored) {}
    }
}
