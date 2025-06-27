package FlashCard;

import OOP.Card;
import OOP.boThe;
import Practice.practice_bottomController;
import Practice.practice_centerController;
import Read.Read_bottomController;

import Read.Read_centerController;
import Review.review_bottomController;
import Review.review_centerController;
import Write.write_bottomController;
import Write.write_centerController;
import javafx.fxml.FXML;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class card_bottomController {
     private trangChuController parent;
     @FXML private ImageView add;
     @FXML private AnchorPane anchorPane;
     @FXML private ImageView searchIV;
     @FXML private TextField searchField;


    private BorderPane mainRoot;
    private boThe deck;
    /**
     * MainController hoặc trangChuController phải gọi setContext() ngay khi load FXML
     */
    public void setContext(BorderPane mainRoot, boThe deck) {
        this.mainRoot = mainRoot;
        this.deck = deck;
    }

    public void setContext(BorderPane mainRoot, boThe deck, trangChuController parentController) {
        this.mainRoot = mainRoot;
        this.deck = deck;
        this.parent = parentController;
    }

    @FXML
    private void handlePracticeWriting(ActionEvent event) {
//        if (deck == null) {
//            System.out.println("Deck chưa được khởi tạo.");
//            // TODO: Hiển thị alert cho người dùng
//            return;
//        }
        try {
            FXMLLoader centerLoader = new FXMLLoader(
                    getClass().getResource("/Write/write_center.fxml")
            );
            BorderPane centerPane = centerLoader.load();
            write_centerController centerCtrl = centerLoader.getController();


            FXMLLoader bottomLoader = new FXMLLoader(
                    getClass().getResource("/Write/write_bottom.fxml")
            );
            AnchorPane bottomPane = bottomLoader.load();
            write_bottomController bottomCtrl = bottomLoader.getController();

            centerCtrl.setBottomController(bottomCtrl);
            bottomCtrl.setCenterController(centerCtrl);
            bottomCtrl.setContext(mainRoot,deck,parent);

//            List<Card> cardList = CardRequest.getCard(deck.getDeckId());
//            centerCtrl.setCards(cardList);

            // 1. Lấy toàn bộ thẻ
            List<Card> allCards = CardRequest.getCard(deck.getDeckId());

            // 2. Lọc thẻ due để review đầu tiên
            List<Card> due = allCards.stream()
                    .filter(c -> !c.getNextReviewDate().isAfter(LocalDateTime.now()))
                    .limit(5)
                    .collect(Collectors.toList());

            boolean noDue = due.isEmpty();
            // nếu có due thì chỉ review due, ngược lại thì bắt đầu paging từ đầu
            List<Card> firstBatch = noDue
                    ? allCards.stream().limit(5).collect(Collectors.toList())
                    : due;

            // 3. Truyền cả list gốc, offset=0, và flag skipRating (= noDue)
            centerCtrl.initReviewSession(allCards, 0, noDue);
            centerCtrl.setCards(firstBatch);


            mainRoot.setCenter(centerPane);
            mainRoot.setBottom(bottomPane);

        } catch (IOException e) {
            e.printStackTrace();
            // TODO: Hiển thị Alert báo lỗi load FXML
        } catch (RuntimeException e) {
            e.printStackTrace();
            // TODO: Alert lấy dữ liệu từ server không thành công
        }
    }

    @FXML
    private void handleReview(ActionEvent evt) {
        try {
            FXMLLoader centerLoader = new FXMLLoader(getClass().getResource("/Review/review_center.fxml"));
            BorderPane centerPane = centerLoader.load();
            review_centerController centerCtrl = centerLoader.getController();

            FXMLLoader bottomLoader = new FXMLLoader(getClass().getResource("/Review/review_bottom.fxml"));
            AnchorPane bottomPane = bottomLoader.load();
            review_bottomController bottomCtrl = bottomLoader.getController();

            centerCtrl.setBottomController(bottomCtrl);
            bottomCtrl.setCenterController(centerCtrl);
            bottomCtrl.setContext(mainRoot, deck, parent);

            // 1. Lấy toàn bộ thẻ
            List<Card> allCards = CardRequest.getCard(deck.getDeckId());

            // 2. Lọc thẻ due để review đầu tiên
            List<Card> due = allCards.stream()
                    .filter(c -> !c.getNextReviewDate().isAfter(LocalDateTime.now()))
                    .limit(5)
                    .collect(Collectors.toList());

            boolean noDue = due.isEmpty();
            // nếu có due thì chỉ review due, ngược lại thì bắt đầu paging từ đầu
            List<Card> firstBatch = noDue
                    ? allCards.stream().limit(5).collect(Collectors.toList())
                    : due;

            // 3. Truyền cả list gốc, offset=0, và flag skipRating (= noDue)
            centerCtrl.initReviewSession(allCards, 0, noDue);
            centerCtrl.setCards(firstBatch);

            mainRoot.setCenter(centerPane);
            mainRoot.setBottom(bottomPane);

        } catch (IOException|RuntimeException e) {
            e.printStackTrace();
            // TODO: Alert lỗi
        }
    }



    @FXML
    private void handlePractice(ActionEvent evt) {
        try {
            // Hiển thị hộp thoại chọn ngôn ngữ
            ChoiceDialog<String> dialog = new ChoiceDialog<>("Tiếng Anh", "Tiếng Anh", "Tiếng Việt");
            dialog.setTitle("Chọn ngôn ngữ");
            dialog.setHeaderText("Chọn ngôn ngữ hiển thị cho nhãn trên cùng:");
            dialog.setContentText("Ngôn ngữ:");

            dialog.getDialogPane().getStylesheets().add(getClass().getResource("/Flashcard/dialog.css").toExternalForm());

            Optional<String> result = dialog.showAndWait();
            if (!result.isPresent()) {
                return;
            }
            String selectedLanguage = result.get();

            // Tải practice_center.fxml
            FXMLLoader centerLoader = new FXMLLoader(getClass().getResource("/Practice/practice_center.fxml"));
            VBox centerPane = centerLoader.load(); // Sử dụng VBox
            practice_centerController centerCtrl = centerLoader.getController();

            // Tải practice_bottom.fxml
            FXMLLoader bottomLoader = new FXMLLoader(getClass().getResource("/Practice/practice_bottom.fxml"));
            Parent bottomPane = bottomLoader.load();
            practice_bottomController bottomCtrl = bottomLoader.getController();

            // Thiết lập liên kết
            centerCtrl.setBottomController(bottomCtrl);
            bottomCtrl.setCenterController(centerCtrl);
            bottomCtrl.setContext(mainRoot, deck, parent);

            // Lấy danh sách thẻ và truyền ngôn ngữ
//            List<Card> cardList = CardRequest.getCard(deck.getDeckId());
//            centerCtrl.setCards(cardList, selectedLanguage);

            // 1. Lấy toàn bộ thẻ
            List<Card> allCards = CardRequest.getCard(deck.getDeckId());

            // 2. Lọc thẻ due để review đầu tiên
            List<Card> due = allCards.stream()
                    .filter(c -> !c.getNextReviewDate().isAfter(LocalDateTime.now()))
                    .limit(5)
                    .collect(Collectors.toList());

            boolean noDue = due.isEmpty();
            // nếu có due thì chỉ review due, ngược lại thì bắt đầu paging từ đầu
            List<Card> firstBatch = noDue
                    ? allCards.stream().limit(5).collect(Collectors.toList())
                    : due;

            // 3. Truyền cả list gốc, offset=0, và flag skipRating (= noDue)
            centerCtrl.initReviewSession(allCards, 0, noDue);
            centerCtrl.setCards(firstBatch,selectedLanguage);

            // Cập nhật giao diện
            mainRoot.setCenter(centerPane);
            mainRoot.setBottom(bottomPane);

        } catch (IOException e) {
            e.printStackTrace();
            // TODO: Hiển thị Alert báo lỗi tải FXML
        } catch (RuntimeException e) {
            e.printStackTrace();
            // TODO: Hiển thị Alert báo lỗi lấy dữ liệu từ server
        }
    }

    @FXML
    private void handleRead() {
        if (mainRoot == null || deck == null || parent == null) {
            System.err.println("card_bottomController: context chưa được thiết lập trước khi gọi handleRead");
            return;
        }
        try {
            // Load Read_center.fxml
            FXMLLoader centerLoader = new FXMLLoader(getClass().getResource("/Read/Read_center.fxml"));
            Parent centerPane = centerLoader.load();
            Read_centerController centerCtrl = centerLoader.getController();
            centerCtrl.setCards(CardRequest.getCard(deck.getDeckId())); // Lấy danh sách thẻ từ server

            // Load Read_bottom.fxml
            FXMLLoader bottomLoader = new FXMLLoader(getClass().getResource("/Read/Read_bottom.fxml"));
            Parent bottomPane = bottomLoader.load();
            Read_bottomController bottomCtrl = bottomLoader.getController();
            bottomCtrl.setContext(mainRoot, deck, parent);
            bottomCtrl.setCenterController(centerCtrl);

            // Liên kết bottom controller với center controller
            centerCtrl.setBottomController(bottomCtrl);
            centerCtrl.setParentController(parent);

            // 1. Lấy toàn bộ thẻ
            List<Card> allCards = CardRequest.getCard(deck.getDeckId());

            // 2. Lọc thẻ due để review đầu tiên
            List<Card> due = allCards.stream()
                    .filter(c -> !c.getNextReviewDate().isAfter(LocalDateTime.now()))
                    .limit(5)
                    .collect(Collectors.toList());

            boolean noDue = due.isEmpty();
            // nếu có due thì chỉ review due, ngược lại thì bắt đầu paging từ đầu
            List<Card> firstBatch = noDue
                    ? allCards.stream().limit(5).collect(Collectors.toList())
                    : due;

            // 3. Truyền cả list gốc, offset=0, và flag skipRating (= noDue)
            centerCtrl.initReviewSession(allCards, 0, noDue);
            centerCtrl.setCards(firstBatch);

            // Cập nhật giao diện
            mainRoot.setCenter(centerPane);
            mainRoot.setBottom(bottomPane);
        } catch (IOException e) {
            e.printStackTrace();
            // TODO: Hiển thị Alert cho người dùng
        }
    }

    @FXML
     public void initialize(){
         Image image = new Image("/addfc.png");
         add.setImage(image);
         Image image1 = new Image("/search.png");
         searchIV.setImage(image1);

         searchField.textProperty().addListener((obs, oldVal, newVal) -> {
             card_centerController.getInstance().setFilter(newVal);
         });
     }

     public void setParent(trangChuController parent) {
         this.parent = parent;
     }

    @FXML
    private void handleBack(ActionEvent e) {
            parent.restoreHome();  // Chỉ quay lại trang chủ mà không thêm từ
    }

    @FXML
    private void handleAdd(ActionEvent e) {
        // 1. Tạo dialog nhập Việt – Anh
        Dialog<Pair<String,String>> dlg = new Dialog<>();
        dlg.setTitle("Thêm từ mới");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField tfAnh  = new TextField();
        tfAnh.setPromptText("Tiếng Anh");
        TextField tfViet = new TextField();
        tfViet.setPromptText("Tiếng Việt");

        grid.add(new Label("Tiếng Anh:"),  0, 0);
        grid.add(tfAnh,  1, 0);
        grid.add(new Label("Tiếng Việt:"), 0, 1);
        grid.add(tfViet, 1, 1);

        dlg.getDialogPane().setContent(grid);
        dlg.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                return new Pair<>(tfAnh.getText().trim(), tfViet.getText().trim());
            }
            return null;
        });

        Optional<Pair<String,String>> res = dlg.showAndWait();
        res.ifPresent(pair -> {
             String engText = pair.getKey();
             String vietText  = pair.getValue();
            if (!engText.isEmpty() && !vietText.isEmpty()) {
                // 2. Gọi thẳng vào centerController qua singleton
                card_centerController.getInstance().addVocabulary(engText, vietText);

            }
        });
    }

    @FXML
    private void handleLoadFromFile(ActionEvent event) {
        Alert format = new Alert(Alert.AlertType.INFORMATION);
        format.setTitle("Định dạng file");
        format.setHeaderText("Vui lòng dùng định dạng:\nTiếng anh, tiếng việt");
        format.setContentText("Ví dụ:\nHello, xin chào\n...");
        format.showAndWait();

        // Lấy cửa sổ hiện tại để làm owner của FileChooser
        Window stage = anchorPane.getScene().getWindow();

        // Khởi tạo FileChooser và lọc chỉ hiển thị file CSV
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Chọn file từ vựng");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv","*.txt")
        );

        // Hiển thị dialog chọn file
        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    // Giả sử định dạng: "Tiếng Việt,Tiếng Anh"
                    String[] parts = line.split(",");
                    if (parts.length >= 2) {
                        String anh = parts[0].trim();
                        String viet  = parts[1].trim();
                        if (!viet.isEmpty() && !anh.isEmpty()) {
                            card_centerController.getInstance()
                                    .addVocabulary(anh, viet);

                        } else {
                            Alert err = new Alert(Alert.AlertType.ERROR);
                            err.setTitle("Lỗi định dạng dòng");
                            err.setHeaderText(null);
                            err.setContentText("Dòng sau không hợp lệ:\n "+line);
                            err.showAndWait();
                        }
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                Alert io = new Alert(Alert.AlertType.ERROR);
                io.setTitle("Lỗi đọc file");
                io.setHeaderText("Không thể đọc file\nVui lòng kiểm tra lại file");
                io.showAndWait();
            }
        }
    }



}
