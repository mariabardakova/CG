package com.cgvsu;

import com.cgvsu.model.Polygon;
import com.cgvsu.objwriter.ObjWriter;
import com.cgvsu.removers.PolygonRemover;
import com.cgvsu.removers.vertexremover.VertexRemoverImpl;
import com.cgvsu.render_engine.RenderEngine;
import javafx.fxml.FXML;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.geometry.Point2D;
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
import java.util.*;
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

    @FXML
    private Button editVerticesButton;

    private boolean editVerticesMode = false;

    Set<Model> activeModels = new HashSet<>();

    private List<Model> models = new ArrayList<>();
    private int modelCounter = 1;;

    private Camera camera = new Camera(
            new Vector3f(0, 00, 100),
            new Vector3f(0, 0, 0),
            1.0F, 1, 0.01F, 100);

    private Timeline timeline;

    @FXML
    private void toggleEditVerticesMode() {
        editVerticesMode = !editVerticesMode;
        editVerticesButton.setStyle(editVerticesMode ?
                "-fx-background-color: #ff6b6b; -fx-text-fill: white;" :
                "-fx-background-color: #f0f0f0;");
    }

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
                RenderEngine.render(canvas.getGraphicsContext2D(), camera, model, (int) width, (int) height, editVerticesMode);
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

            int v = 0;
            int vt = 0;
            int vn = 0;
            for(Model model : activeModels){

                String modelObj = ObjWriter.modelToString(model, null);
                String fixModel = shiftIndices(modelObj, v, vt, vn);

                sb.append(fixModel).append("\n");

                v += model.getVertices().size();
                if (model.getTextureVertices() != null){
                    vt += model.getTextureVertices().size();
                }else{
                    vt += 0;
                }
                if (model.getNormals() != null){
                    vn += model.getNormals().size();
                }else{
                    vn += 0;
                }
            }
            Files.writeString(file.toPath(), sb.toString());
        } catch (IOException exception) {

        }
    }

    private String shiftIndices(String objContent, int vOffset, int vtOffset, int vnOffset) {
        if (vOffset == 0 && vtOffset == 0 && vnOffset == 0) {
            return objContent;
        }

        StringBuilder result = new StringBuilder();
        String[] lines = objContent.split("\n");

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("f ")) {
                String[] tokens = trimmed.substring(2).split("\\s+");
                StringBuilder newFace = new StringBuilder("f");

                for (String token : tokens) {
                    if (token.isEmpty()) continue;

                    String[] indices = token.split("/");
                    try {
                        int vIdx = Integer.parseInt(indices[0]) + vOffset;
                        newFace.append(" ").append(vIdx);

                        if (indices.length > 1 && !indices[1].isEmpty()) {
                            int vtIdx = Integer.parseInt(indices[1]) + vtOffset;
                            newFace.append("/").append(vtIdx);
                        } else if (indices.length > 2) {
                            newFace.append("/");
                        }

                        if (indices.length > 2 && !indices[2].isEmpty()) {
                            int vnIdx = Integer.parseInt(indices[2]) + vnOffset;
                            if (indices.length <= 1 || indices[1].isEmpty()) {
                                newFace.append("/").append(vnIdx);
                            } else {
                                newFace.append("/").append(vnIdx);
                            }
                        }
                    } catch (NumberFormatException e) {
                        newFace.append(" ").append(token);
                    }
                }
                result.append(newFace).append("\n");
            } else {
                result.append(line).append("\n");
            }
        }

        return result.toString();
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