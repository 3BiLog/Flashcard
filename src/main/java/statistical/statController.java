package statistical;

import OOP.Card;
import OOP.Stat;
import FlashCard.CardRequest;
import FlashCard.botheRequest;
import OOP.boThe;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class statController {
    @FXML private DatePicker calendar;
    @FXML private Label lblDate;
    @FXML private Label lblCount;
    @FXML private BarChart<String, Number> barChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private Button pieChartButton;
    @FXML private AnchorPane anchorPane;

    private PieChart pieChart;
    private boolean isPieChartVisible = false;
    private int userId;
    private StatsRequest statRequest = new StatsRequest();
    private botheRequest botheRequest = new botheRequest();
    private List<Stat> stats = new ArrayList<>();

    public void setUserId(int userId) {
        this.userId = userId;
        loadDataAndInitialize();
    }

    private void loadDataAndInitialize() {
        boolean syncSuccess = statRequest.syncStats();
        System.out.println("Sync stats result: " + (syncSuccess ? "Success" : "Failed"));

        stats = statRequest.getStats(userId);
        System.out.println("Loaded stats: " + stats);

        initializeUI();
    }

    private void initializeUI() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        calendar.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return (date != null) ? fmt.format(date) : "";
            }
            @Override
            public LocalDate fromString(String str) {
                return (str != null && !str.isEmpty())
                        ? LocalDate.parse(str, fmt)
                        : null;
            }
        });

        calendar.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setStyle("");
                } else {
                    boolean has = stats.stream()
                            .anyMatch(s -> date.equals(s.getDate()) && s.getCount() > 0);
                    setStyle(has
                            ? "-fx-background-color: #FF6B6B; -fx-text-fill: black;"
                            : "-fx-background-color: #D3D3D3; -fx-text-fill: black;");
                }
            }
        });

        calendar.valueProperty().addListener((o, oldV, newV) -> {
            if (newV != null) {
                updateStats(newV);
                if (!isPieChartVisible) {
                    updateBarChart(newV);
                }
            }
        });

        if (barChart != null) {
            xAxis.setLabel("Ngày");
            yAxis.setTickLabelsVisible(false);
            yAxis.setTickMarkVisible(false);
            yAxis.setMinorTickVisible(false);
            yAxis.setLabel("");
            barChart.setLegendVisible(false);
            barChart.setAnimated(false);
        }

        // Khởi tạo PieChart
        pieChart = new PieChart();
        pieChart.setPrefWidth(1000);
        pieChart.setPrefHeight(300);
        pieChart.setLegendVisible(true);
        pieChart.setAnimated(false);
        pieChart.setLabelsVisible(true); // Có thể đặt false nếu không muốn nhãn
        AnchorPane.setLeftAnchor(pieChart, 10.0);
        AnchorPane.setRightAnchor(pieChart, 10.0);
        AnchorPane.setTopAnchor(pieChart, 100.0);
        AnchorPane.setBottomAnchor(pieChart, 20.0);

        if (calendar.getValue() == null) {
            calendar.setValue(LocalDate.now());
        }
        updateStats(calendar.getValue());
        updateBarChart(calendar.getValue());
    }

    private void updateStats(LocalDate date) {
        stats = statRequest.getStats(userId);
        Stat st = statRequest.getStatByDate(userId, date);
        lblDate.setText("Ngày: " + date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        lblCount.setText("Số từ ôn: " + (st != null ? st.getCount() : 0));
    }

    private void updateBarChart(LocalDate selectedDate) {
        if (barChart == null || isPieChartVisible) return;

        // Xóa dữ liệu cũ
        barChart.getData().clear();

        // Lấy dữ liệu mới
        stats = statRequest.getStats(userId);
        LocalDate start = selectedDate.minusDays(3);
        LocalDate end = selectedDate.plusDays(3);
        Map<LocalDate, Integer> map = stats.stream()
                .filter(s -> s.getDate() != null)
                .collect(Collectors.toMap(Stat::getDate, Stat::getCount));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        DateTimeFormatter sf = DateTimeFormatter.ofPattern("dd-MM");
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            series.getData().add(new XYChart.Data<>(d.format(sf), map.getOrDefault(d, 0)));
        }
        barChart.getData().add(series);

        // Thêm nhãn sau khi layout xong
        Platform.runLater(() -> {
            Node chartPlot = barChart.lookup(".chart-plot-background");
            Pane plotArea = (chartPlot != null && chartPlot.getParent() instanceof Pane)
                    ? (Pane) chartPlot.getParent() : null;

            // Xóa nhãn cũ
            if (plotArea != null) {
                plotArea.getChildren().removeIf(node -> node instanceof Text && node.getStyleClass().contains("bar-label"));
            }

            // Thêm nhãn mới
            for (XYChart.Data<String, Number> data : series.getData()) {
                // Tô màu thanh
                LocalDate d = LocalDate.parse(
                        data.getXValue() + "-" + selectedDate.getYear(),
                        DateTimeFormatter.ofPattern("dd-MM-yyyy")
                );
                String col = d.equals(selectedDate) ? "#FF6B6B" : "#85C1E9";
                Node barNode = data.getNode();
                if (barNode != null) {
                    barNode.setStyle("-fx-bar-fill: " + col + ";");
                }

                // Thêm nhãn chỉ khi giá trị > 0 và node tồn tại
                if (plotArea != null && data.getYValue().intValue() > 0 && barNode != null) {
                    Text label = new Text(data.getYValue().intValue() + "");
                    label.getStyleClass().add("bar-label");
                    plotArea.getChildren().add(label);

                    // Định vị nhãn
                    barNode.boundsInParentProperty().addListener((obs, oldBounds, newBounds) -> {
                        double x = newBounds.getMinX() + newBounds.getWidth() / 2 - label.prefWidth(-1) / 2;
                        double y = newBounds.getMinY() - label.prefHeight(-1) - 2;
                        label.relocate(x, y);
                    });

                    // Cập nhật vị trí nhãn ngay lập tức
                    if (barNode.getBoundsInParent() != null) {
                        double x = barNode.getBoundsInParent().getMinX() + barNode.getBoundsInParent().getWidth() / 2 - label.prefWidth(-1) / 2;
                        double y = barNode.getBoundsInParent().getMinY() - label.prefHeight(-1) - 2;
                        label.relocate(x, y);
                    }
                }
            }
        });
    }

    @FXML
    private void showPieChart() {
        isPieChartVisible = !isPieChartVisible;
        pieChartButton.setText(isPieChartVisible ? "Xem Biểu Đồ Cột" : "Xem Biểu Đồ Tròn");

        if (anchorPane == null) {
            System.err.println("AnchorPane is null, cannot switch charts.");
            return;
        }

        // Xóa tất cả các node biểu đồ khỏi AnchorPane
        anchorPane.getChildren().removeAll(barChart, pieChart);

        if (isPieChartVisible) {
            // Thêm PieChart
            anchorPane.getChildren().add(pieChart);
            updatePieChart();
        } else {
            // Thêm BarChart
            anchorPane.getChildren().add(barChart);
            updateStats(calendar.getValue());
            updateBarChart(calendar.getValue());
            updatePieChart();
        }

        // Debug số lượng node
        System.out.println("Số node trong AnchorPane: " + anchorPane.getChildren().size());
    }

    private void updatePieChart() {
        // Xóa dữ liệu cũ và làm sạch PieChart
        pieChart.getData().clear();
        pieChart.getChildrenUnmodifiable().forEach(node -> {
            if (node instanceof Text && node.getStyleClass().contains("chart-pie-label")) {
                pieChart.getChildrenUnmodifiable().remove(node);
            }
        });

        // Lấy dữ liệu
        List<boThe> decks = botheRequest.getDecks(userId);
        List<Card> allCards = new ArrayList<>();
        for (boThe deck : decks) {
            allCards.addAll(CardRequest.getCard(deck.getDeckId()));
        }

        // Đếm số thẻ theo memory_level (0–5)
        Map<Integer, Long> memoryLevelCounts = allCards.stream()
                .collect(Collectors.groupingBy(Card::getMemoryLevel, Collectors.counting()));

        // Tạo dữ liệu cho PieChart
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        String[] colors = { "#FF0000", "#FF4500", "#FFA500", "#80ff80", "#008000", "#0000CD" };
        for (int level = 0; level <= 5; level++) {
            long count = memoryLevelCounts.getOrDefault(level, 0L);
            PieChart.Data data = new PieChart.Data("Mức " + level + " (" + count + " từ)", count);
            pieChartData.add(data);
        }


        // Cập nhật PieChart
        pieChart.setData(pieChartData);

// Gán màu từng lát cắt
        Platform.runLater(() -> {
            pieChart.applyCss();
            pieChart.layout();
            int colorIndex = 0;
            for (PieChart.Data data : pieChart.getData()) {
                if (colorIndex < colors.length) {
                    data.getNode().setStyle("-fx-pie-color: " + colors[colorIndex++] + ";");
                }
            }

            // Style nhãn
            for (Node node : pieChart.lookupAll(".chart-pie-label")) {
                if (node instanceof Text) {
                    Text label = (Text) node;
                    label.setStyle("-fx-font-size: 12; -fx-fill: black;");
                }
            }
        });
    }

    public void refreshStats() {
        boolean ok = statRequest.syncStats();
        System.out.println("Refresh Stats: " + (ok ? "Success" : "Failed"));
        stats = statRequest.getStats(userId);
        updateStats(calendar.getValue());
        if (!isPieChartVisible) {
            updateBarChart(calendar.getValue());
        } else {
            updatePieChart();
        }
    }
}