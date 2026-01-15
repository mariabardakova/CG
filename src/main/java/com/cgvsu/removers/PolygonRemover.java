package com.cgvsu.removers;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.*;
import java.util.*;

public class PolygonRemover {

    public static Model removePolygons(Model model,
                                       List<Integer> polygonIndicesToRemove,
                                       boolean removeOrphanedVertices) {
        if (model == null) {
            throw new IllegalArgumentException("Модель не может быть null");
        }

        ElementUsageInfo originallyOrphaned = null;
        if (removeOrphanedVertices) {
            originallyOrphaned = findOriginallyOrphanedElements(model);
        }

        removeSelectedPolygons(model, polygonIndicesToRemove);

        if (removeOrphanedVertices) {
            removeNewlyOrphanedElements(model, originallyOrphaned);
        }

        return model;
    }

    private static class ElementUsageInfo {
        Set<Integer> usedVertices = new HashSet<>();
        Set<Integer> usedTextures = new HashSet<>();
        Set<Integer> usedNormals = new HashSet<>();
        Set<Integer> orphanedVertices = new HashSet<>();
        Set<Integer> orphanedTextures = new HashSet<>();
        Set<Integer> orphanedNormals = new HashSet<>();

        void collectFromPolygon(Polygon polygon) {
            if (polygon == null) return;

            if (polygon.getVertexIndices() != null) {
                usedVertices.addAll(polygon.getVertexIndices());
            }

            if (polygon.getTextureVertexIndices() != null) {
                usedTextures.addAll(polygon.getTextureVertexIndices());
            }

            if (polygon.getNormalIndices() != null) {
                usedNormals.addAll(polygon.getNormalIndices());
            }
        }
    }

    private static void findOrphanedElements(List<?> elements,
                                             Set<Integer> usedIndices,
                                             Set<Integer> orphanedIndices) {
        for (int i = 0; i < elements.size(); i++) {
            if (!usedIndices.contains(i)) {
                orphanedIndices.add(i);
            }
        }
    }

    private static ElementUsageInfo findOriginallyOrphanedElements(Model model) {
        ElementUsageInfo info = new ElementUsageInfo();

        for (Polygon polygon : model.getPolygons()) {
            info.collectFromPolygon(polygon);
        }
        findOrphanedElements(model.getVertices(), info.usedVertices, info.orphanedVertices);

        if (model.getTextureVertices() != null) {
            findOrphanedElements(model.getTextureVertices(), info.usedTextures, info.orphanedTextures);
        }

        if (model.getNormals() != null) {
            findOrphanedElements(model.getNormals(), info.usedNormals, info.orphanedNormals);
        }

        return info;
    }

    private static void removeSelectedPolygons(Model model, List<Integer> indicesToRemove) {
        List<Polygon> polygons = model.getPolygons();
        if (indicesToRemove == null || indicesToRemove.isEmpty()) {
            return;
        }

        List<Integer> sortedIndices = new ArrayList<>(indicesToRemove);
        sortedIndices.sort(Collections.reverseOrder());

        for (int index : sortedIndices) {
            if (index >= 0 && index < polygons.size()) {
                polygons.remove(index);
            }
        }
    }

    private static <T> RebuildResult<T> rebuildElementList(List<T> elements,
                                                           Set<Integer> usedIndices,
                                                           Set<Integer> skipIndices) {
        List<T> newList = new ArrayList<>();
        List<Integer> indexMap = new ArrayList<>(Collections.nCopies(elements.size(), -1));

        for (int i = 0; i < elements.size(); i++) {
            boolean shouldKeep = usedIndices.contains(i) ||
                    (skipIndices != null && skipIndices.contains(i));
            if (shouldKeep) {
                indexMap.set(i, newList.size());
                newList.add(elements.get(i));
            }
        }

        return new RebuildResult<>(newList, indexMap);
    }

    private static ArrayList<Integer> remapIndices(List<Integer> indices, List<Integer> indexMap) {
        if (indices == null || indexMap == null) {
            return null;
        }

        ArrayList<Integer> newIndices = new ArrayList<>();
        for (int oldIndex : indices) {
            if (oldIndex >= 0 && oldIndex < indexMap.size()) {
                int newIndex = indexMap.get(oldIndex);
                if (newIndex != -1) {
                    newIndices.add(newIndex);
                }
            }
        }
        return newIndices;
    }

    private static void removeNewlyOrphanedElements(Model model, ElementUsageInfo skipInfo) {
        if (skipInfo == null) {
            skipInfo = new ElementUsageInfo();
        }

        ElementUsageInfo currentUsage = new ElementUsageInfo();
        for (Polygon polygon : model.getPolygons()) {
            currentUsage.collectFromPolygon(polygon);
        }

        RebuildResult<Vector3f> vertexResult = rebuildElementList(
                model.getVertices(), currentUsage.usedVertices, skipInfo.orphanedVertices
        );
        model.setVertices(new ArrayList<>(vertexResult.newList));

        RebuildResult<Vector2f> textureResult = null;
        if (model.getTextureVertices() != null && !model.getTextureVertices().isEmpty()) {
            textureResult = rebuildElementList(
                    model.getTextureVertices(), currentUsage.usedTextures, skipInfo.orphanedTextures
            );
            model.setTextureVertices(new ArrayList<>(textureResult.newList));
        } else {
            model.setTextureVertices(null);
        }

        RebuildResult<Vector3f> normalResult = null;
        if (model.getNormals() != null && !model.getNormals().isEmpty()) {
            normalResult = rebuildElementList(
                    model.getNormals(), currentUsage.usedNormals, skipInfo.orphanedNormals
            );
            model.setNormals(new ArrayList<>(normalResult.newList));
        } else {
            model.setNormals(null);
        }

        for (Polygon polygon : model.getPolygons()) {
            polygon.setVertexIndices(remapIndices(polygon.getVertexIndices(), vertexResult.indexMap));
            polygon.setTextureVertexIndices(remapIndices(polygon.getTextureVertexIndices(),
                    textureResult != null ? textureResult.indexMap : null));
            polygon.setNormalIndices(remapIndices(polygon.getNormalIndices(),
                    normalResult != null ? normalResult.indexMap : null));
        }
    }

    private static class RebuildResult<T> {
        final List<T> newList;
        final List<Integer> indexMap;

        RebuildResult(List<T> newList, List<Integer> indexMap) {
            this.newList = newList;
            this.indexMap = indexMap;
        }
    }
}