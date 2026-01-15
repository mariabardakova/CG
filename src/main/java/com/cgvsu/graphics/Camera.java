package com.cgvsu.graphics;


import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector3f;

/*
 * Класс для управления камерой в 3D-пространстве.
 * Поддерживает различные типы проекций и управление положением камеры.
 */
public class Camera {
    private Vector3f position;
    private Vector3f target;
    private Vector3f up;
    
    private float fov;
    private float aspectRatio;
    private float nearPlane;
    private float farPlane;
    
    private CameraType cameraType;
    private Matrix4f viewMatrix;
    private Matrix4f projectionMatrix;
    
    public enum CameraType {
        PERSPECTIVE,
        ORTHOGRAPHIC
    }
    
    public Camera() {
        // Стандартные параметры камеры
        this.position = new Vector3f(0, 0, 5);
        this.target = new Vector3f(0, 0, 0);
        this.up = new Vector3f(0, 1, 0);
        
        this.fov = 60.0f;
        this.aspectRatio = 16.0f / 9.0f;
        this.nearPlane = 0.1f;
        this.farPlane = 100.0f;
        
        this.cameraType = CameraType.PERSPECTIVE;
        
        updateViewMatrix();
        updateProjectionMatrix();
    }
    
    public Camera(Vector3f position, Vector3f target, Vector3f up, 
                  float fov, float aspectRatio, float nearPlane, float farPlane) {
        this.position = position;
        this.target = target;
        this.up = up;
        this.fov = fov;
        this.aspectRatio = aspectRatio;
        this.nearPlane = nearPlane;
        this.farPlane = farPlane;
        this.cameraType = CameraType.PERSPECTIVE;
        
        updateViewMatrix();
        updateProjectionMatrix();
    }
    
    // Обновление матрицы вида
    public void updateViewMatrix() {
        this.viewMatrix = Matrix4f.lookAt(position, target, up);
    }
    
    // Обновление матрицы проекции
    public void updateProjectionMatrix() {
        if (cameraType == CameraType.PERSPECTIVE) {
            this.projectionMatrix = Matrix4f.perspective(fov, aspectRatio, nearPlane, farPlane);
        } else {
            // Для ортографической проекции вычисляем границы на основе расстояния до цели
            float distance = position.distanceTo(target);
            float size = distance * (float)Math.tan(Math.toRadians(fov / 2));
            this.projectionMatrix = Matrix4f.orthographic(
                -size * aspectRatio, size * aspectRatio,
                -size, size,
                nearPlane, farPlane
            );
        }
    }
    
    // Перемещение камеры
    public void move(Vector3f delta) {
        position = position.add(delta);
        target = target.add(delta);
        updateViewMatrix();
    }
    
    // Вращение камеры вокруг цели
    public void rotateAroundTarget(float yaw, float pitch) {
        // Вычисляем радиус-вектор от цели к камере
        Vector3f direction = position.subtract(target);
        
        // Преобразуем углы в радианы
        float yawRad = (float)Math.toRadians(yaw);
        float pitchRad = (float)Math.toRadians(pitch);
        
        // Применяем вращение
        float cosYaw = (float)Math.cos(yawRad);
        float sinYaw = (float)Math.sin(yawRad);
        float cosPitch = (float)Math.cos(pitchRad);
        float sinPitch = (float)Math.sin(pitchRad);
        
        // Вращение вокруг оси Y (горизонтальное)
        float x = direction.getX() * cosYaw - direction.getZ() * sinYaw;
        float z = direction.getX() * sinYaw + direction.getZ() * cosYaw;
        direction = new Vector3f(x, direction.getY(), z);
        
        // Вращение вокруг оси X (вертикальное) с ограничением
        float lengthXY = (float)Math.sqrt(direction.getX() * direction.getX() + 
                                         direction.getZ() * direction.getZ());
        float newPitch = (float)Math.atan2(direction.getY(), lengthXY) + pitchRad;
        
        // Ограничиваем вертикальное вращение, чтобы не перевернуть камеру
        float maxPitch = (float)Math.PI / 2 - 0.1f;
        newPitch = Math.max(-maxPitch, Math.min(maxPitch, newPitch));
        
        float newLength = direction.length();
        direction = new Vector3f(
            (float)(Math.cos(newPitch) * Math.cos(yawRad + Math.atan2(direction.getZ(), direction.getX()))) * newLength,
            (float)Math.sin(newPitch) * newLength,
            (float)(Math.cos(newPitch) * Math.sin(yawRad + Math.atan2(direction.getZ(), direction.getX()))) * newLength
        );
        
        position = target.add(direction);
        updateViewMatrix();
    }
    
    // Приближение/отдаление
    public void zoom(float amount) {
        Vector3f direction = position.subtract(target).normalize();
        float distance = position.distanceTo(target);
        
        // Изменяем расстояние с ограничением
        float newDistance = Math.max(0.1f, Math.min(100.0f, distance + amount));
        
        if (newDistance != distance) {
            position = target.add(direction.multiply(newDistance));
            updateViewMatrix();
        }
    }
    
    // Изменение типа проекции
    public void setCameraType(CameraType type) {
        this.cameraType = type;
        updateProjectionMatrix();
    }
    
    // Изменение параметров проекции
    public void setProjectionParams(float fov, float aspectRatio, float nearPlane, float farPlane) {
        this.fov = fov;
        this.aspectRatio = aspectRatio;
        this.nearPlane = nearPlane;
        this.farPlane = farPlane;
        updateProjectionMatrix();
    }
    
    // Геттеры
    public Vector3f getPosition() { return position; }
    public Vector3f getTarget() { return target; }
    public Vector3f getUp() { return up; }
    public float getFov() { return fov; }
    public float getAspectRatio() { return aspectRatio; }
    public float getNearPlane() { return nearPlane; }
    public float getFarPlane() { return farPlane; }
    public CameraType getCameraType() { return cameraType; }
    public Matrix4f getViewMatrix() { return viewMatrix; }
    public Matrix4f getProjectionMatrix() { return projectionMatrix; }
    
    // Сеттеры
    public void setPosition(Vector3f position) { 
        this.position = position; 
        updateViewMatrix();
    }
    
    public void setTarget(Vector3f target) { 
        this.target = target; 
        updateViewMatrix();
    }
    
    public void setUp(Vector3f up) { 
        this.up = up; 
        updateViewMatrix();
    }
    
    public void setFov(float fov) { 
        this.fov = fov; 
        updateProjectionMatrix();
    }
    
    public void setAspectRatio(float aspectRatio) { 
        this.aspectRatio = aspectRatio; 
        updateProjectionMatrix();
    }
    
    public void setNearPlane(float nearPlane) { 
        this.nearPlane = nearPlane; 
        updateProjectionMatrix();
    }
    
    public void setFarPlane(float farPlane) { 
        this.farPlane = farPlane; 
        updateProjectionMatrix();
    }
    
    @Override
    public String toString() {
        return String.format("Camera[pos=%s, target=%s, fov=%.1f, aspect=%.2f, near=%.2f, far=%.2f]",
                position, target, fov, aspectRatio, nearPlane, farPlane);
    }
}
