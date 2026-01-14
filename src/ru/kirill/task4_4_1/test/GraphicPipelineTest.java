package ru.kirill.task4_4_1.test;


import java.util.ArrayList;

import ru.kirill.task4_4_1.graphics.*;
import ru.kirill.task4_4_1.math.*;
import ru.kirill.task4_4_1.utils.*;


/*
 * Тестовый класс для проверки графического конвейера и аффинных преобразований.
 */
public class GraphicPipelineTest {
    
    public static void main(String[] args) {
        System.out.println("=== Тестирование графического конвейера ===\n");
        
        // Создаем тестовую модель (куб)
        Model cube = createCubeModel();
        System.out.println("Создана тестовая модель (куб):");
        System.out.println("  Вершин: " + cube.getVertices().size());
        System.out.println("  Полигонов: " + cube.getPolygons().size());
        
        // Создаем графический конвейер
        int viewportWidth = 800;
        int viewportHeight = 600;
        GraphicPipeline pipeline = new GraphicPipeline(viewportWidth, viewportHeight);
        
        System.out.println("\nСоздан графический конвейер:");
        System.out.println("  Размер области: " + viewportWidth + "x" + viewportHeight);
        System.out.println("  Камера: " + pipeline.getCamera());
        
        // Тест 1: Базовое преобразование вершины
        System.out.println("\n=== Тест 1: Базовое преобразование вершины ===");
        Vector3f testVertex = new Vector3f(1.0f, 0.5f, -0.5f);
        
        System.out.println("Исходная вершина: " + testVertex);
        
        Vector3f worldSpace = pipeline.transformVertex(testVertex, 
            GraphicPipeline.TransformSpace.WORLD_SPACE);
        System.out.println("В мировых координатах: " + worldSpace);
        
        Vector3f viewSpace = pipeline.transformVertex(testVertex, 
            GraphicPipeline.TransformSpace.VIEW_SPACE);
        System.out.println("В координатах камеры: " + viewSpace);
        
        Vector3f screenSpace = pipeline.transformVertex(testVertex, 
            GraphicPipeline.TransformSpace.SCREEN_SPACE);
        System.out.println("В экранных координатах: " + screenSpace);
        
        // Тест 2: Применение трансформаций модели
        System.out.println("\n=== Тест 2: Применение трансформаций модели ===");
        
        Transformation modelTransform = new Transformation();
        modelTransform.applyScaling(2.0f, 1.0f, 1.0f);
        modelTransform.applyRotationY(45.0f);
        modelTransform.applyTranslation(3.0f, 0.0f, 0.0f);
        
        pipeline.setModelTransformation(modelTransform);
        
        System.out.println("Матрица трансформации модели:");
        System.out.println(modelTransform);
        
        Vector3f transformedVertex = pipeline.transformVertex(testVertex, 
            GraphicPipeline.TransformSpace.WORLD_SPACE);
        System.out.println("Вершина после трансформации: " + transformedVertex);
        
        // Тест 3: Преобразование нормали
        System.out.println("\n=== Тест 3: Преобразование нормали ===");
        Vector3f testNormal = new Vector3f(0.0f, 1.0f, 0.0f).normalize();
        System.out.println("Исходная нормаль: " + testNormal);
        
        Vector3f transformedNormal = pipeline.transformNormal(testNormal);
        System.out.println("Трансформированная нормаль: " + transformedNormal);
        
        // Тест 4: Работа со сценой
        System.out.println("\n=== Тест 4: Работа со сценой ===");
        Scene scene = new Scene(viewportWidth, viewportHeight);
        
        scene.addObject("cube1", cube, new Transformation());
        
        Transformation cube2Transform = new Transformation();
        cube2Transform.applyTranslation(5.0f, 0.0f, 0.0f);
        cube2Transform.applyScaling(0.5f, 0.5f, 0.5f);
        
        scene.addObject("cube2", cube, cube2Transform);
        
        System.out.println("Сцена создана с объектами:");
        for (String objName : scene.getObjects().keySet()) {
            System.out.println("  - " + objName);
        }
        
        // Тест 5: Управление камерой
        System.out.println("\n=== Тест 5: Управление камерой ===");
        Camera camera = scene.getCamera();
        
        System.out.println("Исходное положение камеры: " + camera.getPosition());
        System.out.println("Цель камеры: " + camera.getTarget());
        
        // Перемещаем камеру
        camera.move(new Vector3f(2.0f, 1.0f, 0.0f));
        System.out.println("После перемещения: " + camera.getPosition());
        
        // Вращаем камеру
        camera.rotateAroundTarget(30.0f, 15.0f);
        System.out.println("После вращения: " + camera.getPosition());
        
        // Приближаем камеру
        camera.zoom(-2.0f);
        System.out.println("После приближения: " + camera.getPosition());
        
        System.out.println("\n=== Тестирование завершено ===");
    }
    
    // Создание простой модели куба
    private static Model createCubeModel() {
        Model cube = new Model();
        
        // Вершины куба
        cube.getVertices().add(new Vector3f(-1, -1, -1)); // 0
        cube.getVertices().add(new Vector3f(1, -1, -1));  // 1
        cube.getVertices().add(new Vector3f(1, 1, -1));   // 2
        cube.getVertices().add(new Vector3f(-1, 1, -1));  // 3
        cube.getVertices().add(new Vector3f(-1, -1, 1));  // 4
        cube.getVertices().add(new Vector3f(1, -1, 1));   // 5
        cube.getVertices().add(new Vector3f(1, 1, 1));    // 6
        cube.getVertices().add(new Vector3f(-1, 1, 1));   // 7
        
        // Полигоны (грани куба)
        // Передняя грань
        addPolygon(cube, 0, 1, 2, 3);
        // Задняя грань
        addPolygon(cube, 4, 5, 6, 7);
        // Левая грань
        addPolygon(cube, 0, 3, 7, 4);
        // Правая грань
        addPolygon(cube, 1, 2, 6, 5);
        // Верхняя грань
        addPolygon(cube, 3, 2, 6, 7);
        // Нижняя грань
        addPolygon(cube, 0, 1, 5, 4);
        
        return cube;
    }
    
    private static void addPolygon(Model model, int v1, int v2, int v3, int v4) {
        Polygon poly = new Polygon();
        ArrayList<Integer> indices = new ArrayList<>();
        indices.add(v1);
        indices.add(v2);
        indices.add(v3);
        indices.add(v4);
        poly.setVertexIndices(indices);
        model.getPolygons().add(poly);
    }
}
