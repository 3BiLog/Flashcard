package FlashCard;


import Chat.ChatBottomController;
import Chat.ChatCenterController;
import Chat.ChatConnectionManager;
import OOP.Card;
import OOP.Message;
import OOP.User;
import OOP.boThe;

import Read.Read_centerController;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
//import java.time.Duration;
import java.awt.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.time.LocalDateTime;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import javafx.scene.input.MouseEvent;
import statistical.statController;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


public class  trangChuController {
    @FXML private GridPane cardGrid;
    @FXML private BorderPane mainPane;
    @FXML private VBox leftPane;
    @FXML private ScrollPane centerPane;
    @FXML private AnchorPane bottomPane;
    @FXML private BorderPane topPane;
    @FXML private ImageView menu;
    @FXML private ImageView add;
    @FXML private ImageView user;
    @FXML private ImageView searchIV;
    @FXML private TextField searchField;
    @FXML private Button userButton;

    private ObservableList<boThe> boTheList = FXCollections.observableArrayList();
    private FilteredList<boThe> filteredList;

    private ReviewTrayNotifier notifier;

    private List<boThe> cards = new ArrayList<>();
    private int userID;

    private statController statController;

    private User currentUser;
    private ChatCenterController chatCenterCtrl;
    private ChatBottomController chatBottomCtrl;
    private ChatConnectionManager connectionManager;
    private Socket sharedSocket;
    private ObjectOutputStream sharedOut;
    private ObjectInputStream sharedIn;
    private Parent chatCenterPane; // Lưu pane chat center
    private Parent chatBottomPane;

    private Read_centerController readCenterController;

    private boolean isLeftVisible = true;

    private int row = 0;
    private int col = 0;

//    public void setUser(int userID, User user) {
//        this.userID = userID;
//        this.currentUser = user;
//        loadDecksFromServer();
//    }

//     Phương thức cũ (nếu cần giữ lại)
    public void SetUserID(int userID) {
        this.userID = userID;
        this.currentUser = getUserFromServer(userID);
        loadDecksFromServer();
    }

    private User getUserFromServer(int userID) {
        try {
            ProfileRequest profileRequest = new ProfileRequest();
            return profileRequest.getUserById(userID);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @FXML
    public void initialize() {
        try {
            notifier = new ReviewTrayNotifier();
        } catch (AWTException e) {
            System.err.println("Không thể khởi tạo System Tray: " + e.getMessage());
        }
//        homeTopChildren = new ArrayList<>(topContainer.getChildren());
//
//        // Các thiết lập khác (không đổi gì)
//        Node node = topContainer.lookup("#menu");
//        if (node instanceof Button) {
//            // Bạn có thể thiết lập sự kiện cho nút menu tại đây nếu cần.
//        }
        Image image = new Image("/menu.png");
        menu.setImage(image);
        Image image1 = new Image("/addfc.png");
        add.setImage(image1);
        Image image2 = new Image("/user.png");
        user.setImage(image2);
        Image image3 = new Image("/search.png");
        searchIV.setImage(image3);
        // Khi ScrollPane thay đổi kích thước, GridPane luôn bám theo
        cardGrid.prefWidthProperty().bind(
                centerPane.widthProperty()
                        .subtract( centerPane.getPadding().getLeft() + centerPane.getPadding().getRight() )
        );

        leftPane.prefWidthProperty().bind(mainPane.widthProperty().multiply(0.15));

        filteredList = new FilteredList<>(boTheList,b->true);
        searchField.textProperty().addListener((obs,oldVal,newVal) ->{
            String filter = newVal.toLowerCase().trim();
            if(filter.isEmpty()){
                filteredList.setPredicate(b->true);
            }else{
                filteredList.setPredicate(b->
                        b.getName().toLowerCase().contains(filter));
            }
            refreshGridSearch();
        });

        loadDecksFromServer();
    }


    //hàm thêm 1 bộ thẻ
    @FXML
    private void handleThem() {
        promptForTitle().ifPresent(userTitle -> {
            boThe create = botheRequest.addDeck(userID,userTitle,10);
//                    AnchorPane card = buildCard(userTitle);
//                    cards.add(card);
            if (create != null) {
                boTheList.add(create);
                refreshGridSearch();
//                cards.add(create);
//                refreshGrid();
            }

        });
    }

    /** Hiển thị dialog và trả về Optional tiêu đề nếu người dùng OK. */
    private Optional<String> promptForTitle() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nhập tiêu đề thẻ");
        dialog.setHeaderText(null);
        dialog.setContentText("Tiêu đề:");
        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            String trimmed = result.get().trim();
            if (!trimmed.isEmpty()) {
                return Optional.of(trimmed);
            }
        }

        return Optional.empty();
    }

