package org.example;

import java.util.function.Function;
import java.util.stream.Stream;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {

    private Slider priceSlider;
    private Slider tempSlider;
    private Slider adsSlider;
    private Label resultLabel;

    private LineChart<Number, Number> chart;
    private XYChart.Series<Number, Number> aggregatedSeries;
    private XYChart.Series<Number, Number> centroidSeries;

    // --- 1. ОПРЕДЕЛЕНИЕ ФУНКЦИЙ ПРИНАДЛЕЖНОСТИ (через Function) ---
    // Вход: Цена (0 - 100)[cite: 7]
    private final Function<Double, Double> priceLow = trapmf(0, 0, 30, 50);
    private final Function<Double, Double> priceMed = trimf(30, 50, 70);
    private final Function<Double, Double> priceHigh = trapmf(50, 70, 100, 100);

    // Вход: Температура (-10 - 40)[cite: 7]
    private final Function<Double, Double> tempLow = trapmf(-10, -10, 10, 25);
    private final Function<Double, Double> tempHigh = trapmf(15, 25, 40, 40);

    // Вход: Рекламная активность (0 - 100)[cite: 7]
    private final Function<Double, Double> adsLow = trapmf(0, 0, 40, 60);
    private final Function<Double, Double> adsHigh = trapmf(40, 60, 100, 100);

    // Выход: Спрос (0 - 100)[cite: 7]
    private final Function<Double, Double> demandLow = trapmf(0, 0, 30, 50);
    private final Function<Double, Double> demandMed = trimf(30, 50, 70);
    private final Function<Double, Double> demandHigh = trapmf(
        50,
        70,
        100,
        100
    );

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        priceSlider = createSlider(0, 100, 50);
        tempSlider = createSlider(-10, 40, 20);
        adsSlider = createSlider(0, 100, 50);

        resultLabel = new Label("Спрос: расчет...");
        resultLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        VBox controls = new VBox(
            10,
            new Label("Цена (0-100):"),
            priceSlider,
            new Label("Температура (-10..40):"),
            tempSlider,
            new Label("Рекламная активность (0-100):"),
            adsSlider,
            resultLabel
        );
        controls.setPadding(new Insets(15));
        controls.setPrefWidth(250);

        chart = new LineChart<>(
            new NumberAxis(0, 100, 10),
            new NumberAxis(0, 1.1, 0.1)
        );
        chart.setCreateSymbols(false);
        chart.setAnimated(false);
        chart.setTitle("Агрегированное множество и Дефаззификация");

        aggregatedSeries = new XYChart.Series<>();
        aggregatedSeries.setName("Функция спроса");
        centroidSeries = new XYChart.Series<>();
        centroidSeries.setName("Центр тяжести");

        chart.getData().addAll(aggregatedSeries, centroidSeries);

        priceSlider
            .valueProperty()
            .addListener((obs, o, n) -> calculateAndDraw());
        tempSlider
            .valueProperty()
            .addListener((obs, o, n) -> calculateAndDraw());
        adsSlider
            .valueProperty()
            .addListener((obs, o, n) -> calculateAndDraw());

        BorderPane root = new BorderPane();
        root.setLeft(controls);
        root.setCenter(chart);

        primaryStage.setTitle("Нечеткая логика: Прогнозирование спроса ");
        primaryStage.setScene(new Scene(root, 900, 500));
        primaryStage.show();

        calculateAndDraw();
    }

    private void calculateAndDraw() {
        double p = priceSlider.getValue();
        double t = tempSlider.getValue();
        double a = adsSlider.getValue();

        // --- 2. ФАЗЗИФИКАЦИЯ (вызываем .apply()) ---
        double pL = priceLow.apply(p);
        double pM = priceMed.apply(p);
        double pH = priceHigh.apply(p);

        double tL = tempLow.apply(t);
        double tH = tempHigh.apply(t);

        double aL = adsLow.apply(a);
        double aH = adsHigh.apply(a);

        // --- 3. АКТИВИЗАЦИЯ ПРАВИЛ ---
        double rule1 = Math.min(pL, aH);
        double rule2 = Math.min(pH, aL);
        double rule3 = Math.min(pM, aH);
        double rule4 = Math.min(pM, aL);
        double rule5 = Math.min(pH, tL);
        double rule6 = Math.min(pL, tH);
        double rule7 = Math.min(pH, aH);
        double rule8 = Math.min(pL, aL);

        // --- 4. ИМПЛИКАЦИЯ (API-магия через .andThen) ---
        // Берем базовую функцию терма и передаем ее результат дальше в лямбду обрезки
        Function<Double, Double> imp1 = demandHigh.andThen(val ->
            Math.min(rule1, val)
        );
        Function<Double, Double> imp2 = demandLow.andThen(val ->
            Math.min(rule2, val)
        );
        Function<Double, Double> imp3 = demandHigh.andThen(val ->
            Math.min(rule3, val)
        );
        Function<Double, Double> imp4 = demandMed.andThen(val ->
            Math.min(rule4, val)
        );
        Function<Double, Double> imp5 = demandLow.andThen(val ->
            Math.min(rule5, val)
        );
        Function<Double, Double> imp6 = demandHigh.andThen(val ->
            Math.min(rule6, val)
        );
        Function<Double, Double> imp7 = demandMed.andThen(val ->
            Math.min(rule7, val)
        );
        Function<Double, Double> imp8 = demandMed.andThen(val ->
            Math.min(rule8, val)
        );

        // --- 5. АГРЕГАЦИЯ (Через Stream.reduce) ---
        // Сводим массив функций в одну супер-функцию, вычисляющую максимум на лету
        Function<Double, Double> aggregatedMf = Stream.of(
            imp1,
            imp2,
            imp3,
            imp4,
            imp5,
            imp6,
            imp7,
            imp8
        )
            .reduce((f1, f2) -> y -> Math.max(f1.apply(y), f2.apply(y)))
            .orElse(y -> 0.0);

        // --- 6. ДЕФАЗЗИФИКАЦИЯ ---
        double outMin = 0,
            outMax = 100;
        int steps = 200;
        double stepSize = (outMax - outMin) / steps;

        double num = 0.0;
        double den = 0.0;

        aggregatedSeries.getData().clear();

        for (int i = 0; i <= steps; i++) {
            double currentY = outMin + i * stepSize;
            double mu = aggregatedMf.apply(currentY);

            num += currentY * mu;
            den += mu;

            aggregatedSeries.getData().add(new XYChart.Data<>(currentY, mu));
        }

        double centroid = den == 0.0 ? (outMax - outMin) / 2.0 : num / den;

        String interpretation =
            centroid < 40 ? "Низкий" : centroid > 60 ? "Высокий" : "Средний";
        resultLabel.setText(
            String.format("Спрос: %.2f (%s)", centroid, interpretation)
        );

        centroidSeries.getData().clear();
        centroidSeries.getData().add(new XYChart.Data<>(centroid, 0));
        centroidSeries
            .getData()
            .add(new XYChart.Data<>(centroid, aggregatedMf.apply(centroid)));
    }

    private Slider createSlider(double min, double max, double value) {
        Slider slider = new Slider(min, max, value);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        return slider;
    }

    // Треугольная функция принадлежности
    private Function<Double, Double> trimf(double a, double b, double c) {
        return x -> {
            double left = a == b ? (x >= a ? 1.0 : 0.0) : (x - a) / (b - a);
            double right = b == c ? (x <= c ? 1.0 : 0.0) : (c - x) / (c - b);
            return Math.max(0.0, Math.min(left, right));
        };
    }

    // Трапециевидная функция принадлежности
    private Function<Double, Double> trapmf(
        double a,
        double b,
        double c,
        double d
    ) {
        return x -> {
            // Если левый или правый край вертикальный (a == b или c == d), деления на ноль не происходит
            double left = a == b ? (x >= a ? 1.0 : 0.0) : (x - a) / (b - a);
            double right = c == d ? (x <= d ? 1.0 : 0.0) : (d - x) / (d - c);

            return Math.max(0.0, Math.min(Math.min(left, 1.0), right));
        };
    }
}
