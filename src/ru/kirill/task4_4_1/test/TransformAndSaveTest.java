package ru.kirill.task4_4_1.test;


import ru.kirill.task4_4_1.ui.*;
import ru.kirill.task4_4_1.utils.*;
import ru.kirill.task4_4_1.math.Transformation;
import ru.kirill.task4_4_1.math.Matrix4f;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Тестовый класс для проверки трансформаций модели и сохранения.
 */
public class TransformAndSaveTest {
    
    public static void main(String[] args) {
        System.out.println("=== Тестирование трансформаций и сохранения модели ===\n");
        
        // Создаем тестовую модель
        Model testModel = createTestModel("TestCube");
        System.out.println("Создана тестовая модель:");
        System.out.println(testModel);
        
        // Создаем контроллер трансформаций
        ModelTransformController controller = new ModelTransformController(testModel);
        
        // Тест 1: Базовые трансформации
        System.out.println("\n=== Тест 1: Базовые трансформации ===");
        
        System.out.println("Начальное состояние: " + controller);
        
        // Применяем трансформации
        controller.translate(2.0f, 1.0f, 0.0f);
        System.out.println("После перемещения на (2,1,0): " + controller);
        
        controller.rotateY(45.0f);
        System.out.println("После вращения на 45° вокруг Y: " + controller);
        
        controller.scale(2.0f, 1.0f, 1.0f);
        System.out.println("После масштабирования (2,1,1): " + controller);
        
        // Тест 2: Получение трансформированной модели
        System.out.println("\n=== Тест 2: Получение трансформированной модели ===");
        
        Model originalModel = controller.getTransformedModel(false);
        Model transformedModel = controller.getTransformedModel(true);
        
        System.out.println("Исходная модель - вершин: " + originalModel.getVertices().size());
        System.out.println("Трансформированная модель - вершин: " + transformedModel.getVertices().size());
        
        // Сравниваем первую вершину
        if (!originalModel.getVertices().isEmpty() && !transformedModel.getVertices().isEmpty()) {
            System.out.println("Первая вершина исходной модели: " + originalModel.getVertices().get(0));
            System.out.println("Первая вершина трансформированной модели: " + transformedModel.getVertices().get(0));
        }
        
        // Тест 3: Сохранение модели
        System.out.println("\n=== Тест 3: Сохранение модели ===");
        
        try {
            // Создаем временные файлы
            String tempDir = System.getProperty("java.io.tmpdir");
            String originalPath = tempDir + "test_original.obj";
            String transformedPath = tempDir + "test_transformed.obj";
            
            // Сохраняем исходную модель
            ObjWriter.write(testModel, originalPath, false);
            System.out.println("Исходная модель сохранена в: " + originalPath);
            
            // Сохраняем трансформированную модель
            ObjWriter.write(testModel, transformedPath, true);
            System.out.println("Трансформированная модель сохранена в: " + transformedPath);
            
            // Проверяем размеры файлов
            long originalSize = Files.size(Paths.get(originalPath));
            long transformedSize = Files.size(Paths.get(transformedPath));
            
            System.out.println("Размер файла исходной модели: " + originalSize + " байт");
            System.out.println("Размер файла трансформированной модели: " + transformedSize + " байт");
            
            // Читаем и сравниваем файлы
            String originalContent = Files.readString(Paths.get(originalPath));
            String transformedContent = Files.readString(Paths.get(transformedPath));
            
            boolean filesDifferent = !originalContent.equals(transformedContent);
            System.out.println("Файлы отличаются: " + filesDifferent);
            
            // Очистка временных файлов
            Files.deleteIfExists(Paths.get(originalPath));
            Files.deleteIfExists(Paths.get(transformedPath));
            System.out.println("Временные файлы удалены");
            
        } catch (IOException e) {
            System.err.println("Ошибка при работе с файлами: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Тест 4: Применение трансформаций (финализация)
        System.out.println("\n=== Тест 4: Применение трансформаций ===");
        
        System.out.println("Состояние до применения: " + controller);
        System.out.println("Количество вершин до применения: " + testModel.getVertices().size());
        
        // Добавляем слушателя для отслеживания применения трансформаций
        controller.addTransformChangeListener(new ModelTransformController.TransformChangeListener() {
            @Override
            public void onTransformChanged(ModelTransformController controller) {
                System.out.println("Трансформация изменена: " + controller);
            }
            
            @Override
            public void onTransformApplied(Model model) {
                System.out.println("Трансформации применены к модели");
                System.out.println("Новая модель: " + model.getName());
            }
        });
        
        // Применяем трансформации
        controller.applyTransformation();
        
        System.out.println("Состояние после применения: " + controller);
        System.out.println("Количество вершин после применения: " + testModel.getVertices().size());
        
        // Тест 5: Сброс трансформаций
        System.out.println("\n=== Тест 5: Сброс трансформаций ===");
        
        controller.resetTransformation();
        System.out.println("После сброса: " + controller);
        
        System.out.println("\n=== Тестирование завершено ===");
    }
    
    // Создание простой тестовой модели (тетраэдр)
    private static Model createTestModel(String name) {
        Model model = new Model(name);
        
        // Вершины тетраэдра
        model.getVertices().add(new Vector3f(0.0f, 1.0f, 0.0f));     // Верхняя вершина
        model.getVertices().add(new Vector3f(-1.0f, -1.0f, 1.0f));  // Передняя левая
        model.getVertices().add(new Vector3f(1.0f, -1.0f, 1.0f));   // Передняя правая
        model.getVertices().add(new Vector3f(0.0f, -1.0f, -1.0f));  // Задняя
        
        // Грани тетраэдра
        addTriangle(model, 0, 1, 2); // Передняя грань
        addTriangle(model, 0, 2, 3); // Правая грань
        addTriangle(model, 0, 3, 1); // Левая грань
        addTriangle(model, 1, 3, 2); // Нижняя грань
        
        return model;
    }
    
    private static void addTriangle(Model model, int v1, int v2, int v3) {
        Polygon poly = new Polygon();
        ArrayList<Integer> indices = new ArrayList<>();
        indices.add(v1);
        indices.add(v2);
        indices.add(v3);
        poly.setVertexIndices(indices);
        model.getPolygons().add(poly);
    }
}
