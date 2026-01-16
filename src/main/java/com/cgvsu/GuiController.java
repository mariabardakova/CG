package com.cgvsu;
import com.cgvsu.model.Polygon;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.objwriter.ObjWriter;
import com.cgvsu.removers.PolygonRemover;
import com.cgvsu.removers.vertexremover.VertexRemover;
import com.cgvsu.removers.vertexremover.VertexRemoverImpl;
import com.cgvsu.render_engine.RenderEngine;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import java.nio.file.Files;
import java.io.File;
import java.util.*;

import com.cgvsu.model.Model;
import com.cgvsu.render_engine.Camera;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point2f;
import javax.vecmath.Vector3f;

import static com.cgvsu.render_engine.GraphicConveyor.*;

public class GuiController {

    final private float TRANSLATION = 0.5F;

    @FXML
    AnchorPane anchorPane;

    @FXML
    private Canvas canvas;

    @FXML
    private VBox modelsListContainer;
    /**
     *Модель, над которой находится курсор
     */
    private Model hoveredModel = null;
    /**
     * Индекс полигона под курсором
     */
    private Integer hoveredPolygonIndex = null;
    /**
     * Индекс вершины под курсором
     */
    private Integer hoveredVertexIndex = null;

    /**
     * Флаг для режима редактирования
     */
    private boolean editVerticesMode = false;

    /**
     * Множество скрытых моделей
     */
    private Set<Model> hiddenModels = new HashSet<>();
    Set<Model> activeModels = new HashSet<>();

    private List<Model> models = new ArrayList<>();
    /**
     * Счётчик моделей
     */
    private int modelCounter = 1;

    private Camera camera = new Camera(
            new Vector3f(0, 0, 100),
            new Vector3f(0, 0, 0),
            1.0F, 1, 0.01F, 100);

    private Timeline timeline;

    @FXML private Button toggleThemeButton;

    /**
     * Контейнер для камер (заглушка, нет логики)
     */
    @FXML
    private VBox camerasContainer;

    /**
     * Выбор режима камеры
     */
    @FXML private ComboBox<String> cameraModeCombo;
    /**
     * Флажок для инверсии по Y (для Кирилла)
     */
    @FXML private CheckBox invertYAxisCheckBox;

    /**
     * Поля ввода для перемещения модели по осям
     */
    @FXML private TextField transXField, transYField, transZField;
    /**
     * Поля ввода для вращения
     */
    @FXML private TextField rotXField, rotYField, rotZField;
    /**
     * Поля ввода для масштабирования
     */
    @FXML private TextField scaleXField, scaleYField, scaleZField;

    /**
     * Парсит TextField в double
     * @param field
     * @param defaultValue
     * @return
     */
    private double parseDouble(TextField field, double defaultValue) {
        try {
            return Double.parseDouble(field.getText().trim());
        } catch (NumberFormatException e) {
            showErrorAlert("Некорректное значение", "Поле '" + field.getId() + "' содержит недопустимое число.", e.getMessage());
            return defaultValue;
        }
    }

    @FXML
    private void toggleDarkTheme() {
        Scene scene = canvas.getScene();
        if (scene == null || scene.getRoot() == null) return;

        ObservableList<String> styles = scene.getRoot().getStyleClass();
        if (styles.contains("dark-theme")) {
            styles.remove("dark-theme");
            toggleThemeButton.setText("Тёмная тема");
        } else {
            styles.add("dark-theme");
            toggleThemeButton.setText("Светлая тема");
        }
    }


    private Color currentColor = Color.WHITE;

    /**
     * Открывает диалог выбора цвета
     */
    @FXML
    private void chooseColor() {
        ColorPicker colorPicker = new ColorPicker(currentColor);
        colorPicker.setOnAction(event -> {
            currentColor = colorPicker.getValue();
        });


        Dialog<Color> dialog = new Dialog<>();
        dialog.setTitle("Выбор цвета");
        dialog.getDialogPane().setContent(colorPicker);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        java.util.Optional<Color> result = dialog.showAndWait();
        if (result.isPresent()) {
            currentColor = result.get();
        }
    }

