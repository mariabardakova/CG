package com.cgvsu.triangulation;

import com.cgvsu.model.Polygon;

import java.util.ArrayList;
import java.util.List;

public class Triangulation {
    public static ArrayList<Polygon> triangulate(ArrayList<Polygon> polygons) {
        ArrayList<Polygon> result = new ArrayList<>();

        for (Polygon polygon : polygons) {
            List<Integer> vertices = polygon.getVertexIndices();

            if (vertices.size() < 3) {
                continue;
            }

            for (int i = 1; i < vertices.size() - 1; i++) {
                Polygon triangle = new Polygon();

                ArrayList<Integer> triangleVertices = new ArrayList<>();
                triangleVertices.add(vertices.get(0));
                triangleVertices.add(vertices.get(i));
                triangleVertices.add(vertices.get(i + 1));

                triangle.setVertexIndices(triangleVertices);
                result.add(triangle);
            }
        }
        return result;
    }
}
