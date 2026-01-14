package com.cgvsu;

import com.cgvsu.model.Polygon;
import com.cgvsu.objwriter.ObjWriter;
import com.cgvsu.removers.PolygonRemover;
import com.cgvsu.removers.vertexremover.VertexRemover;
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
import javafx.scene.input.KeyCode;
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
import javax.vecmath.Matrix4f;
import javax.vecmath.Point2f;
import javax.vecmath.Vector3f;

import com.cgvsu.model.Model;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.render_engine.Camera;

import static com.cgvsu.render_engine.GraphicConveyor.*;

public class GuiController {

    final private float TRANSLATION = 0.5F;

    @FXML
    AnchorPane anchorPane;

    @FXML
    private Canvas canvas;

    @FXML
    private VBox modelsListContainer;

    private Model hoveredModel = null;
    private Integer hoveredPolygonIndex = null;
    private Integer hoveredVertexIndex = null;

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
        if (editVerticesMode){
            canvas.requestFocus();
        }
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

                Integer hvi = (model == hoveredModel) ? hoveredVertexIndex : null;
                Integer hpi = (model == hoveredModel) ? hoveredPolygonIndex : null;
                RenderEngine.render(canvas.getGraphicsContext2D(), camera, model, (int) width, (int) height, editVerticesMode, hpi, hvi);
            }
        });

        canvas.setOnMouseMoved(event -> {
            if (!editVerticesMode){
                hoveredModel = null;
                hoveredPolygonIndex = null;
                return;
            }

            double mouseX = event.getX();
            double mouseY = event.getY();

            boolean found = false;
            for(Model model: activeModels){
                Integer polygonIndex = findPolygonUnderCursor(model, mouseX, mouseY, (int) canvas.getWidth(), (int) canvas.getHeight());
                if(polygonIndex != null){
                    hoveredModel = model;
                    hoveredPolygonIndex = polygonIndex;
                    found = true;
                    hoveredVertexIndex = null;
                    break;
                }
            }
            if (!found) {
                for (Model model : activeModels) {
                    Integer vertexIndex = findVertexUnderCursor(model, mouseX, mouseY, (int) canvas.getWidth(), (int) canvas.getHeight());
                    if (vertexIndex != null) {
                        hoveredModel = model;
                        hoveredVertexIndex = vertexIndex;
                        hoveredPolygonIndex = null;
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                hoveredModel = null;
                hoveredPolygonIndex = null;
                hoveredVertexIndex = null;
            }
        });

        canvas.setFocusTraversable(true);
        canvas.setOnKeyPressed(event -> {
            if (!editVerticesMode) return;
            if(event.getCode() == KeyCode.DELETE){
                if(hoveredPolygonIndex != null && hoveredModel != null){
                    removeSelectPolygon(hoveredModel, hoveredPolygonIndex);
                    hoveredPolygonIndex = null;
                    hoveredModel = null;
                }else if (hoveredVertexIndex != null && hoveredModel != null) {
                    removeSelectedVertex(hoveredModel, hoveredVertexIndex);
                    hoveredVertexIndex = null;
                    hoveredModel = null;
                }
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

    private Integer findPolygonUnderCursor(Model model, double mouseX, double mouseY, int width, int height){
        Camera cam = camera;
        Matrix4f modelMatrix = rotateScaleTranslate();
        Matrix4f viewMatrix = cam.getViewMatrix();
        Matrix4f projectionMatrix = cam.getProjectionMatrix();

        Matrix4f mvp = new Matrix4f(modelMatrix);
        mvp.mul(viewMatrix);
        mvp.mul(projectionMatrix);

        for(int i = 0; i < model.polygons.size(); i ++){
            Polygon polygon = model.polygons.get(i);
            List<Integer> vertexIndices = polygon.getVertexIndices();
            if (vertexIndices == null || vertexIndices.isEmpty()) continue;

            List<Point2f> screenPoints = new ArrayList<>();
            for (Integer idx : vertexIndices){
                com.cgvsu.math.Vector3f v3d = model.vertices.get(idx);
                javax.vecmath.Vector3f v = new javax.vecmath.Vector3f(v3d.x, v3d.y, v3d.z);
                Point2f p = vertexToPoint(multiplyMatrix4ByVector3(mvp, v), width, height);
                if (!Float.isFinite(p.x) || !Float.isFinite(p.y)) {
                    screenPoints = null;
                    break;
                }
                screenPoints.add(p);
            }
            if(screenPoints == null || screenPoints.size() < 3) continue;

            if (isPointInPolygon((float) mouseX, (float) mouseY, screenPoints)){
                return i;
            }
        }
        return null;
    }

    private boolean isPointInPolygon(float x, float y, List<Point2f> polygon) {
        int n = polygon.size();
        boolean inside = false;

        for (int i = 0, j = n - 1; i < n; j = i++) {
            float xi = polygon.get(i).x;
            float yi = polygon.get(i).y;
            float xj = polygon.get(j).x;
            float yj = polygon.get(j).y;

            boolean intersect = ((yi > y) != (yj > y)) &&
                    (x < (xj - xi) * (y - yi) / (yj - yi) + xi);
            if (intersect) inside = !inside;
        }
        return inside;
    }

    private void removeSelectPolygon(Model model, int polygonIndex){
        List<Integer> indices = Collections.singletonList(polygonIndex);
        PolygonRemover.removePolygons(model, indices, true);

        hoveredVertexIndex = null;
        hoveredModel = null;
        hoveredPolygonIndex = null;
    }

    private Integer findVertexUnderCursor(Model model, double mouseX, double mouseY, int width, int height) {
        Camera cam = camera;
        Matrix4f modelMatrix = rotateScaleTranslate();
        Matrix4f viewMatrix = cam.getViewMatrix();
        Matrix4f projectionMatrix = cam.getProjectionMatrix();

        Matrix4f mvp = new Matrix4f(modelMatrix);
        mvp.mul(viewMatrix);
        mvp.mul(projectionMatrix);

        float threshold = 5.0f;

        for (int i = 0; i < model.vertices.size(); i++) {
            com.cgvsu.math.Vector3f v3d = model.vertices.get(i);
            javax.vecmath.Vector3f v = new javax.vecmath.Vector3f(v3d.x, v3d.y, v3d.z);
            Point2f p = vertexToPoint(multiplyMatrix4ByVector3(mvp, v), width, height);

            if (Float.isFinite(p.x) && Float.isFinite(p.y)) {
                double dx = p.x - mouseX;
                double dy = p.y - mouseY;
                if (dx * dx + dy * dy <= threshold * threshold) {
                    return i;
                }
            }
        }
        return null;
    }

    private void removeSelectedVertex(Model model, int vertexIndex) {
        Set<Integer> indices = Collections.singleton(vertexIndex);
        try {
            VertexRemover remover = new VertexRemoverImpl();
            remover.removeVertices(model, indices, true);
            hoveredModel = null;
            hoveredPolygonIndex = null;
            hoveredVertexIndex = null;
        } catch (Exception e) {
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