    /**
     * Счётчик камер
     */
    private int cameraCnt = 1;

    /**
     * Добавляет новую камеру. Заглушка.
     */
    @FXML
    private void addCamera() {
        VBox cameraItem = new VBox(5);

        HBox headerRow = new HBox(10);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Button cameraBtn = new Button("Камера " + cameraCnt++);
        Button deleteBtn = new Button("Удалить");
        deleteBtn.setOnAction(e -> {
            camerasContainer.getChildren().remove(cameraItem);
        });

        headerRow.getChildren().addAll(cameraBtn, deleteBtn);
        VBox positionPanel = createVectorPanel("Позиция");
        VBox targetPanel = createVectorPanel("Точка направления");

        HBox fieldsRow = new HBox(20);
        fieldsRow.getChildren().addAll(positionPanel, targetPanel);
        cameraItem.getChildren().addAll(headerRow, fieldsRow);

        camerasContainer.getChildren().add(cameraItem);
    }

    /**
     * Создаёт панель ввода координат
     * @param title
     * @return
     */
    private VBox createVectorPanel(String title) {
        VBox panel = new VBox(5);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold;");

        TextField xField = new TextField("0.0");
        TextField yField = new TextField("0.0");
        TextField zField = new TextField("0.0");

        xField.setPrefWidth(80);
        yField.setPrefWidth(80);
        zField.setPrefWidth(80);

        HBox xRow = new HBox(5);
        xRow.getChildren().addAll(new Label("X:"), xField);
        HBox yRow = new HBox(5);
        yRow.getChildren().addAll(new Label("Y:"), yField);
        HBox zRow = new HBox(5);
        zRow.getChildren().addAll(new Label("Z:"), zField);

        panel.getChildren().addAll(titleLabel, xRow, yRow, zRow);

        return panel;
    }

    /**
     * Включает и выключает режим редактирования
     */
    @FXML
    private void toggleEditVerticesMode() {
        editVerticesMode = !editVerticesMode;
        if (editVerticesMode) {
            canvas.requestFocus();
        }
    }

