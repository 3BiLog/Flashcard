package loginFlashcard;

import FlashCard.trangChu;
import OOP.User;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.image.Image;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import static javafx.scene.control.Alert.*;

public class LoginController {

    @FXML private Hyperlink signUpLink;
    @FXML private VBox leftPane;
    @FXML private Polygon animatedPolygon;
    @FXML private ImageView eyeIcon;
    @FXML private ImageView user;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private Button togglePasswordButton;

    @FXML private TextField emailField;
    @FXML private Label errorLabel;
    @FXML private Hyperlink forgotLink;
    private Map<String, String> otpStore = new HashMap<>();
    private boolean passwordVisible = false;

    @FXML
    private void initialize() {
        fadeInContent();
        Image image = new Image("/eye.png");
        eyeIcon.setImage(image);
        Image image1 = new Image("/users.png");
        user.setImage(image1);
    }
    //hiệu ứng làm mờ
    private void fadeInContent() {
        FadeTransition fade = new FadeTransition(Duration.millis(800), leftPane);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }

    @FXML
    private void handleSignUpAction() {
        Stage stage = (Stage) signUpLink.getScene().getWindow();
        SceneSwitcher.slideOutAndSwitchScene(
                stage,
                animatedPolygon,
                "/loginFlashcard/RegisterClient.fxml",
                "/loginFlashcard/RegisterStyle.css",
                "Register Page",
                true // slide sang trái
        );
    }



    @FXML
    private void togglePasswordVisibility() {
        if (passwordVisible) {
            // Ẩn mật khẩu
            passwordField.setText(visiblePasswordField.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);

            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);

            eyeIcon.setImage(new Image("/hidden.png"));
        } else {
            // Hiện mật khẩu
            visiblePasswordField.setText(passwordField.getText());
            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);

            passwordField.setVisible(false);
            passwordField.setManaged(false);

            eyeIcon.setImage(new Image("/eye.png"));
        }
        passwordVisible = !passwordVisible;
    }


    @FXML
    private void handleLoginAction(){
        String username = emailField.getText().trim();
        String email = emailField.getText().trim();
        String pass = passwordVisible?visiblePasswordField.getText() : passwordField.getText();
        if(email.isEmpty() || username.isEmpty()||pass.isEmpty()){
            errorLabel.setText("Vui lòng điền đầy đủ các trường.");
            errorLabel.setVisible(true);
            return;
        }


        errorLabel.setVisible(false);
        String hashedPassword = Utils.hashPassword(pass);
        User loggedUser = LoginRequest.sendRequest("LOGIN",username,hashedPassword);
        if(loggedUser != null){
            try {
//                int userID = LoginRequest.
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/FlashCard/trangChu.fxml"));
                Parent root = loader.load();

                // Nếu cần truyền dữ liệu thì thêm ở đây
                // trangChuController controller = loader.getController();
                // controller.setUserInfo(username);

                FlashCard.trangChuController home = loader.getController();
                home.SetUserID(loggedUser.getId());

                Stage stage = (Stage) emailField.getScene().getWindow();
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/FlashCard/trangChu.css").toExternalForm());

                stage.setScene(scene);
                stage.setMaximized(true);
                stage.setTitle("FlashCard - Trang chủ");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                errorLabel.setText("Lỗi khi mở trang chủ!");
                errorLabel.setVisible(true);
            }
        }else {
            errorLabel.setText("Đăng nhập thất bại");
            errorLabel.setVisible(true);
        }
    }

    @FXML
    private void handleForgotPassword() {
        // 1) Nhập email
        TextInputDialog emailDialog = new TextInputDialog();
        emailDialog.setTitle("Quên mật khẩu");
        emailDialog.setHeaderText("Nhập email để nhận mã OTP");
        emailDialog.setContentText("Email:");
        Optional<String> emailOpt = emailDialog.showAndWait();
        if (!emailOpt.isPresent()) {
            return;  // Người dùng bấm Cancel
        }
        String email = emailOpt.get().trim();

        // 2) Kiểm tra định dạng email
        if (!email.endsWith("@gmail.com")) {
            showAlert(AlertType.ERROR, "Email không hợp lệ (phải có đuôi @gmail.com).");
            return;
        }

        // 3) Kiểm tra email đã đăng ký trên server
        boolean exists = LoginRequest.checkEmailExists(email);
        if (!exists) {
            showAlert(AlertType.WARNING, "Email này chưa được đăng ký.");
            return;
        }

        // 4) Sinh và gửi OTP
        String otp = String.format("%06d", new Random().nextInt(1_000_000));
        otpStore.put(email, otp);
        boolean sent = EmailUtil.sendOTP(email, otp);
        if (!sent) {
            showAlert(AlertType.ERROR, "Gửi OTP thất bại, vui lòng thử lại sau.");
            return;
        }
        showAlert(AlertType.INFORMATION, "Mã OTP đã được gửi vào email của bạn.");

        // 5) Hiển thị dialog nhập OTP và mật khẩu mới
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Xác thực OTP & Đổi mật khẩu");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // Các ô nhập
        Label otpLabel      = new Label("OTP:");
        TextField otpField  = new TextField();
        Label newPassLabel  = new Label("Mật khẩu mới:");
        PasswordField newPw = new PasswordField();
        Label confPassLabel = new Label("Xác nhận mật khẩu:");
        PasswordField confPw= new PasswordField();

        grid.add(otpLabel,      0, 0);
        grid.add(otpField,      1, 0);
        grid.add(newPassLabel,  0, 1);
        grid.add(newPw,         1, 1);
        grid.add(confPassLabel, 0, 2);
        grid.add(confPw,        1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Validate trước khi đóng dialog
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, ev -> {
            String enteredOtp = otpField.getText().trim();
            String p1 = newPw.getText().trim();
            String p2 = confPw.getText().trim();

            if (!enteredOtp.equals(otp)) {
                showAlert(AlertType.ERROR, "OTP không đúng!");
                ev.consume();
            } else if (p1.isEmpty() || p2.isEmpty()) {
                showAlert(AlertType.WARNING, "Mật khẩu không được để trống!");
                ev.consume();
            } else if (!p1.equals(p2)) {
                showAlert(AlertType.WARNING, "Mật khẩu xác nhận không khớp!");
                ev.consume();
            } else if (p1.length() < 6) {
                showAlert(AlertType.WARNING, "Mật khẩu phải có ít nhất 6 ký tự!");
                ev.consume();
            }
        });

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // 6) Đổi mật khẩu trên server
            String hashed = Utils.hashPassword(newPw.getText().trim());
            boolean resetOk = LoginRequest.resetPassword(email, hashed);
            if (resetOk) {
                showAlert(AlertType.INFORMATION, "Đổi mật khẩu thành công! Vui lòng đăng nhập lại.");
            } else {
                showAlert(AlertType.ERROR, "Đổi mật khẩu thất bại, vui lòng thử lại.");
            }
        }
    }

    // Giữ nguyên phương thức tiện ích này trong controller
    private void showAlert(AlertType type, String msg) {
        Alert alert = new Alert(type, msg, ButtonType.OK);
        alert.initOwner(forgotLink.getScene().getWindow());
        alert.showAndWait();
    }



}
