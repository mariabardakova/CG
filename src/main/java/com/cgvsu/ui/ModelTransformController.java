package com.cgvsu.ui;

import com.cgvsu.math.Transformation;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;

import java.util.ArrayList;
import java.util.List;


/*
 * Контроллер для управления трансформациями модели.
 * Предоставляет методы для масштабирования, вращения и перемещения модели.
 */
public class ModelTransformController {
    
    private Model model;
    private Transformation currentTransformation;
    
    // Наблюдатели за изменениями трансформации
    private List<TransformChangeListener> listeners;
    
    // Состояние трансформаций
    private Vector3f translation;
    private Vector3f rotation; // углы Эйлера в градусах
    private Vector3f scale;
    
    // Исходное состояние (для сброса)
    private Transformation initialTransformation;
    private Vector3f initialTranslation;
    private Vector3f initialRotation;
    private Vector3f initialScale;
    
    public interface TransformChangeListener {
        void onTransformChanged(ModelTransformController controller);
        void onTransformApplied(Model model);
    }
    
    public ModelTransformController() {
        this.currentTransformation = new Transformation();
        this.listeners = new ArrayList<>();
        
        // Начальные значения
        this.translation = new Vector3f(0, 0, 0);
        this.rotation = new Vector3f(0, 0, 0);
        this.scale = new Vector3f(1, 1, 1);
        
        saveInitialState();
    }
    
    public ModelTransformController(Model model) {
        this();
        setModel(model);
    }
    
    public void setModel(Model model) {
        this.model = model;
        if (model != null) {
            // Копируем трансформацию из модели
            Transformation modelTransform = model.getTransformation();
            if (modelTransform != null) {
                this.currentTransformation = modelTransform;
            }
            
            saveInitialState();
            notifyTransformChanged();
        }
    }
    
    private void saveInitialState() {
        this.initialTransformation = new Transformation();
        this.initialTranslation = new Vector3f(0, 0, 0);
        this.initialRotation = new Vector3f(0, 0, 0);
        this.initialScale = new Vector3f(1, 1, 1);
    }
    
    // Методы трансформации
    
    public void translate(float dx, float dy, float dz) {
        translation = translation.add(new Vector3f(dx, dy, dz));
        updateTransformation();
    }
    
    public void setTranslation(float x, float y, float z) {
        translation = new Vector3f(x, y, z);
        updateTransformation();
    }
    
    public void rotateX(float angleDegrees) {
        rotation = new Vector3f(
            rotation.getX() + angleDegrees,
            rotation.getY(),
            rotation.getZ()
        );
        updateTransformation();
    }
    
    public void rotateY(float angleDegrees) {
        rotation = new Vector3f(
            rotation.getX(),
            rotation.getY() + angleDegrees,
            rotation.getZ()
        );
        updateTransformation();
    }
    
    public void rotateZ(float angleDegrees) {
        rotation = new Vector3f(
            rotation.getX(),
            rotation.getY(),
            rotation.getZ() + angleDegrees
        );
        updateTransformation();
    }
    
    public void setRotation(float x, float y, float z) {
        rotation = new Vector3f(x, y, z);
        updateTransformation();
    }
    
    public void scale(float sx, float sy, float sz) {
        scale = new Vector3f(
            scale.getX() * sx,
            scale.getY() * sy,
            scale.getZ() * sz
        );
        updateTransformation();
    }
    
    public void setScale(float sx, float sy, float sz) {
        scale = new Vector3f(sx, sy, sz);
        updateTransformation();
    }
    
    public void scaleUniform(float factor) {
        scale(factor, factor, factor);
    }
    
    // Обновление общей трансформации на основе отдельных компонентов
    private void updateTransformation() {
        currentTransformation.reset();
        
        // Порядок применения: масштабирование -> вращение -> перемещение
        currentTransformation.applyScaling(scale.getX(), scale.getY(), scale.getZ());
        currentTransformation.applyRotationX(rotation.getX());
        currentTransformation.applyRotationY(rotation.getY());
        currentTransformation.applyRotationZ(rotation.getZ());
        currentTransformation.applyTranslation(translation.getX(), translation.getY(), translation.getZ());
        
        // Применяем трансформацию к модели
        if (model != null) {
            model.setTransformation(currentTransformation);
        }
        
        notifyTransformChanged();
    }
    
    // Применение трансформации к модели (финализирует изменения)
    public void applyTransformation() {
        if (model != null) {
            // Создаем новую модель с примененными трансформациями
            Model transformedModel = model.applyTransformation(true);
            
            // Уведомляем наблюдателей
            for (TransformChangeListener listener : listeners) {
                listener.onTransformApplied(transformedModel);
            }
            
            // Сбрасываем трансформации
            resetTransformation();
        }
    }
    
    // Сброс трансформаций
    public void resetTransformation() {
        translation = new Vector3f(0, 0, 0);
        rotation = new Vector3f(0, 0, 0);
        scale = new Vector3f(1, 1, 1);
        currentTransformation.reset();
        
        if (model != null) {
            model.setTransformation(currentTransformation);
        }
        
        notifyTransformChanged();
    }
    
    // Получение копии модели с примененными трансформациями
    public Model getTransformedModel(boolean applyTransformations) {
        if (model == null) {
            return null;
        }
        
        return model.applyTransformation(applyTransformations);
    }
    
    // Уведомление наблюдателей
    private void notifyTransformChanged() {
        for (TransformChangeListener listener : listeners) {
            listener.onTransformChanged(this);
        }
    }
    
    // Регистрация/удаление наблюдателей
    public void addTransformChangeListener(TransformChangeListener listener) {
        listeners.add(listener);
    }
    
    public void removeTransformChangeListener(TransformChangeListener listener) {
        listeners.remove(listener);
    }
    
    // Геттеры
    public Model getModel() { return model; }
    public Transformation getCurrentTransformation() { return currentTransformation; }
    public Vector3f getTranslation() { return translation; }
    public Vector3f getRotation() { return rotation; }
    public Vector3f getScale() { return scale; }
    
    @Override
    public String toString() {
        return String.format("TransformController[translation=%s, rotation=%s, scale=%s]", 
                translation, rotation, scale);
    }
}
