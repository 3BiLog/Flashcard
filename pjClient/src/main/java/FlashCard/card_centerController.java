package FlashCard;


import OOP.Card;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import statistical.statController;


import javax.sound.sampled.AudioInputStream;


public class card_centerController {
    @FXML private GridPane gridPaneCard;
    @FXML private ScrollPane scroll;

    private List<Card> Vocabulary = new ArrayList<>();
    private int boThe_id;

    private ObservableList<Card> masterList = FXCollections.observableArrayList();
    private FilteredList<Card> filteredList;

    //load lại bộ thẻ theo id
    public void setBoThe_id(int boThe_id){
        this.boThe_id = boThe_id;
        loadCardFromServer();
    }

    private int col = 0, row = 0;

    // ★ singleton instance để các controller khác gọi
    private static card_centerController instance;
    private trangChuController controller;

    public void setController(trangChuController parent){
        this.controller = parent;
    }

    public card_centerController() {
        instance = this;
    }

    public static card_centerController getInstance() {
        return instance;
    }

    private statController statController;

    @FXML
    public void initialize() {
        filteredList = new FilteredList<>(masterList,c->true);

        gridPaneCard.prefWidthProperty().bind(
                scroll.widthProperty().subtract(scroll.getPadding().getLeft() + scroll.getPadding().getRight())
        );
    }

    /** public API để thêm 1 card mới */
    public void addVocabulary(String anh,String viet ) {
        Card created = CardRequest.addCard(boThe_id,anh,viet);
        if(created !=null){
//            Vocabulary.add(created);
            masterList.add(created);
            refreshGridSearch();
//            refreshGrid();
            if (controller != null){
                controller.loadDecksFromServer();
            }
        }else {
            System.out.println("thêm card thất bại");
        }
    }

