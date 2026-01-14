package ru.kirill.task4_4_1.graphics;


import ru.kirill.task4_4_1.math.Matrix4f;
import ru.kirill.task4_4_1.math.Transformation;
import ru.kirill.task4_4_1.utils.Vector3f;
import ru.kirill.task4_4_1.utils.Vector2f;
import ru.kirill.task4_4_1.utils.Model;

import java.util.ArrayList;


/**
 * Класс для реализации графического конвейера.
 * Выполняет преобразование вершин из локальных координат в экранные координаты.
 */
public class GraphicPipeline {
    
    public enum TransformSpace {
        LOCAL_SPACE,      // Локальные координаты модели
        WORLD_SPACE,      // Мировые координаты
        VIEW_SPACE,       // Координаты камеры
        CLIP_SPACE,       // Пространство отсечения
        NDC_SPACE,        // Нормализованные координаты устройства
        SCREEN_SPACE      // Экранные координаты
    }
    
    // Трансформации модели
    private Transformation modelTransformation;
    
    // Камера
    private Camera camera;
    
    // Размеры области отображения (в пикселях)
    private int viewportWidth;
    private int viewportHeight;
    
    // MVP матрица (Model-View-Projection)
    private Matrix4f mvpMatrix;
    
    public GraphicPipeline(int viewportWidth, int viewportHeight) {
        this.modelTransformation = new Transformation();
        this.camera = new Camera();
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        updateMVPMatrix();
    }
    
    public GraphicPipeline(Camera camera, int viewportWidth, int viewportHeight) {
        this.modelTransformation = new Transformation();
        this.camera = camera;
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        updateMVPMatrix();
    }
    
    // Обновление MVP матрицы
    private void updateMVPMatrix() {
        Matrix4f modelMatrix = modelTransformation.getMatrix();
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();
        
        // MVP = Projection * View * Model
        mvpMatrix = projectionMatrix.mul(viewMatrix.mul(modelMatrix));
    }
    
    // Преобразование вершины через весь конвейер
    public Vector3f transformVertex(Vector3f vertex, TransformSpace outputSpace) {
        return transformVertex(vertex, modelTransformation.getMatrix(), outputSpace);
    }
    
    // Преобразование вершины с заданной матрицей модели
    public Vector3f transformVertex(Vector3f vertex, Matrix4f customModelMatrix, TransformSpace outputSpace) {
        Vector3f result = vertex;
        
        // 1. Модельное преобразование (локальные -> мировые)
        if (outputSpace.compareTo(TransformSpace.LOCAL_SPACE) > 0) {
            result = customModelMatrix.mul(result);
        }
        
        // 2. Преобразование вида (мировые -> камеры)
        if (outputSpace.compareTo(TransformSpace.WORLD_SPACE) > 0) {
            result = camera.getViewMatrix().mul(result);
        }
        
        // 3. Проекционное преобразование (камера -> отсечение)
        if (outputSpace.compareTo(TransformSpace.VIEW_SPACE) > 0) {
            result = camera.getProjectionMatrix().mul(result);
        }
        
        // 4. Перспективное деление (отсечение -> NDC)
        if (outputSpace.compareTo(TransformSpace.CLIP_SPACE) > 0) {
            // Для NDC и SCREEN_SPACE нужно перспективное деление
            // Оно уже выполнено в Matrix4f.mul() для Vector3f
        }
        
        // 5. Преобразование в экранные координаты (NDC -> экран)
        if (outputSpace.compareTo(TransformSpace.NDC_SPACE) > 0) {
            result = ndcToScreen(result);
        }
        
        return result;
    }
    
    // Преобразование нормали
    public Vector3f transformNormal(Vector3f normal) {
        return modelTransformation.transformNormal(normal);
    }
    
    // Преобразование из NDC в экранные координаты
    private Vector3f ndcToScreen(Vector3f ndc) {
        // NDC: x,y ∈ [-1, 1], z ∈ [0, 1] (для глубины)
        float screenX = (ndc.getX() + 1.0f) * 0.5f * viewportWidth;
        float screenY = (1.0f - ndc.getY()) * 0.5f * viewportHeight; // Ось Y инвертирована
        float screenZ = ndc.getZ(); // Сохраняем глубину
        
        return new Vector3f(screenX, screenY, screenZ);
    }
    
    // Преобразование из экранных координат в NDC
    public Vector3f screenToNDC(Vector3f screen) {
        float ndcX = (screen.getX() / viewportWidth) * 2.0f - 1.0f;
        float ndcY = 1.0f - (screen.getY() / viewportHeight) * 2.0f; // Ось Y инвертирована
        float ndcZ = screen.getZ();
        
        return new Vector3f(ndcX, ndcY, ndcZ);
    }
    
    // Преобразование всей модели
    public Model transformModel(Model model, boolean transformNormals, boolean includeTransformations) {
        Model transformedModel = new Model();
        
        // Трансформируем вершины
        for (Vector3f vertex : model.getVertices()) {
            Vector3f transformedVertex = includeTransformations ? 
                transformVertex(vertex, TransformSpace.WORLD_SPACE) : vertex;
            transformedModel.getVertices().add(transformedVertex);
        }
        
        // Трансформируем текстурные координаты (если есть)
        if (model.getTextureVertices() != null) {
            for (Vector2f texCoord : model.getTextureVertices()) {
                transformedModel.getTextureVertices().add(texCoord);
            }
        }
        
        // Трансформируем нормали (если есть и нужно)
        if (transformNormals && model.getNormals() != null) {
            for (Vector3f normal : model.getNormals()) {
                Vector3f transformedNormal = includeTransformations ? 
                    transformNormal(normal) : normal;
                transformedModel.getNormals().add(transformedNormal);
            }
        }
        
        // Копируем полигоны (индексы не меняются)
        transformedModel.setPolygons(model.getPolygons());
        
        return transformedModel;
    }
    
    // Обновление размеров области отображения
    public void updateViewport(int width, int height) {
        this.viewportWidth = width;
        this.viewportHeight = height;
        camera.setAspectRatio((float)width / height);
        updateMVPMatrix();
    }
    
    // Применение трансформации к модели
    public void applyModelTransformation(Transformation transformation) {
        modelTransformation = transformation;
        updateMVPMatrix();
    }
    
    // Геттеры
    public Transformation getModelTransformation() { return modelTransformation; }
    public Camera getCamera() { return camera; }
    public int getViewportWidth() { return viewportWidth; }
    public int getViewportHeight() { return viewportHeight; }
    public Matrix4f getMVPMatrix() { return mvpMatrix; }
    
    // Сеттеры
    public void setCamera(Camera camera) { 
        this.camera = camera; 
        updateMVPMatrix();
    }
    
    public void setModelTransformation(Transformation transformation) { 
        this.modelTransformation = transformation; 
        updateMVPMatrix();
    }
}
