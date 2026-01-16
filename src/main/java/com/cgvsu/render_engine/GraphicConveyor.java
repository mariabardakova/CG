package com.cgvsu.render_engine;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Point2f;
import com.cgvsu.math.Vector3f;

public class GraphicConveyor {

    public static Matrix4f rotateScaleTranslate() {
        return Matrix4f.identity();
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f target) {
        return lookAt(eye, target, new Vector3f(0F, 1.0F, 0F));
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f target, Vector3f up) {
        return Matrix4f.lookAt(eye, target, up);
    }

    public static Matrix4f perspective(
            final float fov,
            final float aspectRatio,
            final float nearPlane,
            final float farPlane) {
        return Matrix4f.perspective(fov, aspectRatio, nearPlane, farPlane);
    }

    public static Vector3f multiplyMatrix4ByVector3(final Matrix4f matrix, final Vector3f vertex) {
        final float x = (vertex.x * matrix.get(0, 0)) + (vertex.y * matrix.get(1, 0)) + (vertex.z * matrix.get(2, 0)) + matrix.get(3, 0);
        final float y = (vertex.x * matrix.get(0, 1)) + (vertex.y * matrix.get(1, 1)) + (vertex.z * matrix.get(2, 1)) + matrix.get(3, 1);
        final float z = (vertex.x * matrix.get(0, 2)) + (vertex.y * matrix.get(1, 2)) + (vertex.z * matrix.get(2, 2)) + matrix.get(3, 2);
        final float w = (vertex.x * matrix.get(0, 3)) + (vertex.y * matrix.get(1, 3)) + (vertex.z * matrix.get(2, 3)) + matrix.get(3, 3);
        return new Vector3f(x / w, y / w, z / w);
    }

    public static Point2f vertexToPoint(final Vector3f vertex, final int width, final int height) {
        return new Point2f(vertex.x * width + width / 2.0F, -vertex.y * height + height / 2.0F);
    }
}