    private AnchorPane buildCard(Card c) {
        AnchorPane card = new AnchorPane();
        card.setPrefHeight(180);
        card.setMinWidth(0);
        card.setMaxWidth(Double.MAX_VALUE);
        GridPane.setFillWidth(card, true);
        card.setStyle(
                "-fx-background-color: #f0f8ff; " +
                        "-fx-background-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2); " +
                        "-fx-transition: all 0.3s ease; " +
                        "-fx-cursor: hand;"
        );

// Add hover effect
        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: #f0f8ff; " +
                        "-fx-background-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 12, 0, 0, 3); " +
                        "-fx-scale-x: 1.02; " +
                        "-fx-scale-y: 1.02; " +
                        "-fx-transition: all 0.3s ease; " +
                        "-fx-cursor: hand;"
        ));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: #f0f8ff; " + // Added semicolon here
                        "-fx-background-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2); " +
                        "-fx-transition: all 0.3s ease; " +
                        "-fx-cursor: hand;"
        ));

        Rectangle header   = createHeader(card);
        Circle memoryCircle = createMemoryCircle(c.getMemoryLevel()); // Thêm vòng tròn độ nhớ
        Text memoryText = createMemoryText(c.getMemoryLevel());
        Text title         = createTitle(c.getEnglish_text());
        Text subtitle      = createSubtitle(c.getVietnamese_text());
        Button button = createOptionsButton(c,title,subtitle);
        Button speak = createSpeakButton(c);

        card.getChildren().addAll(header, title, subtitle,button,speak,memoryCircle,memoryText);
        return card;
    }

    private Circle createMemoryCircle(int memoryLevel) {
        Circle circle = new Circle(15); // Bán kính 15
//        circle.setStroke(Color.WHITE);
//        circle.setStrokeWidth(1);

        // Màu sắc dựa trên memoryLevel (tương tự 4English)
        String color;
        switch (memoryLevel) {
            case 0: color = "#FF0000"; break; // Đỏ - chưa nhớ
            case 1: color = "#FF4500"; break; // Cam đậm
            case 2: color = "#FFA500"; break; // Cam nhạt
            case 3: color = "#80ff80"; break; // Vàng
            case 4: color = "#008000"; break; // Xanh nhạt
            case 5: color = "#0000CD"; break; // Xanh đậm - nhớ tốt
            default: color = ""; break;
        }
        circle.setFill(Color.web(color));

        AnchorPane.setTopAnchor(circle, 7.0);
        AnchorPane.setLeftAnchor(circle, 10.0);
        return circle;
    }

    private Text createMemoryText(int memoryLevel) {
        Text text = new Text(String.valueOf(memoryLevel));
        text.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        text.setFill(Color.WHITE);
        AnchorPane.setTopAnchor(text, 14.0);
        AnchorPane.setLeftAnchor(text, 22.0);
        return text;
    }

    private Rectangle createHeader(AnchorPane card) {
        Rectangle r = new Rectangle(0,0,0,40);
        r.widthProperty().bind(card.widthProperty());
        r.setArcWidth(10);
        r.setArcHeight(10);
        r.setFill(Color.web(getRandomColor()));
        AnchorPane.setTopAnchor(r, 0.0);
        AnchorPane.setLeftAnchor(r, 0.0);
        return r;
    }
    private Text createTitle(String text) {
        Text t = new Text(text);
        t.setStyle("-fx-font-size:23px;-fx-font-weight:bold;");
        t.setWrappingWidth(210);
        AnchorPane.setTopAnchor(t, 60.0);
        AnchorPane.setLeftAnchor(t,10.0);
        return t;
    }
    private Text createSubtitle(String text) {
        Text t = new Text(text);
        t.setStyle("-fx-font-size:23px;-fx-fill:#555;");
        AnchorPane.setTopAnchor(t,100.0);
        AnchorPane.setLeftAnchor(t,10.0);
        return t;
    }

    private Button createOptionsButton(Card card, Text titleText, Text subtitleText) {
        Image img = new Image("/3cham.png");
        ImageView icon = new ImageView(img);
        icon.setFitWidth(17);
        icon.setFitHeight(17);

        Button opts = new Button();
        opts.setGraphic(icon);
        opts.setStyle("-fx-background-color:transparent; -fx-cursor:hand;");
        opts.setPrefSize(30, 30);

        ContextMenu menu = new ContextMenu();
        MenuItem editItem = new MenuItem("Chỉnh sửa");
        MenuItem delItem  = new MenuItem("Xóa thẻ");
        editItem.setStyle("-fx-pref-width: 150px; -fx-font-size: 14px;");
        delItem.setStyle("-fx-pref-width: 150px; -fx-font-size: 14px;");

        menu.getItems().addAll(editItem, delItem);

        // Hiển thị menu khi click
        opts.setOnMouseClicked(e -> menu.show(opts, e.getScreenX(), e.getScreenY()));

        // Xử lý edit
        editItem.setOnAction(evt -> {
            Dialog<Pair<String, String>> dlg = new Dialog<>();
            dlg.setTitle("Chỉnh sửa từ");
            dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20));

            TextField tfAnh  = new TextField(titleText.getText());
            tfAnh.setPromptText("Tiếng Anh");
            TextField tfViet = new TextField(subtitleText.getText());
            tfViet.setPromptText("Tiếng Việt");

            grid.add(new Label("Tiếng Anh:"), 0, 0);
            grid.add(tfAnh, 1, 0);
            grid.add(new Label("Tiếng Việt:"),  0, 1);
            grid.add(tfViet,  1, 1);
            dlg.getDialogPane().setContent(grid);

            dlg.setResultConverter(btn -> btn == ButtonType.OK
                    ? new Pair<>(tfAnh.getText().trim(), tfViet.getText().trim())
                    : null);

            Optional<Pair<String, String>> res = dlg.showAndWait();
            res.ifPresent(pair -> {
                boolean ok = CardRequest.updateCard(card.getId(),pair.getKey(),pair.getValue());
                if (ok) {
                    card.setEnglish_text(pair.getKey());
                    card.setVietnamese_text(pair.getValue());
                    titleText.setText(pair.getKey());
                    subtitleText.setText(pair.getValue());

                    AnchorPane parentPane = (AnchorPane) opts.getParent();
                    // Find the speak button by ID
                    Button speakButton = (Button) parentPane.lookup("#speakButton_" + card.getId());
                    if (speakButton != null) {
                        // Update the speak button's action
                        speakButton.setOnAction(e -> speakText(pair.getKey()));
                    }
                }
            });
        });

        // Xử lý delete
        delItem.setOnAction(evt -> {
            boolean ok = CardRequest.deleteCard(card.getId(),statController);
            if (ok){
                Vocabulary.remove(card);
                refreshGrid();
                if(controller !=null){
                    controller.loadDecksFromServer();
                    refreshGrid();
                }
            }

        });
        AnchorPane.setTopAnchor(opts, 55.0);
        AnchorPane.setRightAnchor(opts, 5.0);
        return opts;
    }

    private Button createSpeakButton(Card card) { // Modified to take Card instead of String
        Image image = new Image("/speaker.png");
        ImageView icon = new ImageView(image);
        icon.setFitWidth(17);
        icon.setFitHeight(17);

        Button speakBtn = new Button();
        speakBtn.setGraphic(icon);
        speakBtn.setStyle("-fx-background-color:transparent; -fx-cursor:hand; -fx-font-size: 16px;");
        speakBtn.setPrefSize(30, 30);

        // Set ID for easy lookup
        speakBtn.setId("speakButton_" + card.getId());

        // Set initial action
        speakBtn.setOnAction(e -> speakText(card.getEnglish_text()));

        AnchorPane.setTopAnchor(speakBtn, 75.0);
        AnchorPane.setRightAnchor(speakBtn, 5.0);
        return speakBtn;
    }

    private void speakText(String text) {
        System.setProperty("freetts.voices",
                "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
        VoiceManager vm = VoiceManager.getInstance();
        Voice voice = vm.getVoice("kevin");
        if (voice != null) {
            voice.allocate();
            voice.speak(text);
            voice.deallocate();
        } else {
            System.err.println("Voice 'kevin' not found.");
        }
    }

    private String getRandomColor() {
        String[] colors = {
                "#85C1E9","#EC7063","#58D68D","#8E44AD",
                "#F5B041","#5DADE2","#45B39D","#AF7AC5"
        };
        int idx = (row*4 + col) % colors.length;
        return colors[idx];
    }

    private void refreshGrid() {
        gridPaneCard.getChildren().clear();
        row = col = 0;
        for (Card c : Vocabulary) {
            AnchorPane pane = buildCard(c);
            gridPaneCard.add(pane, col, row);
            if (++col >= 4) { col = 0; row++; }
        }
    }

    public void setFilter(String filter) {
        String f = filter.toLowerCase().trim();
        if (f.isEmpty()) {
            filteredList.setPredicate(c -> true);
        } else {
            filteredList.setPredicate(c ->
                    c.getEnglish_text().toLowerCase().contains(f) ||
                            c.getVietnamese_text().toLowerCase().contains(f)
            );
        }
        refreshGridSearch();
    }

    private void refreshGridSearch() {
        gridPaneCard.getChildren().clear();
        row = col = 0;
        for (Card c : filteredList) {
            AnchorPane pane = buildCard(c);
            gridPaneCard.add(pane, col, row);
            if (++col >= 4) { col = 0; row++; }
        }
    }

    public void loadCardFromServer(){
        List<Card> cards = CardRequest.getCard(boThe_id);
        masterList.setAll(cards);
        this.Vocabulary = cards;
        refreshGrid();
    }
}