    /** Xây dựng 1 AnchorPane card với tiêu đề, nút học, menu tuỳ chọn… */
    private AnchorPane buildCard(boThe deck) {
        AnchorPane card = new AnchorPane();
        card.setPrefHeight(400);
        card.setMinWidth(0);
        card.setMaxWidth(Double.MAX_VALUE);
        GridPane.setFillWidth(card, true);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #DDD; -fx-border-radius: 10;");

        Rectangle header = createHeader(card);
        Text title     = createTitle(deck.getName());
        int count = botheRequest.countCard(deck.getDeckId());
        Text subtitle = createSubtitle(count + " thẻ");
        Button start   = createStartButton(deck);
        Button opts    = createOptionsButton(card,deck);

        BarChart<String, Number> memoryChart = createMemoryChart(deck.getDeckId());
        AnchorPane.setTopAnchor(memoryChart, 110.0); // Điều chỉnh vị trí
        AnchorPane.setLeftAnchor(memoryChart, 10.0);
        AnchorPane.setRightAnchor(memoryChart, 10.0);

        // Thêm thông tin từ cần ôn và đếm ngược
        Text reviewInfo = createReviewInfo(deck.getDeckId());
        AnchorPane.setTopAnchor(reviewInfo, 310.0); // Điều chỉnh vị trí dưới biểu đồ
        AnchorPane.setLeftAnchor(reviewInfo, 10.0);
        AnchorPane.setRightAnchor(reviewInfo, 10.0);


//        card.getChildren().addAll(header, title, subtitle, start, opts,chart);
        card.getChildren().addAll(header, title, subtitle, start, opts,memoryChart,reviewInfo);

        return card;
    }

    private BarChart<String, Number> createMemoryChart(int deckId) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        // Ẩn toàn bộ trục Y
        yAxis.setTickLabelsVisible(false);
        yAxis.setTickMarkVisible(false);
        yAxis.setMinorTickVisible(false);
        yAxis.setLabel("");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setPrefHeight(200);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setCategoryGap(20);
        chart.setBarGap(5);
        chart.setStyle("-fx-background-color: transparent;");

        // Lấy dữ liệu
        List<Card> cards = CardRequest.getCard(deckId);
        Map<Integer, Long> memoryCount = cards.stream()
                .filter(c -> c.getNextReviewDate() != null)
                .collect(Collectors.groupingBy(Card::getMemoryLevel, Collectors.counting()));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        String[] levels = { "0", "1", "2", "3", "4", "5" };
        String[] colors = { "#FF0000", "#FF4500", "#FFA500", "#80ff80", "#008000", "#0000CD" };