    /**
     * Запускает анимацию рендеринга, настраивает камеру и обработчики.
     */
    @FXML
    private void initialize() {
        anchorPane.prefWidthProperty().addListener((ov, oldValue, newValue) -> canvas.setWidth(newValue.doubleValue()));
        anchorPane.prefHeightProperty().addListener((ov, oldValue, newValue) -> canvas.setHeight(newValue.doubleValue()));

        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        cameraModeCombo.getItems().addAll("Свободное перемещение", "Вращение вокруг цели");
        cameraModeCombo.setValue("Свободное перемещение");

        KeyFrame frame = new KeyFrame(Duration.millis(15), event -> {
            double width = canvas.getWidth();
            double height = canvas.getHeight();

            canvas.getGraphicsContext2D().clearRect(0, 0, width, height);
            GraphicsContext gc = canvas.getGraphicsContext2D();
            Color bgColor = canvas.getScene().getRoot().getStyleClass().contains("dark-theme") ? Color.BLACK : Color.rgb(220, 220, 220);
            gc.setFill(bgColor);
            gc.fillRect(0, 0, width, height);

            camera.setAspectRatio((float) (width / height));

            for (Model model : models) {
                if (hiddenModels.contains(model)) continue;

                Integer hvi = (activeModels.contains(model) && model == hoveredModel) ? hoveredVertexIndex : null;
                Integer hpi = (activeModels.contains(model) && model == hoveredModel) ? hoveredPolygonIndex : null;
                Color strokeColor = canvas.getScene().getRoot().getStyleClass().contains("dark-theme") ? Color.WHITE : Color.BLACK;
                RenderEngine.render(canvas.getGraphicsContext2D(), camera, model, (int) width, (int) height, editVerticesMode, hpi, hvi, strokeColor);            }
        });

        canvas.setOnMouseMoved(event -> {
            if (!editVerticesMode) {
                hoveredModel = null;
                hoveredPolygonIndex = null;
                hoveredVertexIndex = null;
                return;
            }

            double mouseX = event.getX();
            double mouseY = event.getY();

            boolean found = false;
            for (Model model : activeModels) {
                if (hiddenModels.contains(model)) continue;
                Integer polygonIndex = findPolygonUnderCursor(model, mouseX, mouseY, (int) canvas.getWidth(), (int) canvas.getHeight());
                if (polygonIndex != null) {
                    hoveredModel = model;
                    hoveredPolygonIndex = polygonIndex;
                    found = true;
                    hoveredVertexIndex = null;
                    break;
                }
            }
            if (!found) {
                for (Model model : activeModels) {
                    if (hiddenModels.contains(model)) continue;
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
            if (event.getCode() == KeyCode.DELETE) {
                if (hoveredPolygonIndex != null && hoveredModel != null) {
                    removeSelectPolygon(hoveredModel, hoveredPolygonIndex);
                    hoveredPolygonIndex = null;
                    hoveredModel = null;
                } else if (hoveredVertexIndex != null && hoveredModel != null) {
                    removeSelectedVertex(hoveredModel, hoveredVertexIndex);
                    hoveredVertexIndex = null;
                    hoveredModel = null;
                }
            }
        });

        timeline.getKeyFrames().add(frame);
        timeline.play();
    }

    /**
     * Загрузчик файлов
     */
    @FXML
    private void onOpenModelMenuItemClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Загрузить модель");

        File file = fileChooser.showOpenDialog((Stage) canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        try {
            String fileContent = Files.readString(file.toPath());
            Model model = ObjReader.read(fileContent);

            model.setName("Модель " + modelCounter++);

            // Сохраняем оригинальные вершины модели
            model.saveOriginalVertices();

            models.add(model);
            activeModels.add(model);

            updateModelsListUI();
        } catch (Exception e) {
            showErrorAlert("Ошибка при загрузке модели",
                    "Не удалось прочитать файл OBJ.",
                    e.getMessage());
        }
    }


    /**
     * Сохраняет все активные модели в один файл(теперь и те, что скрыты)
     */
    @FXML
    private void onSaveModelMenuItemClick() {
        List<Model> modelsToSave = new ArrayList<>();
        for (Model model : models) {
            if (activeModels.contains(model)) {
                modelsToSave.add(model);
            }
        }

        if (modelsToSave.isEmpty()) {
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
            int v = 0, vt = 0, vn = 0;

            for (Model model : modelsToSave) {
                String modelObj = ObjWriter.modelToString(model, null);
                String fixModel = shiftIndices(modelObj, v, vt, vn);
                sb.append(fixModel).append("\n");

                v += model.getVertices().size();
                if (model.getTextureVertices() != null) {
                    vt += model.getTextureVertices().size();
                }
                if (model.getNormals() != null) {
                    vn += model.getNormals().size();
                }
            }

            Files.writeString(file.toPath(), sb.toString());
        } catch (Exception e) {
            showErrorAlert("Ошибка при сохранении модели",
                    "Не удалось записать файл OBJ.",
                    e.getMessage());
        }
    }

    /**
     * Корректирует индексы при сохранении нескольких моделей в один файл
     * @param objContent
     * @param vOffset
     * @param vtOffset
     * @param vnOffset
     * @return
     */
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


    /**
     * Обновляет панель списка моделей
     */
    private void updateModelsListUI() {
        modelsListContainer.getChildren().clear();

        for (Model model : models) {
            HBox item = new HBox(5);
            item.setAlignment(Pos.CENTER_LEFT);

            Button modelBtn = new Button(model.getName());
            modelBtn.getStyleClass().add("model-button");
            modelBtn.setOnAction(e -> {
                if (activeModels.contains(model)) {
                    activeModels.remove(model);
                    if (hoveredModel == model) {
                        hoveredModel = null;
                        hoveredPolygonIndex = null;
                        hoveredVertexIndex = null;
                    }
                } else {
                    activeModels.add(model);
                    if (editVerticesMode) {
                        canvas.requestFocus();
                    }
                }
                updateModelButtonStyle(modelBtn, activeModels.contains(model));
            });
            updateModelButtonStyle(modelBtn, activeModels.contains(model));

            Button deleteBtn = new Button("Удалить");
            deleteBtn.getStyleClass().add("model-button");
            deleteBtn.setOnAction(e -> {
                models.remove(model);
                activeModels.remove(model);
                hiddenModels.remove(model);
                updateModelsListUI();
            });

            Button toggleVisibilityBtn = new Button(hiddenModels.contains(model) ? "Показать" : "Скрыть");
            toggleVisibilityBtn.getStyleClass().add("model-button");
            toggleVisibilityBtn.setOnAction(e -> {
                if (hiddenModels.contains(model)) {
                    hiddenModels.remove(model);
                    toggleVisibilityBtn.setText("Скрыть");
                    if (editVerticesMode && activeModels.contains(model)) {
                        canvas.requestFocus();
                    }
                } else {
                    hiddenModels.add(model);
                    toggleVisibilityBtn.setText("Показать");
                }
            });
            item.getChildren().addAll(modelBtn, deleteBtn, toggleVisibilityBtn);
            modelsListContainer.getChildren().add(item);

            HBox extraButtonsRow = new HBox(5);
            extraButtonsRow.setAlignment(Pos.CENTER_LEFT);
            extraButtonsRow.setStyle("-fx-padding: 0 0 5 20;");

            Button addTextureBtn = new Button("Добавить текстуру");
            addTextureBtn.getStyleClass().add("extra-button");
            Button removeTextureBtn = new Button("Удалить текстуру");
            removeTextureBtn.getStyleClass().add("extra-button");
            Button polygonBnt = new Button("Полигональная сетка");
            polygonBnt.getStyleClass().add("extra-button");

            extraButtonsRow.getChildren().addAll(addTextureBtn, removeTextureBtn, polygonBnt);
            modelsListContainer.getChildren().add(extraButtonsRow);

            HBox transformRow = new HBox(5);
            transformRow.setAlignment(Pos.CENTER_LEFT);
            transformRow.setStyle("-fx-padding: 0 0 10 20;");

            Button transformBtn = new Button("Трансформация");
            transformBtn.getStyleClass().add("extra-button");
            transformBtn.setOnAction(e -> {
                showTransformationDialog(model);
            });

            transformRow.getChildren().add(transformBtn);
            modelsListContainer.getChildren().add(transformRow);
        }
    }

    /**
     * Показывает диалог трансформаций для конкретной модели
     */

    private void showTransformationDialog(Model model) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Трансформации модели: " + model.getName());
        
        // Получаем текущие значения трансформаций модели
        float currentTx = model.getTranslationX();
        float currentTy = model.getTranslationY();
        float currentTz = model.getTranslationZ();
        
        float currentRx = model.getRotationX();
        float currentRy = model.getRotationY();
        float currentRz = model.getRotationZ();
        
        float currentSx = model.getScaleX();
        float currentSy = model.getScaleY();
        float currentSz = model.getScaleZ();
        
        // Создаем поля ввода с текущими значениями, используя Locale.US для точки
        TextField transX = new TextField(String.format(Locale.US, "%.2f", currentTx));
        TextField transY = new TextField(String.format(Locale.US, "%.2f", currentTy));
        TextField transZ = new TextField(String.format(Locale.US, "%.2f", currentTz));
        
        TextField rotX = new TextField(String.format(Locale.US, "%.2f", currentRx));
        TextField rotY = new TextField(String.format(Locale.US, "%.2f", currentRy));
        TextField rotZ = new TextField(String.format(Locale.US, "%.2f", currentRz));
        
        TextField scaleX = new TextField(String.format(Locale.US, "%.2f", currentSx));
        TextField scaleY = new TextField(String.format(Locale.US, "%.2f", currentSy));
        TextField scaleZ = new TextField(String.format(Locale.US, "%.2f", currentSz));
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 10, 10, 10));
        
