package com.cgvsu.normalization;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Polygon;
import java.util.ArrayList;
import java.util.List;

public class Normalization {

    public static List<Vector3f> getVertexNormals(
            final List<Vector3f> vertexes,
            final List<Polygon> polygons) {

        List<Vector3f> normals = new ArrayList<>();
        for (int i = 0; i < vertexes.size(); i++) {
            normals.add(new Vector3f(0, 0, 0));
        }

        for (Polygon polygon : polygons) {
            List<Integer> indices = polygon.getVertexIndices();

            Vector3f vertex0 = vertexes.get(indices.get(0));
            Vector3f vertex1 = vertexes.get(indices.get(1));
            Vector3f vertex2 = vertexes.get(indices.get(2));

            Vector3f triangleNormale = getTriangleNormale(vertex0, vertex1, vertex2);

            for (int vertexIndex : indices) {
                normals.set(vertexIndex, normals.get(vertexIndex).add(triangleNormale));
            }
        }

        for (int i = 0; i < normals.size(); i++) {
            Vector3f normal = normals.get(i);
            if (normal.length() > 0) {
                normals.set(i, normal.normalize());
            }
        }

        return normals;
    }

    private static Vector3f getTriangleNormale(Vector3f genv, Vector3f v2, Vector3f v3) {
        Vector3f a = genv.subtract(v2);
        Vector3f b = genv.subtract(v3);
        return a.cross(b);
    }

}
