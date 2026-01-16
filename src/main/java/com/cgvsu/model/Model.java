package com.cgvsu.model;

import com.cgvsu.math.Transformation;
import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.triangulation.Triangulation;
import com.cgvsu.normalization.Normalization;

import java.util.*;


public class Model {
    private ArrayList<Vector3f> vertices = new ArrayList<Vector3f>();
    private ArrayList<Vector2f> textureVertices = new ArrayList<Vector2f>();
    private ArrayList<Vector3f> normals = new ArrayList<Vector3f>();
    private ArrayList<Polygon> polygons = new ArrayList<Polygon>();

    // Трансформация модели (опционально)
    private Transformation transformation;
    private String name;

    public Model() {
        this.transformation = new Transformation();
        this.name = "Unnamed Model";
    }

    public Model(String name) {
        this();
        this.name = name;
    }

    // Применение трансформации к модели
    public Model applyTransformation(boolean includeInExport) {
        if (!includeInExport || transformation == null) {
            return this;
        }

        Model transformed = new Model(this.name + " (transformed)");

        // Трансформируем вершины
        for (Vector3f vertex : vertices) {
            transformed.getVertices().add(transformation.transform(vertex));
        }

        // Копируем текстурные координаты (не трансформируются)
        transformed.setTextureVertices(new ArrayList<>(textureVertices));

        // Трансформируем нормали
        for (Vector3f normal : normals) {
            transformed.getNormals().add(transformation.transformNormal(normal));
        }

        // Копируем полигоны
        transformed.setPolygons(new ArrayList<>(polygons));

        return transformed;
    }

    // Создание копии модели
    public Model copy() {
        Model copy = new Model(this.name);

        // Копируем вершины
        for (Vector3f vertex : vertices) {
            copy.getVertices().add(new Vector3f(vertex.getX(), vertex.getY(), vertex.getZ()));
        }

        // Копируем текстурные координаты
        if (textureVertices != null) {
            for (Vector2f texVert : textureVertices) {
                copy.getTextureVertices().add(new Vector2f(texVert.getX(), texVert.getY()));
            }
        }

        // Копируем нормали
        if (normals != null) {
            for (Vector3f normal : normals) {
                copy.getNormals().add(new Vector3f(normal.getX(), normal.getY(), normal.getZ()));
            }
        }

        // Копируем полигоны (нужно глубокое копирование)
        for (Polygon poly : polygons) {
            Polygon polyCopy = new Polygon();
            polyCopy.setVertexIndices(new ArrayList<>(poly.getVertexIndices()));

            if (poly.getTextureVertexIndices() != null) {
                polyCopy.setTextureVertexIndices(new ArrayList<>(poly.getTextureVertexIndices()));
            }

            if (poly.getNormalIndices() != null) {
                polyCopy.setNormalIndices(new ArrayList<>(poly.getNormalIndices()));
            }

            copy.getPolygons().add(polyCopy);
        }

        return copy;
    }


    public void normalizationPolygons() {
        normals = Normalization.getVertexNormals(vertices, polygons);
    }

    public void triangulatePolygons() {
        polygons = Triangulation.triangulate(polygons);
    }


    // Геттеры и сеттеры
    public ArrayList<Vector3f> getVertices() {
        return vertices;
    }

    public void setVertices(ArrayList<Vector3f> vertices) {
        this.vertices = vertices;
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
