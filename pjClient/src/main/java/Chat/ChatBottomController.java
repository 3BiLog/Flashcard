package Chat;

import OOP.Message;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;

public class ChatBottomController {
    @FXML private TextArea messageInput;
    @FXML private Button sendButton;
    @FXML private Button iconButton;
    @FXML private Button imageButton;
    @FXML private Button fileButton;
    @FXML private Button encryptedFileButton;
    @FXML private Button recordButton;
    @FXML
    private HBox chatBottomPane;

    private ChatConnectionManager connectionManager;
    private int userId;
    private String username;
    private AudioRecorder audioRecorder;

    public void setUserInfo(int userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public void setConnectionManager(ChatConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @FXML
    private void initialize() {
        sendButton.setOnAction(event -> sendTextMessage());
        iconButton.setOnAction(event -> showIconPicker());
        imageButton.setOnAction(event -> sendImageFile());
        fileButton.setOnAction(event -> sendGenericFile());
        encryptedFileButton.setOnAction(event -> sendEncryptedFile());
        recordButton.setOnAction(event -> toggleRecording());
        audioRecorder = new AudioRecorder();

        Platform.runLater(() -> {
            if (chatBottomPane.getScene() != null && chatBottomPane.getScene().getWindow() != null) {
                Stage stage = (Stage) chatBottomPane.getScene().getWindow();
                double leftPadding = stage.getWidth() * 0.15;
                chatBottomPane.setPadding(new Insets(10, 10, 10, leftPadding));
            }
        });

        chatBottomPane.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                Stage stage = (Stage) newScene.getWindow();
                stage.widthProperty().addListener((obs, oldWidth, newWidth) -> {
                    double leftPadding = newWidth.doubleValue() * 0.15;
                    chatBottomPane.setPadding(new Insets(10, 10, 10, leftPadding));
                });
                double leftPadding = stage.getWidth() * 0.15;
                chatBottomPane.setPadding(new Insets(10, 10, 10, leftPadding));
            }
        });
    }

    private void sendTextMessage() {
        String content = messageInput.getText().trim();
        if (!content.isEmpty()) {
            Message message = new Message("SEND_MESSAGE", userId, username, content);
            connectionManager.sendMessage(message);
            messageInput.clear();
        }
    }

    private void showIconPicker() {
        Stage iconStage = new Stage();
        iconStage.setTitle("Chọn Icon");
        VBox iconBox = new VBox(10);
        iconBox.setStyle("-fx-padding: 10; -fx-background-color: #FFFFFF;");

        String[] emojis = {"😊", "👍", "❤️", "😂", "😢", "😎", "🚀", "🌟","🔔"};
        HBox emojiRow = new HBox(5);
        for (String emoji : emojis) {
            Button emojiButton = new Button(emoji);
            emojiButton.setStyle("-fx-font-size: 20px; -fx-background-color: transparent;");
            emojiButton.setOnAction(event -> {
                messageInput.appendText(emoji);
            });
            emojiRow.getChildren().add(emojiButton);
            if (emojiRow.getChildren().size() >= 4) {
                iconBox.getChildren().add(emojiRow);
                emojiRow = new HBox(5);
            }
        }
        if (!emojiRow.getChildren().isEmpty()) {
            iconBox.getChildren().add(emojiRow);
        }

        iconStage.setScene(new javafx.scene.Scene(iconBox, 250, 200));
        iconStage.show();
    }

    private void sendImageFile() {
        File file = chooseFile(new String[]{"*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"});
        if (file != null) {
            try {
                byte[] fileData = Files.readAllBytes(file.toPath());
                Message message = new Message("IMAGE_MESSAGE", userId, username, fileData, file.getName(), "image");
                connectionManager.sendMessage(message);
            } catch (Exception e) {
                showErrorAlert("Lỗi khi gửi ảnh: " + e.getMessage());
            }
        }
    }

    private void sendGenericFile() {
        File file = chooseFile(new String[]{"*.mp4", "*.mkv", "*.avi", "*.mov", "*.mp3", "*.wav", "*.aac", "*.flac", "*.*"});
        if (file != null) {
            try {
                byte[] fileData = Files.readAllBytes(file.toPath());
                String fileType = determineFileType(file.getName());
                System.out.println("Sending file: name=" + file.getName() + ", type=" + fileType);
                Message message = new Message("FILE_MESSAGE", userId, username, fileData, file.getName(), fileType);
                connectionManager.sendMessage(message);
            } catch (Exception e) {
                showErrorAlert("Lỗi khi gửi file: " + e.getMessage());
            }
        }
    }

    private void sendEncryptedFile() {
        File file = chooseFile(new String[]{"*.*"});
        if (file == null) return;

        // Yêu cầu người dùng nhập mật khẩu
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nhập mật khẩu");
        dialog.setHeaderText("Nhập mật khẩu để mã hóa file");
        dialog.setContentText("Mật khẩu:");
        String password = dialog.showAndWait().orElse(null);
        if (password == null || password.trim().isEmpty()) {
            showErrorAlert("Mật khẩu không được để trống!");
            return;
        }

        try {
            byte[] fileData = Files.readAllBytes(file.toPath());
            byte[] encryptedData = EncryptionUtil.encrypt(fileData, password);
            String fileType = determineFileType(file.getName());
            System.out.println("Sending encrypted file: name=" + file.getName() + ", type=" + fileType);
            Message message = new Message("ENCRYPTED_FILE_MESSAGE", userId, username, encryptedData, file.getName(), fileType);
            connectionManager.sendMessage(message);
        } catch (Exception e) {
            showErrorAlert("Lỗi khi gửi file mã hóa: " + e.getMessage());
            e.printStackTrace(); // Thêm để debug
        }
    }

    private File chooseFile(String[] extensions) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn file");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Files", extensions)
        );
        return fileChooser.showOpenDialog(messageInput.getScene().getWindow());
    }

    private String determineFileType(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            System.err.println("Invalid file name: " + fileName);
            return "other";
        }
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "png":
            case "jpg":
            case "jpeg":
            case "gif":
            case "bmp":
                return "image";
            case "mp4":
            case "mkv":
            case "avi":
            case "mov":
                return "video";
            case "mp3":
            case "wav":
            case "aac":
            case "flac":
                return "audio";
            case "pdf":
            case "txt":
            case "doc":
            case "docx":
                return "other";
            default:
                System.err.println("Unknown file extension: " + extension);
                return "other";
        }
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, javafx.scene.control.ButtonType.OK);
        alert.showAndWait();
    }

    private void toggleRecording() {
        if (!audioRecorder.isRecording()) {
            try {
                audioRecorder.startRecording();
                Image image = new Image(getClass().getResourceAsStream("/dismic.png"));

                // Tạo ImageView và chỉnh kích thước (tuỳ chọn)
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(40);    // Chiều rộng ảnh
                imageView.setFitHeight(40);   // Chiều cao ảnh
                recordButton.setGraphic(imageView);
            } catch (Exception e) {
                showErrorAlert("Lỗi khi bắt đầu ghi âm: " + e.getMessage());
            }
        } else {
            try {
                File audioFile = audioRecorder.stopRecording();
                Image image = new Image(getClass().getResourceAsStream("/mic.png"));
                // Tạo ImageView và chỉnh kích thước (tuỳ chọn)
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(40);    // Chiều rộng ảnh
                imageView.setFitHeight(40);   // Chiều cao ảnh
                recordButton.setGraphic(imageView);
                if (audioFile != null) {
                    sendAudioFile(audioFile);
                }
            } catch (Exception e) {
                showErrorAlert("Lỗi khi dừng ghi âm: " + e.getMessage());
            }
        }
    }

    private void sendAudioFile(File audioFile) {
        try {
            byte[] fileData = Files.readAllBytes(audioFile.toPath());
            String fileName = "recording_" + LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".wav";
            Message message = new Message("AUDIO_RECORD_MESSAGE", userId, username, fileData, fileName, "audio");
            connectionManager.sendMessage(message);
            System.out.println("Sent audio file: " + fileName);
        } catch (Exception e) {
            showErrorAlert("Lỗi khi gửi file âm thanh: " + e.getMessage());
        }
    }

    private class AudioRecorder {
        private AudioFormat audioFormat;
        private TargetDataLine line;
        private File outputFile;
        private boolean isRecording;
        private static final int MAX_RECORDING_SECONDS = 60;

        public AudioRecorder() {
            audioFormat = new AudioFormat(48000, 16, 2, true, false);
            isRecording = false;
        }

        public boolean isRecording() {
            return isRecording;
        }

        public void startRecording() throws Exception {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
            if (!AudioSystem.isLineSupported(info)) {
                throw new Exception("Không hỗ trợ ghi âm trên thiết bị này. Vui lòng kiểm tra microphone.");
            }

            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(audioFormat);
            line.start();
            isRecording = true;

            outputFile = File.createTempFile("recording", ".wav");
            System.out.println("Bắt đầu ghi âm vào file: " + outputFile.getAbsolutePath());

            new Thread(() -> {
                try (AudioInputStream ais = new AudioInputStream(line)) {
                    long startTime = System.currentTimeMillis();
                    AudioSystem.write(ais, AudioFileFormat.Type.WAVE, outputFile);

                    while (isRecording && (System.currentTimeMillis() - startTime) < MAX_RECORDING_SECONDS * 1000) {
                        Thread.sleep(100);
                    }

                    if (isRecording) {
                        stopRecording();
                        Platform.runLater(() -> {
                            recordButton.setText("Ghi âm");
                            recordButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px;");
                            showErrorAlert("Đã đạt thời gian ghi âm tối đa (" + MAX_RECORDING_SECONDS + " giây).");
                            sendAudioFile(outputFile);
                        });
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi khi ghi âm: " + e.getMessage());
                    Platform.runLater(() -> showErrorAlert("Lỗi khi ghi âm: " + e.getMessage()));
                }
            }).start();
        }

        public File stopRecording() {
            if (line != null && isRecording) {
                isRecording = false;
                line.stop();
                line.close();
                System.out.println("Đã dừng ghi âm, file lưu tại: " + outputFile.getAbsolutePath());
                return outputFile;
            }
            return null;
        }
    }
}