        for (int i = 0; i < levels.length; i++) {
            String lvl = levels[i];
            long count = memoryCount.getOrDefault(i, 0L);
            XYChart.Data<String, Number> data = new XYChart.Data<>(lvl, count);
            series.getData().add(data);

            final int colorIndex = i;
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    // Đổi màu ngay
                    newNode.setStyle("-fx-bar-fill: " + colors[colorIndex] + ";");

                    // Chờ đến sau khi node đã được add vào parent
                    Platform.runLater(() -> {
                        Parent parent = newNode.getParent();
                        if (parent instanceof Group) {
                            Group plotArea = (Group) parent;

                            // Tạo label
                            Text label = new Text(String.valueOf(count));
                            plotArea.getChildren().add(label);

                            // Cập nhật vị trí
                            newNode.boundsInParentProperty().addListener((bObs, oldB, bounds) -> {
                                double x = bounds.getMinX() + bounds.getWidth()/2 - label.prefWidth(-1)/2;
                                double y = bounds.getMinY() - label.prefHeight(-1) - 2;
                                label.relocate(x, y);
                            });
                        }
                    });
                }
            });
        }

        chart.getData().add(series);
        return chart;
    }

    private Text createReviewInfo(int deckId) {
        Text reviewInfo = new Text();
        reviewInfo.setStyle("-fx-font-size: 14px; -fx-fill: #333;");

        // Lấy danh sách thẻ cho bộ thẻ
        List<Card> cards = CardRequest.getCard(deckId);

        // Đếm số từ cần ôn (nextReviewDate <= thời điểm hiện tại + buffer 1 phút)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nowWithBuffer = now.plusMinutes(1);
        long dueCount = cards.stream()
                .filter(card -> card.getNextReviewDate() != null && !card.getNextReviewDate().isAfter(nowWithBuffer))
                .count();

        // 4. Nhóm các thẻ chưa đến hạn theo nextReviewDate
        Map<LocalDateTime, Long> futureBatches = cards.stream()
                .filter(card -> card.getNextReviewDate() != null
                        && card.getNextReviewDate().isAfter(now))
                .collect(Collectors.groupingBy(
                        Card::getNextReviewDate,
                        Collectors.counting()
                ));

        // 5. Sắp xếp các đợt theo thời gian
        List<Map.Entry<LocalDateTime, Long>> sortedBatches = futureBatches.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toList());

        // 6. Xây dựng văn bản hiển thị
        StringBuilder infoText = new StringBuilder();
        if ( sortedBatches.isEmpty()) {
            infoText.append("Chưa có từ cần ôn tập");
            reviewInfo.setText(infoText.toString());
            return reviewInfo;
        }

        if (!sortedBatches.isEmpty()) {
            // Đợt kế tiếp
            Map.Entry<LocalDateTime, Long> nextBatch = sortedBatches.get(0);
            long batchSize = nextBatch.getValue();
            LocalDateTime batchTime = nextBatch.getKey();

            long initialSecondsUntilNext = ChronoUnit.SECONDS.between(now, batchTime);
            if (initialSecondsUntilNext > 0) {
                infoText.append(batchSize).append(" từ sau ");
                // Bắt đầu đếm ngược, updateCountdown sẽ nối thêm giờ/phút/giây vào infoText
                updateCountdown(reviewInfo, batchTime, infoText);
            }
        } else {
            // Không còn đợt nào trong tương lai -> chỉ hiển thị dueCount
            reviewInfo.setText(infoText.toString());
        }

        reviewInfo.setWrappingWidth(210);
        return reviewInfo;
    }

    private void updateCountdown(Text reviewInfo, LocalDateTime nextReview, StringBuilder baseText) {
        // Dùng AtomicReference để giữ đối tượng Timeline có thể được tham chiếu bên trong lambda
        AtomicReference<Timeline> timelineRef = new AtomicReference<>();
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> {
                    LocalDateTime now = LocalDateTime.now();
                    long secondsUntilNext = ChronoUnit.SECONDS.between(now, nextReview);
                    if (secondsUntilNext <= 0) {
                        reviewInfo.setText(baseText.toString());
                        // dừng timeline
                        Timeline tl = timelineRef.get();
                        if (tl != null) tl.stop();
                        // gửi thông báo tray cho số từ cần ôn ngay bây giờ (batchSize bằng baseText số từ)
                        try {
                            // baseText chứa số từ trước chữ " từ sau" hoặc " từ tới"
                            String text = baseText.toString().split(" ")[0];
                            int dueCount = Integer.parseInt(text);
                            notifier.displayNotification(dueCount);
                        } catch (Exception ex) {
                            System.err.println("Không thể gửi thông báo tray: " + ex.getMessage());
                        }
                    } else {
                        long hours = secondsUntilNext / 3600;
                        long remaining = secondsUntilNext % 3600;
                        long minutes = remaining / 60;
                        long seconds = remaining % 60;

                        StringBuilder countdownText = new StringBuilder(baseText.toString());
                        if (hours > 0) countdownText.append(hours).append(" giờ ");
                        if (hours > 0 || minutes > 0) countdownText.append(minutes).append(" phút ");
                        countdownText.append(seconds).append(" giây");
                        reviewInfo.setText(countdownText.toString());
                    }
                })
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timelineRef.set(timeline);
        timeline.play();
    }


