package Chat;

import FlashCard.CardRequest;
import FlashCard.botheRequest;
import OOP.Card;
import OOP.Message;
import OOP.boThe;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaView;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ChatCenterController {
    @FXML
    private ScrollPane chatScrollPane;
    @FXML private VBox messageContainer;

    private int currentUserId;
    private static final double MAX_MESSAGE_WIDTH = 500;
    private List<Message> messageHistory = new ArrayList<>();
    private ChatConnectionManager connectionManager;

    public void initialize() {
        messageContainer.heightProperty().addListener((obs, old, newVal) -> {
            chatScrollPane.setVvalue(1.0);
        });
    }

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
        System.out.println("Set currentUserId: " + userId);
    }

    public void setConnectionManager(ChatConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        System.out.println("ChatConnectionManager set in ChatCenterController: " + (connectionManager != null));
    }

    public void addMessage(Message message) {
        Platform.runLater(() -> {
            System.out.println("Adding message: command=" + message.getCommand() +
                    ", userId=" + message.getUserId() +
                    ", username=" + message.getUsername() +
                    ", content=" + message.getContent() +
                    ", fileName=" + message.getFileName() +
                    ", fileType=" + message.getFileType() +
                    ", messageId=" + message.getMessageId());
            if (!"DELETE_MESSAGE".equals(message.getCommand())) {
                messageHistory.add(message);
            }
            addMessageToUI(message);
        });
    }

    public void restoreMessages() {
        Platform.runLater(() -> {
            messageContainer.getChildren().clear();
            for (Message message : messageHistory) {
                System.out.println("Restoring message: command=" + message.getCommand() +
                        ", userId=" + message.getUserId() +
                        ", username=" + message.getUsername() +
                        ", content=" + message.getContent() +
                        ", fileName=" + message.getFileName() +
                        ", fileType=" + message.getFileType() +
                        ", messageId=" + message.getMessageId());
                addMessageToUI(message);
            }
            chatScrollPane.setVvalue(1.0);
        });
    }

    private void addMessageToUI(Message message) {
        if ("DELETE_MESSAGE".equals(message.getCommand())) {
            handleDeleteMessage(message);
            return;
        }

        VBox messageWrapper = createMessageWrapper(message);
        VBox messageContent = createMessageContent(message);
        Label usernameLabel = createUsernameLabel(message);
        Label timestampLabel = createTimestampLabel(message);

        try {
            switch (message.getCommand()) {
                case "SHARE_DECK":
                    addSharedDeckContent(message, messageContent);
                    break;
                case "ICON_MESSAGE":
                    addIconContent(message, messageContent);
                    break;
                case "IMAGE_MESSAGE":
                    addImageContent(message, messageContent);
                    break;
                case "FILE_MESSAGE":
                case "AUDIO_RECORD_MESSAGE":
                    addFileContent(message, messageContent);
                    break;
                case "ENCRYPTED_FILE_MESSAGE":
                    addEncryptedFileContent(message, messageContent);
                    break;
                case "SEND_MESSAGE":
                    addTextContent(message, messageContent);
                    break;
                case "BROADCAST_MESSAGE":
                    addBroadcastContent(message, messageContent);
                    break;
                default:
                    addErrorContent(messageContent, "Lệnh không hỗ trợ: " + message.getCommand());
            }
        } catch (Exception e) {
            System.err.println("Error displaying message: " + e.getMessage());
            addErrorContent(messageContent, "Lỗi hiển thị tin nhắn: " + e.getMessage());
        }

        HBox contentWithDelete = new HBox(5);
        contentWithDelete.getChildren().add(messageContent);
        contentWithDelete.setAlignment(message.getUserId() == currentUserId ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        if (message.getUserId() == currentUserId && !message.getCommand().equals("BROADCAST_MESSAGE") ) {
            Image trashIcon = new Image(getClass().getResourceAsStream("/delete.png"));
            ImageView iconView = new ImageView(trashIcon);
            iconView.setFitWidth(17);
            iconView.setFitHeight(17);
            iconView.setPreserveRatio(true);
            Button deleteButton = new Button();
            deleteButton.setGraphic(iconView);
            deleteButton.setStyle("-fx-background-color: transparent");
            deleteButton.setOnAction(e -> deleteMessage(message));
            contentWithDelete.getChildren().add(deleteButton);
            contentWithDelete.setAlignment(Pos.CENTER_RIGHT);
        }

        messageWrapper.getChildren().addAll(usernameLabel, contentWithDelete, timestampLabel);
        messageContainer.getChildren().add(messageWrapper);
    }

    private void handleDeleteMessage(Message message) {
        System.out.println("Handling DELETE_MESSAGE: messageId=" + message.getMessageId() +
                ", userId=" + message.getUserId() +
                ", username=" + message.getUsername() +
                ", currentUserId=" + currentUserId);

        boolean found = false;
        for (int i = 0; i < messageHistory.size(); i++) {
            if (messageHistory.get(i).getMessageId().equals(message.getMessageId())) {
                int origUserId = messageHistory.get(i).getUserId();
                String origName = messageHistory.get(i).getUsername();
                Message deletedMessage = new Message(
                        "SEND_MESSAGE",
                        origUserId,
                        origName,
                        "Tin nhắn đã bị thu hồi"
                );
                deletedMessage.setMessageId(message.getMessageId());
                deletedMessage.setTimestamp(LocalDateTime.now());
                messageHistory.set(i, deletedMessage);
                found = true;
                break;
            }
        }

        if (found) {
            restoreMessages();
        } else {
            System.out.println("Message not found for deletion: messageId=" + message.getMessageId());
        }
    }

    private void deleteMessage(Message message) {
        if (connectionManager == null) {
            System.err.println("Error: connectionManager is null in deleteMessage");
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR,
                        "Không thể xóa tin nhắn: Mất kết nối với server.",
                        ButtonType.OK);
                alert.showAndWait();
            });
            return;
        }
        Message deleteRequest = new Message("DELETE_MESSAGE", message.getMessageId(), currentUserId, message.getUsername());
        System.out.println("Sending DELETE_MESSAGE: messageId=" + message.getMessageId() +
                ", userId=" + currentUserId +
                ", username=" + message.getUsername());
        connectionManager.sendMessage(deleteRequest);
    }

    private void addSharedDeckContent(Message message, VBox messageContent) {
        if (message.getContent() == null) {
            addErrorContent(messageContent, "Dữ liệu bộ thẻ không hợp lệ");
            return;
        }

        String[] lines = message.getContent().split("\n");
        if (lines.length < 1 || !lines[0].startsWith("DECK|")) {
            addErrorContent(messageContent, "Định dạng bộ thẻ không hợp lệ");
            System.out.println("Invalid deck data: " + message.getContent());
            return;
        }

        String[] deckParts = lines[0].split("\\|");
        if (deckParts.length < 3) {
            addErrorContent(messageContent, "Dữ liệu bộ thẻ không đầy đủ");
            System.out.println("Deck parts insufficient: " + String.join(",", deckParts));
            return;
        }

        String deckName;
        int declaredCardCount;
        try {
            deckName = deckParts[1];
            declaredCardCount = Integer.parseInt(deckParts[2]);
        } catch (NumberFormatException e) {
            addErrorContent(messageContent, "Số lượng thẻ không hợp lệ");
            System.out.println("Invalid card count: " + deckParts[2]);
            return;
        }

        int actualCardCount = 0;
        for (int i = 1; i < lines.length; i++) {
            if (lines[i].startsWith("CARD|")) {
                actualCardCount++;
            }
        }

        if (actualCardCount != declaredCardCount) {
            System.out.println("Warning: Declared card count (" + declaredCardCount + ") does not match actual (" + actualCardCount + ")");
        }

        Label deckLabel = new Label("Bộ thẻ: " + deckName + " (" + actualCardCount + " thẻ)");
        deckLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Button saveButton = new Button("Lưu bộ thẻ");
        saveButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5px 10px;");

        saveButton.setOnAction(e -> saveSharedDeck(message.getContent()));

        messageContent.getChildren().addAll(deckLabel, saveButton);
    }

    private void saveSharedDeck(String deckData) {
        try {
            String[] lines = deckData.split("\n");
            if (lines.length < 1 || !lines[0].startsWith("DECK|")) {
                showErrorAlert("Dữ liệu bộ thẻ không hợp lệ");
                System.out.println("Invalid deck data in saveSharedDeck: " + deckData);
                return;
            }

            String[] deckParts = lines[0].split("\\|");
            if (deckParts.length < 3) {
                showErrorAlert("Dữ liệu bộ thẻ không đầy đủ");
                System.out.println("Deck parts insufficient in saveSharedDeck: " + String.join(",", deckParts));
                return;
            }

            String deckName = deckParts[1];
            int declaredCardCount;
            try {
                declaredCardCount = Integer.parseInt(deckParts[2]);
            } catch (NumberFormatException e) {
                showErrorAlert("Số lượng thẻ không hợp lệ");
                System.out.println("Invalid card count in saveSharedDeck: " + deckParts[2]);
                return;
            }

            int actualCardCount = 0;
            for (int i = 1; i < lines.length; i++) {
                if (lines[i].startsWith("CARD|")) {
                    actualCardCount++;
                }
            }

            boThe newDeck = botheRequest.addDeck(currentUserId, deckName, actualCardCount);
            if (newDeck == null) {
                showErrorAlert("Không thể tạo bộ thẻ mới");
                System.out.println("Failed to create new deck: " + deckName);
                return;
            }
            int deckId = newDeck.getDeckId();

            List<Card> cardsToUpdate = new ArrayList<>();
            int addedCards = 0;
            for (int i = 1; i < lines.length; i++) {
                if (lines[i].startsWith("CARD|")) {
                    String[] cardParts = lines[i].split("\\|");
                    if (cardParts.length >= 3) {
                        String englishText = cardParts[1];
                        String vietnameseText = cardParts[2];
                        Card card = CardRequest.addCard(deckId, englishText, vietnameseText);
                        if (card != null) {
                            card.setMemoryLevel(0);
                            card.setRepetitionCount(0);
                            card.setIntervalDays(0);
                            card.setEaseFactor(2.5);
                            card.setLastReviewDate(LocalDateTime.now());
                            card.setNextReviewDate(LocalDateTime.now());
                            cardsToUpdate.add(card);
                            addedCards++;
                        }
                    }
                }
            }

            boolean updateSuccess = CardRequest.bulkUpdateCards(cardsToUpdate);
            String alertMessage = "Đã lưu bộ thẻ: " + deckName + " với " + addedCards + " thẻ";
            if (!updateSuccess) {
                alertMessage += "\nKhông thể cập nhật thuộc tính cho một số thẻ.";
                System.out.println("Failed to bulk update cards for deck: " + deckName);
            }
            if (addedCards != actualCardCount) {
                System.out.println("Warning: Added " + addedCards + " cards, expected " + actualCardCount);
            }

            String finalMessage = alertMessage;
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, finalMessage, ButtonType.OK);
                alert.showAndWait();
            });
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> showErrorAlert("Lỗi khi lưu bộ thẻ: " + e.getMessage()));
        }
    }


    private VBox createMessageWrapper(Message message) {
        VBox messageWrapper = new VBox();
        messageWrapper.setSpacing(5);
        messageWrapper.setStyle("-fx-font-size: 20px;");
        messageWrapper.setPadding(new Insets(10));
        boolean isCurrentUser = message.getUserId() == currentUserId;
        System.out.println("Creating message wrapper: message userId=" + message.getUserId() +
                ", currentUserId=" + currentUserId +
                ", isCurrentUser=" + isCurrentUser +
                ", alignment=" + (isCurrentUser ? "RIGHT" : "LEFT"));
        messageWrapper.setAlignment(isCurrentUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        return messageWrapper;
    }

    private VBox createMessageContent(Message message) {
        VBox messageContent = new VBox();
        messageContent.setMaxWidth(MAX_MESSAGE_WIDTH);
        boolean isCurrentUser = message.getUserId() == currentUserId;
        messageContent.setStyle(isCurrentUser
                ? "-fx-background-color: #4CAF50; -fx-background-radius: 10; -fx-padding: 10;"
                : "-fx-background-color: #E0E0E0; -fx-background-radius: 10; -fx-padding: 10;");
        return messageContent;
    }

    private Label createUsernameLabel(Message message) {
        Label usernameLabel = new Label(message.getUsername() != null ? message.getUsername() : "Unknown");
        usernameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
        usernameLabel.setAlignment(message.getUserId() == currentUserId ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        usernameLabel.setMaxWidth(MAX_MESSAGE_WIDTH);
        return usernameLabel;
    }

    private Label createTimestampLabel(Message message) {
        Label timestampLabel = new Label(message.getTimestamp().format(
                DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")));
        timestampLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
        return timestampLabel;
    }

    private void addIconContent(Message message, VBox messageContent) {
        if (message.getContent() != null) {
            Text iconText = new Text(message.getContent());
            iconText.setStyle("-fx-font-size: 20px;");
            messageContent.getChildren().add(iconText);
        }
    }

    private void addImageContent(Message message, VBox messageContent) {
        if (message.getFileData() != null) {
            ImageView imageView = new ImageView(new Image(new ByteArrayInputStream(message.getFileData())));
            imageView.setFitWidth(350);
            imageView.setFitHeight(350);
            imageView.setPreserveRatio(true);
            imageView.setOnMouseClicked(event -> saveFile(message.getFileData(), message.getFileName()));
            messageContent.getChildren().add(imageView);
        }
    }

    private void addFileContent(Message message, VBox messageContent) {
        if (message.getFileData() == null || message.getFileName() == null) {
            addErrorContent(messageContent, "File không hợp lệ: thiếu dữ liệu hoặc tên file");
            return;
        }
        System.out.println("Processing file: name=" + message.getFileName() + ", type=" + message.getFileType());
        if ("video".equals(message.getFileType())) {
            addVideoContent(message, messageContent);
        } else if ("audio".equals(message.getFileType())) {
            addAudioContent(message, messageContent);
        } else {
            addGenericFileContent(message, messageContent);
        }
    }

    private void addEncryptedFileContent(Message message, VBox messageContent) {
        if (message.getFileData() == null || message.getFileName() == null) {
            addErrorContent(messageContent, "File mã hóa không hợp lệ: thiếu dữ liệu hoặc tên file");
            return;
        }
        Hyperlink fileLink = new Hyperlink("File mã hóa: " + message.getFileName());
        fileLink.setStyle("-fx-font-size: 15px; -fx-text-fill: blue");
        fileLink.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Nhập mật khẩu");
            dialog.setHeaderText("Nhập mật khẩu để giải mã file");
            dialog.setContentText("Mật khẩu:");
            String password = dialog.showAndWait().orElse(null);
            if (password != null && !password.trim().isEmpty()) {
                try {
                    byte[] decryptedData = EncryptionUtil.decrypt(message.getFileData(), password);
                    saveFile(decryptedData, message.getFileName());
                } catch (Exception ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR,
                            "Giải mã thất bại: Mật khẩu không đúng hoặc lỗi: " + ex.getMessage(),
                            ButtonType.OK);
                    alert.showAndWait();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR,
                        "Mật khẩu không được để trống!",
                        ButtonType.OK);
                alert.showAndWait();
            }
        });
        messageContent.getChildren().add(fileLink);
    }

    private void addVideoContent(Message message, VBox messageContent) {
        try {
            File tempFile = File.createTempFile("video", message.getFileName().substring(message.getFileName().lastIndexOf(".")));
            Files.write(tempFile.toPath(), message.getFileData());
            Media media = new Media(tempFile.toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            MediaView mediaView = new MediaView(mediaPlayer);
            mediaView.setFitWidth(350);
            mediaView.setPreserveRatio(true);

            HBox buttonBox = new HBox(5);
            buttonBox.setAlignment(Pos.CENTER);

            Button playButton = new Button("Phát");
            playButton.setOnAction(e -> {
                mediaPlayer.seek(Duration.ZERO);
                mediaPlayer.play();
            });

            Button toggleButton = new Button("Dừng");
            toggleButton.setOnAction(e -> {
                if (mediaPlayer.getStatus() == Status.PLAYING) {
                    mediaPlayer.pause();
                } else {
                    mediaPlayer.play();
                }
            });

            Button downloadButton = new Button("Tải xuống");
            downloadButton.setOnAction(e -> saveFile(message.getFileData(), message.getFileName()));

            buttonBox.getChildren().addAll(playButton, toggleButton, downloadButton);
            messageContent.getChildren().addAll(mediaView, buttonBox);
            System.out.println("Video UI added for: " + message.getFileName());
        } catch (Exception e) {
            System.err.println("Error playing video: " + e.getMessage());
            addErrorContent(messageContent, "Không thể phát video: " + e.getMessage());
        }
    }

    private void addAudioContent(Message message, VBox messageContent) {
        try {
            String extension = message.getFileName().substring(message.getFileName().lastIndexOf(".") + 1).toLowerCase();
            if (!extension.equals("mp3") && !extension.equals("wav") && !extension.equals("aac") && !extension.equals("flac")) {
                throw new IllegalArgumentException("Định dạng âm thanh không được hỗ trợ: " + extension);
            }

            File tempFile = File.createTempFile("audio", "." + extension);
            Files.write(tempFile.toPath(), message.getFileData());
            Media media = new Media(tempFile.toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(media);

            HBox buttonBox = new HBox(5);
            buttonBox.setAlignment(Pos.CENTER);

            Button playButton = new Button("Phát");
            playButton.setOnAction(e -> {
                mediaPlayer.seek(Duration.ZERO);
                mediaPlayer.play();
            });

            Button toggleButton = new Button("Dừng");
            toggleButton.setOnAction(e -> {
                if (mediaPlayer.getStatus() == Status.PLAYING) {
                    mediaPlayer.pause();
                } else {
                    mediaPlayer.play();
                }
            });

            Button downloadButton = new Button("Tải xuống");
            downloadButton.setOnAction(e -> saveFile(message.getFileData(), message.getFileName()));

            buttonBox.getChildren().addAll(playButton, toggleButton, downloadButton);

            Label fileLabel = new Label("Âm thanh: " + message.getFileName());
            fileLabel.setStyle("-fx-font-size: 15px;");

            messageContent.getChildren().addAll(fileLabel, buttonBox);
            System.out.println("Audio UI added for: " + message.getFileName());
        } catch (Exception e) {
            System.err.println("Error playing audio: " + e.getMessage());
            Hyperlink fileLink = new Hyperlink("Tải xuống âm thanh: " + message.getFileName());
            fileLink.setStyle("-fx-font-size: 15px; -fx-text-fill: blue");
            fileLink.setOnAction(ev -> saveFile(message.getFileData(), message.getFileName()));
            messageContent.getChildren().add(fileLink);
            System.out.println("Fallback to Hyperlink for: " + message.getFileName());
        }
    }

    private void addGenericFileContent(Message message, VBox messageContent) {
        Hyperlink fileLink = new Hyperlink(message.getFileName());
        fileLink.setStyle("-fx-font-size: 15px; -fx-text-fill: blue");
        fileLink.setOnAction(e -> saveFile(message.getFileData(), message.getFileName()));
        messageContent.getChildren().add(fileLink);
    }

    private void addTextContent(Message message, VBox messageContent) {
        if (message.getContent() != null) {
            Text messageText = new Text(message.getContent());
            messageText.setWrappingWidth(MAX_MESSAGE_WIDTH - 20);
            messageContent.getChildren().add(messageText);
        }
    }

    private void addBroadcastContent(Message message, VBox messageContent) {
        if (message.getContent() != null) {
            Text messageText = new Text(message.getContent());
            messageText.setWrappingWidth(MAX_MESSAGE_WIDTH - 20);
            messageText.setStyle("-fx-fill: #000000;");
            messageContent.getChildren().add(messageText);
        }
    }

    private void addErrorContent(VBox messageContent, String errorMessage) {
        Text errorText = new Text(errorMessage);
        messageContent.getChildren().add(errorText);
    }

    private void saveFile(byte[] fileData, String fileName) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu file");
        fileChooser.setInitialFileName(fileName);
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Files", "*.*")
        );
        File file = fileChooser.showSaveDialog(chatScrollPane.getScene().getWindow());
        if (file != null) {
            try {
                Files.write(file.toPath(), fileData);
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR,
                        "Lỗi khi lưu file: " + e.getMessage(),
                        ButtonType.OK);
                alert.showAndWait();
            }
        }
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
    }
}

