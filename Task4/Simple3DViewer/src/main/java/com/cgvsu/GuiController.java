package com.cgvsu;

import com.cgvsu.objwriter.ObjWriter;
import com.cgvsu.render_engine.RenderEngine;
import javafx.fxml.FXML;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    Set<Model> activeModels = new HashSet<>();

    private List<Model> models = new ArrayList<>();
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

            for (Model model : activeModels) {
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
            activeModels.add(model);

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

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Сохранить модель");
        fileChooser.setInitialFileName("model.obj");

        File file = fileChooser.showSaveDialog((Stage) canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        try {
            StringBuilder sb = new StringBuilder();
            for(Model model : activeModels){
                sb.append(ObjWriter.modelToString(model)).append("\n");
                Files.writeString(file.toPath(), sb.toString());
            }
        } catch (IOException exception) {

        }
    }

    private void updateModelsListUI() {
        modelsListContainer.getChildren().clear();

        for (Model model : models) {
            HBox item = new HBox(10);
            item.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            Button modelBtn = new Button(model.getName());

            modelBtn.setOnAction(e -> {
                if (activeModels.contains(model)) {
                    activeModels.remove(model);
                } else {
                    activeModels.add(model);
                }
                updateModelButtonStyle(modelBtn, activeModels.contains(model));
            });

            updateModelButtonStyle(modelBtn, activeModels.contains(model));

            Button deleteBtn = new Button("Удалить");
            deleteBtn.setOnAction(e -> {
                models.remove(model);
                activeModels.remove(model);
                updateModelsListUI();
            });

            item.getChildren().addAll(modelBtn, deleteBtn);
            modelsListContainer.getChildren().add(item);
        }
    }

    private void updateModelButtonStyle(Button button, boolean isA) {
        if (isA) {
            button.setStyle("-fx-font-weight: bold; -fx-background-color: #cce5ff; -fx-border-color: #007bff; -fx-border-width: 1;");
        } else {
            button.setStyle("-fx-font-weight: normal; -fx-background-color: white; -fx-border-color: #ccc; -fx-border-width: 1;");
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