//    private void updateCountdown(Text reviewInfo, LocalDateTime nextReview, StringBuilder baseText) {
//        Timeline timeline = new Timeline(
//                new KeyFrame(Duration.seconds(1), event -> {
//                    LocalDateTime now = LocalDateTime.now();
//                    long secondsUntilNext = ChronoUnit.SECONDS.between(now, nextReview);
//                    if (secondsUntilNext <= 0) {
//                        // Khi hết thời gian, dừng timeline và cập nhật lại giao diện
//                        reviewInfo.setText(baseText.toString());
//                        ((Timeline) event.getSource()).stop();
//                    } else {
//                        long hours = secondsUntilNext / 3600; // 1 giờ = 3600 giây
//                        long remainingSeconds = secondsUntilNext % 3600;
//                        long minutes = remainingSeconds / 60;
//                        long seconds = remainingSeconds % 60;
//
//                        StringBuilder countdownText = new StringBuilder(baseText.toString());
//                        if (hours > 0) {
//                            countdownText.append(hours).append(" giờ ");
//                        }
//                        if (hours > 0 || minutes > 0) {
//                            countdownText.append(minutes).append(" phút ");
//                        }
//                        countdownText.append(seconds).append(" giây");
//                        reviewInfo.setText(countdownText.toString());
//                    }
//                })
//        );
//        timeline.setCycleCount(Timeline.INDEFINITE); // Chạy liên tục
//        timeline.play();
//    }

    // phần màu trên cùng của card
    private Rectangle createHeader(AnchorPane card) {
        Rectangle header = new Rectangle();
        header.setHeight(40);
        header.widthProperty().bind(card.widthProperty());
        header.setArcWidth(10);
        header.setArcHeight(10);
        header.setFill(Color.web(getRandomColor()));
        AnchorPane.setTopAnchor(header, 0.0);
        AnchorPane.setLeftAnchor(header, 0.0);
        return header;
    }
    // tiêu đề chính
    private Text createTitle(String text) {
        Text title = new Text(text);
        title.setStyle("-fx-font-size: 25px; -fx-font-weight: bold;");
        title.setWrappingWidth(210);
        AnchorPane.setTopAnchor(title, 60.0);
        AnchorPane.setLeftAnchor(title, 10.0);
        return title;
    }
    // tiêu đề phụ
    private Text createSubtitle(String text) {
        Text subtitle = new Text(text);
        subtitle.setStyle("-fx-font-size: 18px; -fx-fill: #555;");
        AnchorPane.setTopAnchor(subtitle, 90.0);
        AnchorPane.setLeftAnchor(subtitle, 10.0);
        return subtitle;
    }

    private Button createStartButton(boThe deck) {
        Button btn = new Button("Bắt đầu học");
        btn.setMinHeight(30);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-radius:5; -fx-font-size:14px; -fx-text-fill:white; -fx-background-color:" + getContrastButtonColor() + ";");
        DropShadow shadow = new DropShadow(10, 0, 4, Color.rgb(0,0,0,0.3));
        btn.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> btn.setEffect(shadow));
        btn.addEventHandler(MouseEvent.MOUSE_EXITED,  e -> btn.setEffect(null));
        AnchorPane.setLeftAnchor(btn, 20.0);
        AnchorPane.setRightAnchor(btn, 20.0);
        AnchorPane.setBottomAnchor(btn, 15.0);
        btn.setOnAction(e -> loadCard(deck));
        return btn;
    }

    /** Tạo nút “⋮” và context-menu để edit/delete trên card. */
    private Button createOptionsButton(AnchorPane card, boThe deck) {
        Image img = new Image("/3cham.png");
        ImageView view = new ImageView(img);
        view.setFitWidth(17);   // tuỳ chỉnh kích thước icon
        view.setFitHeight(17);

        Button opts = new Button();
        opts.setGraphic(view);
        opts.setStyle("-fx-background-color:transparent; -fx-font-size:18px; -fx-cursor:hand;");
        opts.setPrefSize(30, 30);

        ContextMenu menu = new ContextMenu();
        MenuItem edit = new MenuItem("Chỉnh sửa tên");
        MenuItem del  = new MenuItem("Xoá thẻ");
        MenuItem share = new MenuItem("Chia sẻ");
        menu.getItems().addAll(edit, del,share);

        opts.setOnMouseClicked(e -> menu.show(opts, e.getScreenX(), e.getScreenY()));
        edit.setOnAction(evt -> {
            Optional<String> result = promptForTitle();
            result.ifPresent(newTitle ->{
                boolean ok = botheRequest.updateDeck(deck.getDeckId(),newTitle);
                if(ok){
                    deck.setName(newTitle);
                    loadDecksFromServer();
                }else {
                    System.out.println("lỗi");
                }
            });
        });
        del.setOnAction(evt -> {
            boolean ok = botheRequest.deleteDeck(deck.getDeckId(),statController);
            if(ok){
                cards.remove(deck);
                boTheList.remove(deck);
                loadDecksFromServer(); // Đảm bảo danh sách được đồng bộ
                refreshGrid();
            }else {
                System.out.println("lỗi");
            }
        });
        share.setOnAction(evt -> shareDeckToChat(deck));

        AnchorPane.setTopAnchor(opts, 55.0);
        AnchorPane.setRightAnchor(opts, 5.0);
        return opts;
    }

    private void shareDeckToChat(boThe deck) {
        if (connectionManager == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Chưa kết nối đến phòng chat!", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        try {
            List<Card> cards = CardRequest.getCard(deck.getDeckId());
            String deckData = serializeDeck(deck, cards);
            System.out.println("Serialized deck data: " + deckData);
            Message shareMessage = new Message("SHARE_DECK", userID, currentUser.getUsername(), deckData);
            connectionManager.sendMessage(shareMessage);
            System.out.println("Đã gửi bộ thẻ: " + deck.getName() + " vào phòng chat với " + cards.size() + " thẻ.");
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Lỗi khi chia sẻ bộ thẻ: " + e.getMessage(), ButtonType.OK);
            alert.showAndWait();
        }
    }

    private String serializeDeck(boThe deck, List<Card> cards) {
        StringBuilder sb = new StringBuilder();
        String safeDeckName = deck.getName().replace("|", "_").replace("\n", " ");
        sb.append("DECK|").append(safeDeckName).append("|").append(cards.size()).append("\n");
        for (Card card : cards) {
            String safeEnglish = card.getEnglish_text().replace("|", "_").replace("\n", " ");
            String safeVietnamese = card.getVietnamese_text().replace("|", "_").replace("\n", " ");
            sb.append("CARD|").append(safeEnglish).append("|").append(safeVietnamese).append("\n");
        }
        String result = sb.toString();
        System.out.println("Serialized deck: " + result);
        return result;
    }


    private void refreshGrid() {
        cardGrid.getChildren().clear();
        row = 0; col = 0;
        for (boThe c : cards) {
            AnchorPane card = buildCard(c);
            cardGrid.add(card, col, row);
            if (++col >= 3) {
                col = 0; row++;
            }
        }
    }

    private void refreshGridSearch() {
        cardGrid.getChildren().clear();
        row = 0; col = 0;
        for (boThe c : filteredList) {
            AnchorPane card = buildCard(c);
            cardGrid.add(card, col, row);
            if (++col >= 3) {
                col = 0; row++;
            }
        }
    }

    public void loadDecksFromServer() {
        List<boThe> decks = botheRequest.getDecks(userID);
        boTheList.setAll(decks);
        this.cards = decks;
        refreshGrid();
    }




    // Hàm giả lập trả về màu header (bạn có thể thay bằng mảng màu cố định)
    private String getRandomColor() {
        String[] colors = { "#85C1E9", // xanh dương nhạt
                "#EC7063", // đỏ hồng
                "#58D68D", // xanh lá
                "#8E44AD", // tím
                "#F5B041", // cam nhạt
                "#5DADE2", // xanh nước biển
                "#45B39D", // xanh ngọc
                "#AF7AC5"  // tím nhạt
        };
        int idx = (row*3 + col) % colors.length;
        return colors[idx];
    }

    // Lấy màu button tương phản (ví dụ: tông đậm của header)
    private String getContrastButtonColor() {
        // Chỉ lấy lại chính header cho đơn giản, hoặc hardcode tông đậm hơn
        String[] btnColors = { "#5499C7", // xanh dương đậm
                "#C0392B", // đỏ đậm
                "#28B463", // xanh lá đậm
                "#6C3483", // tím đậm
                "#D68910", // cam đậm
                "#2E86C1", // xanh đậm hơn
                "#1ABC9C", // xanh ngọc đậm
                "#884EA0"
        };
        int idx = (row*3 + col) % btnColors.length;
        return btnColors[idx];
    }

    @FXML
    public void handleChoose() {
        if (isLeftVisible) {
            mainPane.setLeft(null);
        } else {
            mainPane.setLeft(leftPane);
        }
        isLeftVisible = !isLeftVisible;
    }

    private void loadCard(boThe deckTitle){
        try {
            FXMLLoader fxmlCenter = new FXMLLoader(getClass().getResource("/FlashCard/card_center.fxml"));
            Parent center = fxmlCenter.load();
            FlashCard.card_centerController c = fxmlCenter.getController();
            c.setBoThe_id(deckTitle.getDeckId());
            c.setController(this);
            mainPane.setCenter(center);

            FXMLLoader fxmlBottom = new FXMLLoader(getClass().getResource("/FlashCard/card_bottom.fxml"));
            Parent bottom = fxmlBottom.load();
            card_bottomController card_bottom = fxmlBottom.getController();
                card_bottom.setParent(this);
                card_bottom.setContext(mainPane,deckTitle);
            mainPane.setBottom(bottom);

            FXMLLoader topLoader = new FXMLLoader(getClass().getResource("/FlashCard/card_top.fxml"));
            Parent topPane = topLoader.load();
            // Lấy controller của top và gọi setter để gán tiêu đề
            card_topController topCtrl = topLoader.getController();
            topCtrl.setParentController(this);
            topCtrl.setDeckTitle("Bộ thẻ : "+deckTitle.getName());
            mainPane.setTop(topPane);

            if(readCenterController!=null){
                readCenterController.closeResources();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void restoreHome() {
        mainPane.setCenter(centerPane);
        mainPane.setBottom(bottomPane);
        mainPane.setTop(topPane);

        loadDecksFromServer();
        refreshGridSearch();

        if(readCenterController!=null){
            readCenterController.closeResources();
        }
    }


    @FXML
    private void handleUser() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FlashCard/ProfileDialog.fxml"));
            Parent root = loader.load();
            ProfileDialogController controller = loader.getController();
            if (currentUser != null) {
                controller.setUser(currentUser, userID);
            }
            Stage dialog     = new Stage();
            dialog.setTitle("Thông tin người dùng");
            dialog.initOwner(userButton.getScene().getWindow());
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));
            dialog.setResizable(false);
            dialog.showAndWait();

            if(readCenterController!=null){
                readCenterController.closeResources();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handlTC() {
        mainPane.setCenter(centerPane);
        mainPane.setBottom(bottomPane);
        mainPane.setTop(topPane);
        Label userLable = (Label) topPane.getCenter();
        if (userLable!= null) userLable.setText("FlashCard");

        loadDecksFromServer();
        refreshGridSearch();

        if(readCenterController!=null){
            readCenterController.closeResources();
        }
    }

    @FXML
    private void handlStat(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/statistical/stat.fxml"));
            Parent root = loader.load();
            statController statCtrl = loader.getController();
            statCtrl.setUserId(userID); // Đặt userId trước khi hiển thị

            Label userLable = (Label) topPane.getCenter();
            if (userLable!= null) userLable.setText("FlashCard");
            mainPane.setTop(topPane);
            mainPane.setCenter(root);
            mainPane.setBottom(null);

            if(readCenterController!=null){
                readCenterController.closeResources();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void openChatRoom() {
        try {
            // Load giao diện nếu chưa có
            if (chatCenterPane == null || chatBottomPane == null) {
                // Load ChatCenter
                FXMLLoader centerLoader = new FXMLLoader(getClass().getResource("/Chat/chat_center.fxml"));
                chatCenterPane = centerLoader.load();
                chatCenterCtrl = centerLoader.getController();
                chatCenterCtrl.setCurrentUserId(userID);

                // Load ChatBottom
                FXMLLoader bottomLoader = new FXMLLoader(getClass().getResource("/Chat/chat_bottom.fxml"));
                chatBottomPane = bottomLoader.load();
                chatBottomCtrl = bottomLoader.getController();
                chatBottomCtrl.setUserInfo(userID, currentUser.getUsername());
            }

            // Khởi tạo hoặc cập nhật kết nối chat
            if (connectionManager == null) {
                connectionManager = new ChatConnectionManager("localhost", 1234, userID, chatCenterCtrl);
                System.out.println("Khởi tạo ChatConnectionManager cho userId: " + userID);
            }

            // Gán connectionManager cho cả hai controller
            chatCenterCtrl.setConnectionManager(connectionManager);
            chatBottomCtrl.setConnectionManager(connectionManager);
            System.out.println("Đã gán connectionManager cho ChatCenterController và ChatBottomController");

            Label userLable = (Label) topPane.getCenter();
            if (userLable!= null) userLable.setText("Chào mừng "+currentUser.getUsername() + " đến phòng Chat");

            // Đặt giao diện chat
            mainPane.setTop(topPane);
            mainPane.setCenter(chatCenterPane);
            mainPane.setBottom(chatBottomPane);

            // Khôi phục lịch sử tin nhắn
            if (chatCenterCtrl != null) {
                chatCenterCtrl.restoreMessages();
            }

            if(readCenterController!=null){
                readCenterController.closeResources();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Không thể mở phòng chat: " + e.getMessage(), ButtonType.OK);
            alert.showAndWait();
        }
    }

    @FXML
    private void Exit() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận thoát");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có chắc chắn muốn thoát không?");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            Platform.exit();  // Thoát JavaFX
            System.exit(0);   // Dừng JVM (tuỳ chọn)
        }
    }


}

