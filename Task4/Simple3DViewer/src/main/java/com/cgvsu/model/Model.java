package com.cgvsu.model;
import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;

import java.util.*;

public class Model {

    private String name = "";

    public ArrayList<Vector3f> vertices = new ArrayList<Vector3f>();
    public ArrayList<Vector2f> textureVertices = new ArrayList<Vector2f>();
    public ArrayList<Vector3f> normals = new ArrayList<Vector3f>();
    public ArrayList<Polygon> polygons = new ArrayList<Polygon>();

    public ArrayList<Vector2f> getTextureVertices() {
        return textureVertices;
    }

    public ArrayList<Vector3f> getVertices() {
        return vertices;
    }

    public ArrayList<Polygon> getPolygons() {
        return polygons;
    }

    public ArrayList<Vector3f> getNormals() {
        return normals;
    }

    public void setName(String name) { this.name = name; }
    public String getName() { return name; }
}
