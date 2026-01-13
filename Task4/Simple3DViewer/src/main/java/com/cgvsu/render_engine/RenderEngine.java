package com.cgvsu.render_engine;

import java.util.ArrayList;

import com.cgvsu.math.Vector3f;
import javafx.scene.canvas.GraphicsContext;
import javax.vecmath.*;
import com.cgvsu.model.Model;
import javafx.scene.paint.Color;

import static com.cgvsu.render_engine.GraphicConveyor.*;

public class RenderEngine {

    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final int width,
            final int height,
            final boolean showVertices,
            final Integer highlightedPolygonIndex)
    {
        Matrix4f modelMatrix = rotateScaleTranslate();
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        Matrix4f modelViewProjectionMatrix = new Matrix4f(modelMatrix);
        modelViewProjectionMatrix.mul(viewMatrix);
        modelViewProjectionMatrix.mul(projectionMatrix);

        final int nPolygons = mesh.polygons.size();
        for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
            final int nVerticesInPolygon = mesh.polygons.get(polygonInd).getVertexIndices().size();

            ArrayList<Point2f> resultPoints = new ArrayList<>();
            for (int vertexInPolygonInd = 0; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
                Vector3f vertex = mesh.vertices.get(mesh.polygons.get(polygonInd).getVertexIndices().get(vertexInPolygonInd));

                if (highlightedPolygonIndex != null && polygonInd == highlightedPolygonIndex) {
                    graphicsContext.setStroke(javafx.scene.paint.Color.RED);
                    graphicsContext.setLineWidth(2.0); // можно сделать толще
                } else {
                    graphicsContext.setStroke(javafx.scene.paint.Color.BLACK);
                    graphicsContext.setLineWidth(1.0);
                }

                javax.vecmath.Vector3f vertexVecmath = new javax.vecmath.Vector3f(vertex.x, vertex.y, vertex.z);

                Point2f resultPoint = vertexToPoint(multiplyMatrix4ByVector3(modelViewProjectionMatrix, vertexVecmath), width, height);
                resultPoints.add(resultPoint);
            }

            for (int vertexInPolygonInd = 1; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
                graphicsContext.strokeLine(
                        resultPoints.get(vertexInPolygonInd - 1).x,
                        resultPoints.get(vertexInPolygonInd - 1).y,
                        resultPoints.get(vertexInPolygonInd).x,
                        resultPoints.get(vertexInPolygonInd).y);
            }

            if (nVerticesInPolygon > 0)
                graphicsContext.strokeLine(
                        resultPoints.get(nVerticesInPolygon - 1).x,
                        resultPoints.get(nVerticesInPolygon - 1).y,
                        resultPoints.get(0).x,
                        resultPoints.get(0).y);
        }
        if (showVertices && mesh.vertices !=null){
            Matrix4f modelMatrixNew = rotateScaleTranslate();
            Matrix4f viewMatrixNew = camera.getViewMatrix();
            Matrix4f projectionMatrixNew = camera.getProjectionMatrix();

            Matrix4f mvl = new Matrix4f(modelMatrixNew);
            mvl.mul(viewMatrixNew);
            mvl.mul(projectionMatrixNew);

            graphicsContext.setFill(Color.RED);
            for(Vector3f vertex : mesh.vertices){
                javax.vecmath.Vector3f v = new javax.vecmath.Vector3f(vertex.x, vertex.y, vertex.z);
                Point2f screenPoint = vertexToPoint(multiplyMatrix4ByVector3(mvl, v), width, height);
                if (Float.isFinite(screenPoint.x) && Float.isFinite(screenPoint.y)) {
                    double size = 3.0;
                    graphicsContext.fillOval(
                            screenPoint.x - size / 2,
                            screenPoint.y - size / 2,
                            size,
                            size
                    );
                }
            }
        }

    }
}