package com.cgvsu.graphics;


import com.cgvsu.math.Transformation;
import com.cgvsu.model.Model;

import java.util.HashMap;
import java.util.Map;

/**
 * Класс для управления сценой: содержит объекты, камеру и освещение.
 */
public class Scene {
    private Map<String, SceneObject> objects;
    private Camera camera;
    private GraphicPipeline pipeline;
    
    // Параметры освещения
    private SceneLighting lighting;
    
    public Scene(int viewportWidth, int viewportHeight) {
        this.objects = new HashMap<>();
        this.camera = new Camera();
        this.pipeline = new GraphicPipeline(camera, viewportWidth, viewportHeight);
        this.lighting = new SceneLighting();
    }
    
    // Добавление объекта в сцену
    public void addObject(String name, Model model) {
        addObject(name, model, new Transformation());
    }
    
    public void addObject(String name, Model model, Transformation transformation) {
        SceneObject obj = new SceneObject(name, model, transformation);
        objects.put(name, obj);
    }
    
    // Удаление объекта из сцены
    public void removeObject(String name) {
        objects.remove(name);
    }
    
    // Получение объекта
    public SceneObject getObject(String name) {
        return objects.get(name);
    }
    
    // Обновление трансформации объекта
    public void updateObjectTransformation(String name, Transformation transformation) {
        SceneObject obj = objects.get(name);
        if (obj != null) {
            obj.setTransformation(transformation);
        }
    }
    
    // Рендеринг всей сцены
    public void render() {
        for (SceneObject obj : objects.values()) {
            renderObject(obj);
        }
    }
    
    // Рендеринг конкретного объекта
    private void renderObject(SceneObject obj) {
        // Применяем трансформацию объекта к конвейеру
        pipeline.applyModelTransformation(obj.getTransformation());
        
        // Трансформируем модель для рендеринга
        Model transformedModel = pipeline.transformModel(
            obj.getModel(), 
            lighting.isEnabled(),  // Трансформировать нормали только если включено освещение
            true                   // Включать трансформации
        );
        
        // Здесь будет логика отрисовки трансформированной модели
        // Например: передача в рендерер, вывод на экран и т.д.
        
        // Для отладки выведем информацию
        System.out.println("Rendering object: " + obj.getName());
        System.out.println("  Vertices: " + transformedModel.getVertices().size());
        System.out.println("  Polygons: " + transformedModel.getPolygons().size());
    }
    
    // Обновление размеров области отображения
    public void updateViewport(int width, int height) {
        pipeline.updateViewport(width, height);
    }
    
    // Геттеры
    public Camera getCamera() { return camera; }
    public GraphicPipeline getPipeline() { return pipeline; }
    public SceneLighting getLighting() { return lighting; }
    public Map<String, SceneObject> getObjects() { return objects; }
    
    // Класс для представления объекта сцены
    public static class SceneObject {
        private String name;
        private Model model;
        private Transformation transformation;
        
        public SceneObject(String name, Model model, Transformation transformation) {
            this.name = name;
            this.model = model;
            this.transformation = transformation;
        }
        
        // Геттеры и сеттеры
        public String getName() { return name; }
        public Model getModel() { return model; }
        public Transformation getTransformation() { return transformation; }
        
        public void setTransformation(Transformation transformation) { 
            this.transformation = transformation; 
        }
    }
}
