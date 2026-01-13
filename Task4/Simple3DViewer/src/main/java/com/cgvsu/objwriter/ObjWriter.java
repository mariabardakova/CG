package com.cgvsu.objwriter;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public class ObjWriter {

    public static void write(Model model, String filePath) throws IOException {
        write(model, Path.of(filePath));
    }

    public static void write(Model model, Path outputPath) throws IOException {
        validateArguments(model, outputPath);

        ModelValidator.validate(model);
        String content = modelToString(model);
        Files.writeString(outputPath, content);
    }

    public static String modelToString(Model model) {
        return modelToString(model, "Exported by Team CG&Geom");
    }

    public static String modelToString(Model model, String comment) {
        validateArguments(model);

        ModelValidator.validate(model);
        return buildObjContent(model, comment);
    }

    private static void validateArguments(Model model) {
        if (model == null) {
            throw new IllegalArgumentException("Model cannot be null");
        }
    }

    private static void validateArguments(Model model, Path outputPath) {
        validateArguments(model);
        if (outputPath == null) {
            throw new IllegalArgumentException("Output path cannot be null");
        }
    }

    private static String buildObjContent(Model model, String comment) {
        StringBuilder sb = new StringBuilder();

        appendComment(sb, comment);

        if (model.getName() != null && !model.getName().trim().isEmpty()) {
            sb.append("o ").append(model.getName().trim()).append('\n');
        }

        appendVertices(sb, model.getVertices());

        appendTextureVertices(sb, model.getTextureVertices());

        appendNormals(sb, model.getNormals());

        appendPolygons(sb, model.getPolygons());

        return sb.toString();
    }

    private static void appendComment(StringBuilder sb, String comment) {
        if (comment != null && !comment.trim().isEmpty()) {
            sb.append("# ").append(comment.trim()).append('\n');
        }
    }

    private static void appendVertices(StringBuilder sb, List<Vector3f> vertices) {
        if (vertices == null || vertices.isEmpty()) return;

        for (Vector3f vertex : vertices) {
            sb.append("v ")
                    .append(formatFloat(vertex.getX())).append(' ')
                    .append(formatFloat(vertex.getY())).append(' ')
                    .append(formatFloat(vertex.getZ())).append('\n');
        }
    }

    private static void appendTextureVertices(StringBuilder sb, List<Vector2f> textureVertices) {
        if (textureVertices == null || textureVertices.isEmpty()) return;

        sb.append('\n');
        for (Vector2f texCoord : textureVertices) {
            sb.append("vt ")
                    .append(formatFloat(texCoord.getX())).append(' ')
                    .append(formatFloat(texCoord.getY())).append('\n');
        }
    }

    private static void appendNormals(StringBuilder sb, List<Vector3f> normals) {
        if (normals == null || normals.isEmpty()) return;

        sb.append('\n');
        for (Vector3f normal : normals) {
            sb.append("vn ")
                    .append(formatFloat(normal.getX())).append(' ')
                    .append(formatFloat(normal.getY())).append(' ')
                    .append(formatFloat(normal.getZ())).append('\n');
        }
    }

    private static void appendPolygons(StringBuilder sb, List<Polygon> polygons) {
        if (polygons == null || polygons.isEmpty()) return;

        sb.append('\n');
        for (Polygon polygon : polygons) {
            appendPolygon(sb, polygon);
        }
    }

    private static void appendPolygon(StringBuilder sb, Polygon polygon) {
        if (polygon == null) return;

        sb.append("f");

        List<Integer> vertexIndices = polygon.getVertexIndices();
        List<Integer> textureIndices = polygon.getTextureVertexIndices();
        List<Integer> normalIndices = polygon.getNormalIndices();

        boolean hasTextures = textureIndices != null && !textureIndices.isEmpty();
        boolean hasNormals = normalIndices != null && !normalIndices.isEmpty();

        if (vertexIndices == null || vertexIndices.isEmpty()) {
            return;
        }

        for (int i = 0; i < vertexIndices.size(); i++) {
            sb.append(' ').append(vertexIndices.get(i) + 1);

            if (hasTextures || hasNormals) {
                sb.append('/');
                if (hasTextures) {
                    sb.append(textureIndices.get(i) + 1);
                }
                if (hasNormals) {
                    sb.append('/');
                    sb.append(normalIndices.get(i) + 1);
                }
            }
        }
        sb.append('\n');
    }

    private static String formatFloat(float value) {
        if (Float.isNaN(value)) return "NaN";
        if (Float.isInfinite(value)) return value > 0 ? "Inf" : "-Inf";

        String formatted = String.format(Locale.ROOT, "%.6f", value);

        if (formatted.contains(".")) {
            formatted = formatted.replaceAll("0*$", "");
            if (formatted.endsWith(".")) {
                formatted = formatted.substring(0, formatted.length() - 1);
            }
        }

        return formatted;
    }
}