package loginFlashcard;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;
import javafx.util.Duration;

public class RegisterController {

    @FXML
    private Hyperlink loginLink;

    @FXML
    private VBox leftPane;

    @FXML
    private Polygon animatedPolygon;
    @FXML
    private ImageView eyeIcon;
    @FXML
    private ImageView user;
    @FXML
    ImageView users;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private Button togglePasswordButton;

//    @FXML private PasswordField confirmPasswordField;
//    @FXML private TextField visibleConfirmPasswordField;
//    @FXML private Button toggleConfirmPasswordButton;

    @FXML
    private TextField emailField;
    @FXML
    private Label errorLabel;
    @FXML private TextField username;


    @FXML
    private void initialize() {
        fadeInContent();
        Image image = new Image("/eye.png");
        eyeIcon.setImage(image);
        Image image1 = new Image("/users.png");
        user.setImage(image1);
        Image image2 = new Image("/users.png");
        users.setImage(image2);
    }
    // hiệu ứng
    private void fadeInContent() {
        FadeTransition fade = new FadeTransition(Duration.millis(800), leftPane);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }
    //chuyển cảnh
    @FXML
    private void handleLoginAction() {
        Stage stage = (Stage) loginLink.getScene().getWindow();
        SceneSwitcher.slideOutAndSwitchScene(
                stage,
                animatedPolygon,
                "/loginFlashcard/LoginClient.fxml",
                "/loginFlashcard/LoginStyle.css",
                "Login Page",
                false // slide sang phải
        );
    }



    private boolean passwordVisible = false;

    //hiển thị mật khẩu
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


//    private boolean confirmPasswordVisible = false;
//
//    @FXML
//    private void toggleConfirmPasswordVisibility() {
//        if (confirmPasswordVisible) {
//            confirmPasswordField.setText(visibleConfirmPasswordField.getText());
//            confirmPasswordField.setVisible(true);
//            confirmPasswordField.setManaged(true);
//
//            visibleConfirmPasswordField.setVisible(false);
//            visibleConfirmPasswordField.setManaged(false);
//
//            confirmEyeIcon.setImage(new Image("file:D:/icon/hidden.png"));
//        } else {
//            visibleConfirmPasswordField.setText(confirmPasswordField.getText());
//            visibleConfirmPasswordField.setVisible(true);
//            visibleConfirmPasswordField.setManaged(true);
//
//            confirmPasswordField.setVisible(false);
//            confirmPasswordField.setManaged(false);
//
//            confirmEyeIcon.setImage(new Image("file:D:/icon/eye.png"));
//        }
//        confirmPasswordVisible = !confirmPasswordVisible;
//    }



    // hiển thị không được bỏ trống
    @FXML
    private void handleSignUpAction() {
        // Lấy giá trị
        String user = username.getText().trim();
        String email = emailField.getText().trim();
        String pwd = passwordVisible
                ? visiblePasswordField.getText().trim()
                : passwordField.getText().trim();
//        String confPwd = confirmPasswordVisible
//                ? visibleConfirmPasswordField.getText()
//                : confirmPasswordField.getText();

        if (user.isEmpty()||pwd.isEmpty()||email.isEmpty()) {
            errorLabel.setText("Vui lòng điền đầy đủ các trường.");
            errorLabel.setVisible(true);
            return;
        }
        if (!email.endsWith("@gmail.com")) {
            errorLabel.setText("Email phải có đuôi @gmail.com");
            errorLabel.setVisible(true);
            return;
        }




//        if (confPwd.isEmpty()) {
//            errorLabel.setText("Confirm password cannot be empty!");
//            errorLabel.setVisible(true);
//            return;
//        }
//        if (!pwd.equals(confPwd)) {
//            errorLabel.setText("Confirmation password does not match!");
//            errorLabel.setVisible(true);
//            return;
//        }

        // Hợp lệ → ẩn thông báo và tiếp tục
        errorLabel.setVisible(false);
        String hashPass = Utils.hashPassword(pwd);
        boolean success = LoginRequest.sendRegisterRequest("REGISTER",user,email, hashPass);
        if (success) {
            System.out.println("Đăng ký thành công!");
            // chuyển đến màn login
            handleLoginAction();
        } else {
            errorLabel.setText("Username hoặc Email có thể đã tồn tại.");
            errorLabel.setVisible(true);
        }
    }

}
