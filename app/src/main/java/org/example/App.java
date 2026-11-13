package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        // 1. Настройка осей
        NumberAxis xAxis = new NumberAxis(0, 10, 1); // от 0 до 10 с шагом 1
        xAxis.setLabel("Время (x)");

        NumberAxis yAxis = new NumberAxis(-1.2, 1.2, 0.5);
        yAxis.setLabel("Амплитуда sin(x * freq)");

        // 2. График
        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setAnimated(false);
        chart.setCreateSymbols(false); // убираем кружочки на каждой точке, оставляем гладкую линию
        // отменяем анимацию, что бы не вызыватьанимацию при каждом изменении, что может приветси к лагам
        chart.setLegendVisible(false);

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        chart.getData().add(series);

        // 3. Ползунок с делениями
        Slider freqSlider = new Slider(1, 10, 2);
        freqSlider.setShowTickMarks(true);
        freqSlider.setShowTickLabels(true);
        freqSlider.setMajorTickUnit(1);
        HBox.setHgrow(freqSlider, Priority.ALWAYS); // растягиваем слайдер по ширине

        // Живой текст, который обновляется сам
        Label valueLabel = new Label(
            String.format("Частота: %.2f", freqSlider.getValue())
        );
        valueLabel.setMinWidth(100);

        // 4. Реакция на движение ползунка
        freqSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double freq = newVal.doubleValue();
            valueLabel.setText(String.format("Частота: %.2f", freq));
            updateData(series, freq);
        });

        // Первичная отрисовка при старте
        updateData(series, freqSlider.getValue());

        // 5. Компоновка: панель управления снизу (HBox) + общий вертикальный контейнер (VBox)
        HBox controls = new HBox(15, valueLabel, freqSlider);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(10));

        VBox root = new VBox(10, chart, controls);
        VBox.setVgrow(chart, Priority.ALWAYS); // график занимает всё свободное место при растяжении окна

        stage.setScene(new Scene(root, 700, 450));
        stage.setTitle("JavaFX Interactive Graph");
        stage.show();
    }

    private void updateData(
        XYChart.Series<Number, Number> series,
        double freq
    ) {
        series.getData().clear();
        for (double x = 0; x <= 10; x += 0.05) {
            series.getData().add(new XYChart.Data<>(x, Math.sin(x * freq)));
        }
    }
}
