package com.cgvsu.objwriter;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.util.List;

public final class ModelValidator {

    private ModelValidator() {}

    public static void validate(Model model) {
        validateModelNotNull(model);

        List<Vector3f> vertices = model.getVertices();
        List<Vector2f> textureVertices = model.getTextureVertices();
        List<Vector3f> normals = model.getNormals();
        List<Polygon> polygons = model.getPolygons();

        validateVertices(vertices);
        validateTextureVertices(textureVertices);
        validateNormals(normals);
        validatePolygons(polygons, vertices, textureVertices, normals);
    }

    private static void validateModelNotNull(Model model) {
        if (model == null) {
            throw new ObjWriterException("Model cannot be null");
        }
    }

    private static void validateVertices(List<Vector3f> vertices) {
        if (vertices == null) {
            throw new ObjWriterException("Model vertices list cannot be null");
        }
        if (vertices.isEmpty()) {
            throw new ObjWriterException("Model must have at least one vertex");
        }

        for (int i = 0; i < vertices.size(); i++) {
            Vector3f vertex = vertices.get(i);
            if (vertex == null) {
                throw new ObjWriterException("Vertex at index " + i + " is null");
            }
            validateFloatValues(vertex.getX(), vertex.getY(), vertex.getZ(), "Vertex " + i);
        }
    }

    private static void validateTextureVertices(List<Vector2f> textureVertices) {
        if (textureVertices == null) {
            return;
        }

        for (int i = 0; i < textureVertices.size(); i++) {
            Vector2f texCoord = textureVertices.get(i);
            if (texCoord == null) {
                throw new ObjWriterException("Texture vertex at index " + i + " is null");
            }
            validateFloatValues(texCoord.getX(), texCoord.getY(), "Texture vertex " + i);
        }
    }

    private static void validateNormals(List<Vector3f> normals) {
        if (normals == null) {
            return;
        }

        for (int i = 0; i < normals.size(); i++) {
            Vector3f normal = normals.get(i);
            if (normal == null) {
                throw new ObjWriterException("Normal at index " + i + " is null");
            }
            validateFloatValues(normal.getX(), normal.getY(), normal.getZ(), "Normal " + i);

            validateNormalLength(normal, i);
        }
    }

    private static void validatePolygons(List<Polygon> polygons, List<Vector3f> vertices,
                                         List<Vector2f> textureVertices, List<Vector3f> normals) {
        if (polygons == null) {
            throw new ObjWriterException("Model polygons list cannot be null");
        }
        if (polygons.isEmpty()) {
            throw new ObjWriterException("Model must have at least one polygon");
        }

        int vertexCount = vertices.size();
        int textureCount = textureVertices != null ? textureVertices.size() : 0;
        int normalCount = normals != null ? normals.size() : 0;

        for (int i = 0; i < polygons.size(); i++) {
            Polygon polygon = polygons.get(i);
            validatePolygon(polygon, i, vertexCount, textureCount, normalCount);
        }
    }

    private static void validatePolygon(Polygon polygon, int polygonIndex,
                                        int vertexCount, int textureCount, int normalCount) {
        if (polygon == null) {
            throw new ObjWriterException("Polygon at index " + polygonIndex + " is null");
        }

        List<Integer> vertexIndices = polygon.getVertexIndices();
        List<Integer> textureIndices = polygon.getTextureVertexIndices();
        List<Integer> normalIndices = polygon.getNormalIndices();

        validatePolygonVertexIndices(vertexIndices, polygonIndex, vertexCount);

        if (textureIndices != null && !textureIndices.isEmpty()) {
            validatePolygonTextureIndices(vertexIndices, textureIndices, polygonIndex, textureCount);
        }

        if (normalIndices != null && !normalIndices.isEmpty()) {
            validatePolygonNormalIndices(vertexIndices, normalIndices, polygonIndex, normalCount);
        }
    }

    private static void validatePolygonVertexIndices(List<Integer> vertexIndices, int polygonIndex, int vertexCount) {
        if (vertexIndices == null) {
            throw new ObjWriterException("Polygon " + polygonIndex + " has null vertex indices");
        }
        if (vertexIndices.isEmpty()) {
            throw new ObjWriterException("Polygon " + polygonIndex + " has no vertices");
        }
        if (vertexIndices.size() < 3) {
            throw new ObjWriterException("Polygon " + polygonIndex + " has less than 3 vertices");
        }

        for (int vertexIndex : vertexIndices) {
            if (vertexIndex < 0 || vertexIndex >= vertexCount) {
                throw new ObjWriterException(
                        "Polygon %d: invalid vertex index %d (valid: 0–%d)",
                        polygonIndex, vertexIndex, vertexCount - 1);
            }
        }
    }

    private static void validatePolygonTextureIndices(List<Integer> vertexIndices, List<Integer> textureIndices,
                                                      int polygonIndex, int textureCount) {
        if (textureIndices.size() != vertexIndices.size()) {
            throw new ObjWriterException(
                    "Polygon " + polygonIndex + ": vertex and texture index counts differ (" +
                            vertexIndices.size() + " vs " + textureIndices.size() + ")");
        }

        for (int textureIndex : textureIndices) {
            if (textureIndex < 0 || textureIndex >= textureCount) {
                throw new ObjWriterException(
                        "Polygon %d: invalid texture index %d (valid: 0–%d)",
                        polygonIndex, textureIndex, textureCount - 1);
            }
        }
    }

    private static void validatePolygonNormalIndices(List<Integer> vertexIndices, List<Integer> normalIndices,
                                                     int polygonIndex, int normalCount) {
        if (normalIndices.size() != vertexIndices.size()) {
            throw new ObjWriterException(
                    "Polygon " + polygonIndex + ": vertex and normal index counts differ (" +
                            vertexIndices.size() + " vs " + normalIndices.size() + ")");
        }

        for (int normalIndex : normalIndices) {
            if (normalIndex < 0 || normalIndex >= normalCount) {
                throw new ObjWriterException(
                        "Polygon %d: invalid normal index %d (valid: 0–%d)",
                        polygonIndex, normalIndex, normalCount - 1);
            }
        }
    }

    private static void validateFloatValues(float x, float y, String context) {
        if (Float.isNaN(x) || Float.isNaN(y)) {
            throw new ObjWriterException(context + " contains NaN values");
        }
        if (Float.isInfinite(x) || Float.isInfinite(y)) {
            throw new ObjWriterException(context + " contains infinite values");
        }
    }

    private static void validateFloatValues(float x, float y, float z, String context) {
        if (Float.isNaN(x) || Float.isNaN(y) || Float.isNaN(z)) {
            throw new ObjWriterException(context + " contains NaN values");
        }
        if (Float.isInfinite(x) || Float.isInfinite(y) || Float.isInfinite(z)) {
            throw new ObjWriterException(context + " contains infinite values");
        }
    }

    private static void validateNormalLength(Vector3f normal, int index) {
        float lengthSquared = normal.getX() * normal.getX() +
                normal.getY() * normal.getY() +
                normal.getZ() * normal.getZ();

        if (Math.abs(lengthSquared - 1.0f) > 0.01f) {
            throw new ObjWriterException(
                    "Normal at index %d is not normalized (length squared: %.6f)",
                    index, lengthSquared);
        }
    }
}