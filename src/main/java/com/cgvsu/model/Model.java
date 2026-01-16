package com.cgvsu.model;

import com.cgvsu.math.Transformation;
import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;

import java.util.*;

public class Model {
    private ArrayList<Vector3f> vertices = new ArrayList<Vector3f>();
    private ArrayList<Vector2f> textureVertices = new ArrayList<Vector2f>();
    private ArrayList<Vector3f> normals = new ArrayList<Vector3f>();
    private ArrayList<Polygon> polygons = new ArrayList<Polygon>();

    // Трансформация модели
    private Transformation transformation;
    private String name;
    
    // Хранение оригинальных вершин для сброса
    private ArrayList<Vector3f> originalVertices;
    
    // Текущие значения трансформаций
    private float translationX = 0.0f;
    private float translationY = 0.0f;
    private float translationZ = 0.0f;
    
    private float rotationX = 0.0f;
    private float rotationY = 0.0f;
    private float rotationZ = 0.0f;
    
    private float scaleX = 1.0f;
    private float scaleY = 1.0f;
    private float scaleZ = 1.0f;

    public Model() {
        this.transformation = new Transformation();
        this.name = "Unnamed Model";
        this.originalVertices = new ArrayList<>();
    }

    public Model(String name) {
        this();
        this.name = name;
    }

    // Сохраняет оригинальные вершины
    public void saveOriginalVertices() {
        if (originalVertices.isEmpty() && !vertices.isEmpty()) {
            for (Vector3f vertex : vertices) {
                originalVertices.add(new Vector3f(vertex.getX(), vertex.getY(), vertex.getZ()));
            }
        }
    }

    // Применение трансформации к модели
    public void applyTransformation() {
        if (transformation == null || originalVertices.isEmpty()) {
            return;
        }

        // Сбрасываем трансформацию и применяем заново
        transformation.reset();
        
        // Порядок важен: Scale -> Rotate -> Translate
        transformation.applyScaling(scaleX, scaleY, scaleZ);
        transformation.applyRotation(rotationX, rotationY, rotationZ);
        transformation.applyTranslation(translationX, translationY, translationZ);
        
        // Обновляем вершины
        updateVerticesFromOriginal();
    }

    public void applyTranslation(float tx, float ty, float tz) {
        Transformation t = this.getTransformation();
        t.applyTranslation(tx, ty, tz);
        // Обновляем вершины модели
        updateVerticesAfterTransformation();
    }

    public void applyRotation(float rx, float ry, float rz) {
        Transformation t = this.getTransformation();
        t.applyRotation(rx, ry, rz);
        updateVerticesAfterTransformation();
    }

    public void applyScaling(float sx, float sy, float sz) {
        Transformation t = this.getTransformation();
        t.applyScaling(sx, sy, sz);
        updateVerticesAfterTransformation();
    }

    public void applyAllTransformations(float tx, float ty, float tz, 
                                        float rx, float ry, float rz, 
                                        float sx, float sy, float sz) {
        Transformation t = this.getTransformation();
        t.reset();
        t.applyScaling(sx, sy, sz);
        t.applyRotation(rx, ry, rz);
        t.applyTranslation(tx, ty, tz);
        updateVerticesAfterTransformation();
    }

    // Вспомогательный метод для обновления вершин
    private void updateVerticesAfterTransformation() {
        if (transformation == null) return;
        
        // Создаем копию исходных вершин
        ArrayList<Vector3f> originalVertices = getVertices();
        ArrayList<Vector3f> transformedVertices = new ArrayList<>();
        
        for (Vector3f vertex : originalVertices) {
            // Применяем трансформацию
            Vector3f transformed = transformation.transform(vertex);
            transformedVertices.add(transformed);
        }
        
        // Заменяем вершины
        setVertices(transformedVertices);
    }

    // Обновляет вершины из оригинальных с учетом трансформации
    private void updateVerticesFromOriginal() {
        if (originalVertices.isEmpty() || transformation == null) {
            return;
        }
        
        vertices.clear();
        for (Vector3f originalVertex : originalVertices) {
            Vector3f transformed = transformation.transform(originalVertex);
            vertices.add(transformed);
        }
    }

    // Установка трансформаций
    public void setTranslation(float x, float y, float z) {
        this.translationX = x;
        this.translationY = y;
        this.translationZ = z;
        applyTransformation();
    }

    public void setRotation(float x, float y, float z) {
        this.rotationX = x;
        this.rotationY = y;
        this.rotationZ = z;
        applyTransformation();
    }

    public void setScaling(float x, float y, float z) {
        this.scaleX = x;
        this.scaleY = y;
        this.scaleZ = z;
        applyTransformation();
    }

    public void setAllTransformations(float tx, float ty, float tz, 
                                     float rx, float ry, float rz, 
                                     float sx, float sy, float sz) {
        this.translationX = tx;
        this.translationY = ty;
        this.translationZ = tz;
        this.rotationX = rx;
        this.rotationY = ry;
        this.rotationZ = rz;
        this.scaleX = sx;
        this.scaleY = sy;
        this.scaleZ = sz;
        applyTransformation();
    }

    // Сброс трансформаций
    public void resetTransformations() {
        this.translationX = 0.0f;
        this.translationY = 0.0f;
        this.translationZ = 0.0f;
        this.rotationX = 0.0f;
        this.rotationY = 0.0f;
        this.rotationZ = 0.0f;
        this.scaleX = 1.0f;
        this.scaleY = 1.0f;
        this.scaleZ = 1.0f;
        
        // Восстанавливаем оригинальные вершины
        if (!originalVertices.isEmpty()) {
            vertices.clear();
            for (Vector3f original : originalVertices) {
                vertices.add(new Vector3f(original.getX(), original.getY(), original.getZ()));
            }
        }
        
        transformation.reset();
    }

    // Геттеры для текущих значений трансформаций
    public float getTranslationX() { return translationX; }
    public float getTranslationY() { return translationY; }
    public float getTranslationZ() { return translationZ; }
    
    public float getRotationX() { return rotationX; }
    public float getRotationY() { return rotationY; }
    public float getRotationZ() { return rotationZ; }
    
    public float getScaleX() { return scaleX; }
    public float getScaleY() { return scaleY; }
    public float getScaleZ() { return scaleZ; }

    // Геттеры и сеттеры
    public ArrayList<Vector3f> getVertices() {
        return vertices;
    }

    public void setVertices(ArrayList<Vector3f> vertices) {
        this.vertices = vertices;
        // Сохраняем оригинальные вершины при первом установке
        if (originalVertices.isEmpty() && !vertices.isEmpty()) {
            saveOriginalVertices();
        }
    }

    public ArrayList<Vector2f> getTextureVertices() {
        return textureVertices;
    }

    public void setTextureVertices(ArrayList<Vector2f> textureVertices) {
        this.textureVertices = textureVertices;
    }

    public ArrayList<Vector3f> getNormals() {
        return normals;
    }

    public void setNormals(ArrayList<Vector3f> normals) {
        this.normals = normals;
    }

    public ArrayList<Polygon> getPolygons() {
        return polygons;
    }

    public void setPolygons(ArrayList<Polygon> polygons) {
        this.polygons = polygons;
    }

    public Transformation getTransformation() {
        return transformation;
    }

    public void setTransformation(Transformation transformation) {
        this.transformation = transformation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("Model[name=%s, vertices=%d, polygons=%d, hasTex=%b, hasNormals=%b]",
                name, vertices.size(), polygons.size(),
                textureVertices != null && !textureVertices.isEmpty(),
                normals != null && !normals.isEmpty());
    }
}