package com.cgvsu.render_engine_old;

import java.util.ArrayList;
import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Point2f;
import com.cgvsu.math.Matrix4f;
import javafx.scene.canvas.GraphicsContext;
import com.cgvsu.model.Model;
import javafx.scene.paint.Color;

import static com.cgvsu.render_engine_old.GraphicConveyor.*;

public class RenderEngine {

    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final int width,
            final int height,
            final boolean showVertices,
            final Integer highlightedPolygonIndex,
            final Integer highlightedVertexIndex)
    {
        Matrix4f modelMatrix = rotateScaleTranslate();
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        Matrix4f modelViewProjectionMatrix = new Matrix4f(modelMatrix);
        modelViewProjectionMatrix = modelViewProjectionMatrix.mul(viewMatrix);
        modelViewProjectionMatrix = modelViewProjectionMatrix.mul(projectionMatrix);

        final int nPolygons = mesh.getPolygons().size();
        for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
            final int nVerticesInPolygon = mesh.getPolygons().get(polygonInd).getVertexIndices().size();

            if (highlightedPolygonIndex != null && polygonInd == highlightedPolygonIndex) {
                graphicsContext.setStroke(Color.RED);
                graphicsContext.setLineWidth(2.0);
            } else {
                graphicsContext.setStroke(Color.BLACK);
                graphicsContext.setLineWidth(1.0);
            }

            ArrayList<Point2f> resultPoints = new ArrayList<>();
            for (int vertexInPolygonInd = 0; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
                Vector3f vertex = mesh.getVertices().get(mesh.getPolygons().get(polygonInd).getVertexIndices().get(vertexInPolygonInd));
                Point2f resultPoint = vertexToPoint(multiplyMatrix4ByVector3(modelViewProjectionMatrix, vertex), width, height);
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
        if (showVertices && mesh.getVertices() != null) {
            for (int i = 0; i < mesh.getVertices().size(); i++) {
                Vector3f vertex = mesh.getVertices().get(i);
                Point2f screenPoint = vertexToPoint(multiplyMatrix4ByVector3(modelViewProjectionMatrix, vertex), width, height);
                if (Float.isFinite(screenPoint.x) && Float.isFinite(screenPoint.y)) {
                    if (highlightedVertexIndex != null && i == highlightedVertexIndex) {
                        graphicsContext.setFill(Color.ORANGE);
                        graphicsContext.fillOval(screenPoint.x - 4, screenPoint.y - 4, 8, 8);
                    } else {
                        graphicsContext.setFill(Color.RED);
                        graphicsContext.fillOval(screenPoint.x - 2, screenPoint.y - 2, 4, 4);
                    }
                }
            }
        }
    }
}
