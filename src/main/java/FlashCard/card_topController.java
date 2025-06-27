package FlashCard;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class card_topController {

    @FXML private Label deckTitleLabel;
    @FXML private ImageView menu;
    @FXML private Button menuButton;

    // 1) Thêm field lưu parent controller
    private trangChuController parentController;

    // 3) Setter để bên ngoài gọi khi load FXML
    public void setParentController(trangChuController parent) {
        this.parentController = parent;
    }

    @FXML
    public void initialize() {
        // load icon menu như cũ
        Image image = new Image("/menu.png");
        menu.setImage(image);

    }

    @FXML
    private void onMenuClicked() {
        if (parentController != null) {
            parentController.handleChoose();
        }
    }

    public void setDeckTitle(String title) {
        if (deckTitleLabel != null) {
            deckTitleLabel.setText(title);
        }
    }
}

