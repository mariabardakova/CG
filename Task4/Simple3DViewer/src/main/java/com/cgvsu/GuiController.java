package com.cgvsu;

import com.cgvsu.objwriter.ObjWriter;
import com.cgvsu.render_engine.RenderEngine;
import javafx.fxml.FXML;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.io.File;
import javax.vecmath.Vector3f;

import com.cgvsu.model.Model;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.render_engine.Camera;

public class GuiController {

    final private float TRANSLATION = 0.5F;

    @FXML
    AnchorPane anchorPane;

    @FXML
    private Canvas canvas;

    @FXML
    private VBox modelsListContainer;

    private java.util.List<Model> models = new java.util.ArrayList<>();
    private int modelCounter = 1;;

    private Camera camera = new Camera(
            new Vector3f(0, 00, 100),
            new Vector3f(0, 0, 0),
            1.0F, 1, 0.01F, 100);

    private Timeline timeline;

    @FXML
    private void initialize() {
        anchorPane.prefWidthProperty().addListener((ov, oldValue, newValue) -> canvas.setWidth(newValue.doubleValue()));
        anchorPane.prefHeightProperty().addListener((ov, oldValue, newValue) -> canvas.setHeight(newValue.doubleValue()));

        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        KeyFrame frame = new KeyFrame(Duration.millis(15), event -> {
            double width = canvas.getWidth();
            double height = canvas.getHeight();

            canvas.getGraphicsContext2D().clearRect(0, 0, width, height);
            camera.setAspectRatio((float) (width / height));

            for (Model model : models) {
                RenderEngine.render(canvas.getGraphicsContext2D(), camera, model, (int) width, (int) height);
            }
        });

        timeline.getKeyFrames().add(frame);
        timeline.play();
    }

    @FXML
    private void onOpenModelMenuItemClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Загрузить модель");

        File file = fileChooser.showOpenDialog((Stage) canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        Path fileName = Path.of(file.getAbsolutePath());

        try {
            String fileContent = Files.readString(file.toPath());
            Model model = ObjReader.read(fileContent);

            model.setName("Модель " + modelCounter++);
            models.add(model);

            updateModelsListUI();
            // todo: обработка ошибок
        } catch (IOException exception) {

        }
    }
    @FXML
    private void onSaveModelMenuItemClick() {
        if (models.isEmpty()) {
            System.out.println("Нет модели для сохранения!");
            return;
        }

        Model modelToSave = models.get(0);

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Сохранить модель");
        fileChooser.setInitialFileName("model.obj");

        File file = fileChooser.showSaveDialog((Stage) canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        try {
            String objContent = ObjWriter.modelToString(modelToSave);
            Files.writeString(file.toPath(), objContent);
        } catch (IOException exception) {

        }
    }

    private void updateModelsListUI() {
        modelsListContainer.getChildren().clear();

        for (Model model : models) {
            HBox item = new HBox(10);
            item.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            javafx.scene.control.Label label = new javafx.scene.control.Label(model.getName());

            javafx.scene.control.Button deleteBtn = new javafx.scene.control.Button("Удалить");
            deleteBtn.setOnAction(e -> {
                models.remove(model);
                updateModelsListUI();
            });

            item.getChildren().addAll(label, deleteBtn);
            modelsListContainer.getChildren().add(item);
        }
    }

    @FXML
    public void handleCameraForward(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, 0, -TRANSLATION));
    }

    @FXML
    public void handleCameraBackward(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, 0, TRANSLATION));
    }

    @FXML
    public void handleCameraLeft(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraRight(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(-TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraUp(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, TRANSLATION, 0));
    }

    @FXML
    public void handleCameraDown(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, -TRANSLATION, 0));
    }
}