        // Добавляем элементы в GridPane
        grid.add(new Label("Перемещение:"), 0, 0);
        grid.add(new Label("X:"), 0, 1);
        grid.add(transX, 1, 1);
        grid.add(new Label("Y:"), 0, 2);
        grid.add(transY, 1, 2);
        grid.add(new Label("Z:"), 0, 3);
        grid.add(transZ, 1, 3);
        
        grid.add(new Label("Вращение (градусы):"), 0, 4);
        grid.add(new Label("X:"), 0, 5);
        grid.add(rotX, 1, 5);
        grid.add(new Label("Y:"), 0, 6);
        grid.add(rotY, 1, 6);
        grid.add(new Label("Z:"), 0, 7);
        grid.add(rotZ, 1, 7);
        
        grid.add(new Label("Масштабирование:"), 0, 8);
        grid.add(new Label("X:"), 0, 9);
        grid.add(scaleX, 1, 9);
        grid.add(new Label("Y:"), 0, 10);
        grid.add(scaleY, 1, 10);
        grid.add(new Label("Z:"), 0, 11);
        grid.add(scaleZ, 1, 11);
        
        dialog.getDialogPane().setContent(grid);
        
        // Кнопки
        ButtonType applyButton = new ButtonType("Применить", ButtonBar.ButtonData.OK_DONE);
        ButtonType resetButton = new ButtonType("Сбросить", ButtonBar.ButtonData.OTHER);
        dialog.getDialogPane().getButtonTypes().addAll(applyButton, resetButton, ButtonType.CANCEL);
        
