package FlashCard;

import OOP.User;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.Optional;

public class ProfileDialogController {
    @FXML private ImageView avatarView;
    @FXML private Label nameLabel;
    @FXML private Label emailLabel;
    @FXML private Button editButton;

    private ProfileRequest profileRequest = new ProfileRequest();
    private User currentUser;
    private int userId;

    @FXML
    public void initialize() {
        Image image = new Image("/user.png");
        avatarView.setImage(image);

        if (editButton != null) {
            editButton.setOnAction(event -> handleEdit());
        }
    }

    public void setUser(User user, int userId) {
        this.currentUser = user;
        this.userId = userId;

        nameLabel.setText(user.getUsername() != null ? user.getUsername() : "N/A");
        emailLabel.setText(user.getEmail() != null ? user.getEmail() : "N/A");
    }

    private void handleEdit() {
        TextInputDialog nameDialog = new TextInputDialog(nameLabel.getText());
        nameDialog.setTitle("Chỉnh sửa thông tin");
        nameDialog.setHeaderText("Chỉnh sửa tên người dùng");
        nameDialog.setContentText("Tên mới:");

        Optional<String> nameResult = nameDialog.showAndWait();
        if (nameResult.isPresent() && !nameResult.get().trim().isEmpty()) {
            String newName = nameResult.get().trim();

            TextInputDialog emailDialog = new TextInputDialog(emailLabel.getText());
            emailDialog.setTitle("Chỉnh sửa thông tin");
            emailDialog.setHeaderText("Chỉnh sửa email");
            emailDialog.setContentText("Email mới:");

            Optional<String> emailResult = emailDialog.showAndWait();
            if (emailResult.isPresent() && !emailResult.get().trim().isEmpty()) {
                String newEmail = emailResult.get().trim();
                if (!newEmail.endsWith("@gmail.com")) {
                    showAlert("Lỗi", "Email phải có đuôi @gmail.com.");
                    return;
                }

                // Kiểm tra tính duy nhất của username và email
                if (profileRequest.checkUsernameOrEmailExists(newName, newEmail, userId)) {
                    showAlert("Lỗi", "Tên người dùng hoặc email đã tồn tại.");
                    return;
                }

                boolean success = profileRequest.updateUser(userId, newName, newEmail);
                if (success) {
                    nameLabel.setText(newName);
                    emailLabel.setText(newEmail);
                    currentUser.setUsername(newName);
                    currentUser.setEmail(newEmail);
                } else {
                    showAlert("Lỗi", "Không thể cập nhật thông tin. Vui lòng thử lại.");
                }
            }
        }
    }

    private void showAlert(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}