        // Обработка кнопки "Сбросить"
        Button resetButtonNode = (Button) dialog.getDialogPane().lookupButton(resetButton);
        resetButtonNode.setOnAction(e -> {
            model.resetTransformations();
            // Обновляем поля в диалоге с использованием Locale.US
            transX.setText(String.format(Locale.US, "%.1f", 0.0f));
            transY.setText(String.format(Locale.US, "%.1f", 0.0f));
            transZ.setText(String.format(Locale.US, "%.1f", 0.0f));
            rotX.setText(String.format(Locale.US, "%.1f", 0.0f));
            rotY.setText(String.format(Locale.US, "%.1f", 0.0f));
            rotZ.setText(String.format(Locale.US, "%.1f", 0.0f));
            scaleX.setText(String.format(Locale.US, "%.1f", 1.0f));
            scaleY.setText(String.format(Locale.US, "%.1f", 1.0f));
            scaleZ.setText(String.format(Locale.US, "%.1f", 1.0f));
        });
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == applyButton) {
                try {
                    float tx = Float.parseFloat(transX.getText());
                    float ty = Float.parseFloat(transY.getText());
                    float tz = Float.parseFloat(transZ.getText());
                    
                    float rx = Float.parseFloat(rotX.getText());
                    float ry = Float.parseFloat(rotY.getText());
                    float rz = Float.parseFloat(rotZ.getText());
                    
                    float sx = Float.parseFloat(scaleX.getText());
                    float sy = Float.parseFloat(scaleY.getText());
                    float sz = Float.parseFloat(scaleZ.getText());
                    if (sx == 0 || sy == 0 || sz == 0) {
                        showErrorAlert("Ошибка масштабирования", 
                            "Коэффициент масштабирования не может быть нулевым.", "");
                        return null;
                    }
                    
                    model.setAllTransformations(tx, ty, tz, rx, ry, rz, sx, sy, sz);
                } catch (NumberFormatException ex) {
                    showErrorAlert("Ошибка ввода", "Некорректные числовые значения", 
                        "Используйте формат: 0.00 или 0,00");
                }
            }
            return null;
        });
        
        dialog.showAndWait();
    }


    /**
     * Меняет стиль кнопки в зависимости от активности модели
     *
     * @param button
     * @param isActive
     */
    private void updateModelButtonStyle(Button button, boolean isActive) {
        button.getStyleClass().removeAll("model-active", "model-inactive");

        if (isActive) {
            button.getStyleClass().add("model-active");
        } else {
            button.getStyleClass().add("model-inactive");
        }
    }

    /**
     * Находит полигон под курсором
     *
     * @param model
     * @param mouseX
     * @param mouseY
     * @param width
     * @param height
     * @return
     */
    private Integer findPolygonUnderCursor(Model model, double mouseX, double mouseY, int width, int height) {
        Camera cam = camera;
        Matrix4f modelMatrix = rotateScaleTranslate();
        Matrix4f viewMatrix = cam.getViewMatrix();
        Matrix4f projectionMatrix = cam.getProjectionMatrix();

        Matrix4f mvp = new Matrix4f(modelMatrix);
        mvp.mul(viewMatrix);
        mvp.mul(projectionMatrix);

        for (int i = 0; i < model.getPolygons().size(); i++) {
            Polygon polygon = model.getPolygons().get(i);
            List<Integer> vertexIndices = polygon.getVertexIndices();
            if (vertexIndices == null || vertexIndices.isEmpty()) continue;

            List<Point2f> screenPoints = new ArrayList<>();
            for (Integer idx : vertexIndices) {
                com.cgvsu.math.Vector3f v3d = model.getVertices().get(idx);
                Vector3f v = new Vector3f(v3d.getX(), v3d.getY(), v3d.getZ());
                Point2f p = vertexToPoint(multiplyMatrix4ByVector3(mvp, v), width, height);
                if (!Float.isFinite(p.x) || !Float.isFinite(p.y)) {
                    screenPoints = null;
                    break;
                }
                screenPoints.add(p);
            }
            if (screenPoints == null || screenPoints.size() < 3) continue;

            if (isPointInPolygon((float) mouseX, (float) mouseY, screenPoints)) {
                return i;
            }
        }
        return null;
    }

    /**
     * Проверяет, попадает ли точка в полигон
     * @param x
     * @param y
     * @param polygon
     * @return
     */
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

    /**
     * Удаляет выбранный полигон
     * @param model
     * @param polygonIndex
     */
    private void removeSelectPolygon(Model model, int polygonIndex) {
        try {
            List<Integer> indices = Collections.singletonList(polygonIndex);
            PolygonRemover.removePolygons(model, indices, true);
            hoveredVertexIndex = null;
            hoveredModel = null;
            hoveredPolygonIndex = null;
        } catch (Exception e) {
            showErrorAlert("Ошибка при удалении полигона",
                    "Невозможно удалить выбранный полигон.",
                    e.getMessage());
            hoveredModel = null;
            hoveredVertexIndex = null;
            hoveredPolygonIndex = null;
        }
    }

    /**
     * Находит вершину под курсором
     * @param model
     * @param mouseX
     * @param mouseY
     * @param width
     * @param height
     * @return
     */
    private Integer findVertexUnderCursor(Model model, double mouseX, double mouseY, int width, int height) {
        Camera cam = camera;
        Matrix4f modelMatrix = rotateScaleTranslate();
        Matrix4f viewMatrix = cam.getViewMatrix();
        Matrix4f projectionMatrix = cam.getProjectionMatrix();

        Matrix4f mvp = new Matrix4f(modelMatrix);
        mvp.mul(viewMatrix);
        mvp.mul(projectionMatrix);

        float threshold = 5.0f;

        for (int i = 0; i < model.getVertices().size(); i++) {
            com.cgvsu.math.Vector3f v3d = model.getVertices().get(i);
            javax.vecmath.Vector3f v = new javax.vecmath.Vector3f(v3d.getX(), v3d.getY(), v3d.getZ());
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

    /**
     * Удаляет выбранную вершину
     * @param model
     * @param vertexIndex
     */
    private void removeSelectedVertex(Model model, int vertexIndex) {
        Set<Integer> indices = Collections.singleton(vertexIndex);
        try {
            VertexRemover remover = new VertexRemoverImpl();
            remover.removeVertices(model, indices, true);
            hoveredModel = null;
            hoveredPolygonIndex = null;
            hoveredVertexIndex = null;
        } catch (Exception e) {
            showErrorAlert("Ошибка при удалении вершины",
                    "Невозможно удалить выбранную вершину.",
                    e.getMessage());
            hoveredModel = null;
            hoveredVertexIndex = null;
            hoveredPolygonIndex = null;
        }
    }

    /**
     * Вызывает диалог ошибки
     * @param header
     * @param content
     * @param details
     */
    private void showErrorAlert(String header, String content, String details) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(header);
        alert.setContentText(content + "\n\nДетали: " + (details != null ? details : "—"));
        alert.getButtonTypes().setAll(ButtonType.OK);
        alert.showAndWait();
    }


    /**
     * Применяет перемещение к активным моделям
     */
    @FXML
    private void applyTranslation() {
        double x = parseDouble(transXField, 0.0);
        double y = parseDouble(transYField, 0.0);
        double z = parseDouble(transZField, 0.0);
        
        for (Model model : activeModels) {
            if (!hiddenModels.contains(model)) {
                model.applyTranslation((float)x, (float)y, (float)z);
            }
        }
    }

    /**
     * Применяет вращение к активным моделям
     */
    @FXML
    private void applyRotation() {
        double x = parseDouble(rotXField, 0.0);
        double y = parseDouble(rotYField, 0.0);
        double z = parseDouble(rotZField, 0.0);
        
        for (Model model : activeModels) {
            if (!hiddenModels.contains(model)) {
                model.applyRotation((float)x, (float)y, (float)z);
            }
        }
    }

    /**
     * Применяет масштабирование к активным моделям
     */
    @FXML
    private void applyScaling() {
        double x = parseDouble(scaleXField, 1.0);
        double y = parseDouble(scaleYField, 1.0);
        double z = parseDouble(scaleZField, 1.0);
        
        if (x == 0 || y == 0 || z == 0) {
            showErrorAlert("Ошибка масштабирования", "Коэффициент масштабирования не может быть нулевым.", "");
            return;
        }
        
        for (Model model : activeModels) {
            if (!hiddenModels.contains(model)) {
                model.applyScaling((float)x, (float)y, (float)z);
            }
        }
    }

    /**
     * Применяет все трансформации к активным моделям
     */
    @FXML
    private void applyAllTransformations() {
        double tx = parseDouble(transXField, 0.0);
        double ty = parseDouble(transYField, 0.0);
        double tz = parseDouble(transZField, 0.0);
        
        double rx = parseDouble(rotXField, 0.0);
        double ry = parseDouble(rotYField, 0.0);
        double rz = parseDouble(rotZField, 0.0);
        
        double sx = parseDouble(scaleXField, 1.0);
        double sy = parseDouble(scaleYField, 1.0);
        double sz = parseDouble(scaleZField, 1.0);
        
        if (sx == 0 || sy == 0 || sz == 0) {
            showErrorAlert("Ошибка масштабирования", "Коэффициент масштабирования не может быть нулевым.", "");
            return;
        }
        
        for (Model model : activeModels) {
            if (!hiddenModels.contains(model)) {
                model.applyAllTransformations(
                    (float)tx, (float)ty, (float)tz,
                    (float)rx, (float)ry, (float)rz,
                    (float)sx, (float)sy, (float)sz
                );
            }
        }
    }

    /**
     * Сбрасывает трансформации для активных моделей
     */
    @FXML
    private void resetTransformations() {
        transXField.setText("0.0");
        transYField.setText("0.0");
        transZField.setText("0.0");
        
        rotXField.setText("0.0");
        rotYField.setText("0.0");
        rotZField.setText("0.0");
        
        scaleXField.setText("1.0");
        scaleYField.setText("1.0");
        scaleZField.setText("1.0");
        
        for (Model model : activeModels) {
            if (!hiddenModels.contains(model)) {
                model.resetTransformations();
            }
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