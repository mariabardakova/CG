package com.cgvsu.render_engine;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector3f;

public class Camera {
    public enum CameraMode {
        FIXED,          // Фиксированная камера (режим 1)
        FREE_FLIGHT,    // Свободный полет (режим 2)
        ORBIT           // Вращение вокруг цели (режим 3)
    }

    private CameraMode mode = CameraMode.FIXED;
    
    // Основные параметры камеры
    private Vector3f position;
    private Vector3f target;
    private Vector3f up;
    private float fov;
    private float aspectRatio;
    private float nearPlane;
    private float farPlane;
    
    // Параметры для орбитальной камеры (вращение вокруг цели)
    private float orbitYaw = 0.0f;        // Горизонтальный угол орбиты
    private float orbitPitch = 30.0f;     // Вертикальный угол орбиты
    private float orbitDistance = 100.0f; // Расстояние до цели
    
    // Параметры для свободной камеры
    private float freeYaw = -90.0f;       // Горизонтальный угол для свободной камеры
    private float freePitch = 0.0f;       // Вертикальный угол для свободной камеры
    
    // Чувствительность управления
    private float mouseSensitivity = 0.3f;
    private float movementSpeed = 10.0f;
    private float zoomSensitivity = 0.2f;

    public Camera(
            final Vector3f position,
            final Vector3f target,
            final float fov,
            final float aspectRatio,
            final float nearPlane,
            final float farPlane) {
        this.position = position;
        this.target = target;
        this.up = new Vector3f(0.0F, 1.0F, 0.0F);
        this.fov = fov;
        this.aspectRatio = aspectRatio;
        this.nearPlane = nearPlane;
        this.farPlane = farPlane;
        
        // Инициализация параметров орбитальной камеры
        updateOrbitParametersFromPosition();
        
        // Инициализация параметров свободной камеры
        updateFreeCameraAngles();
    }

    public void setMode(CameraMode mode) {
        CameraMode oldMode = this.mode;
        this.mode = mode;
        
        if (mode == CameraMode.ORBIT && oldMode != CameraMode.ORBIT) {
            // При переходе в режим орбиты обновляем параметры орбиты
            updateOrbitParametersFromPosition();
        } else if (mode == CameraMode.FREE_FLIGHT && oldMode != CameraMode.FREE_FLIGHT) {
            // При переходе в режим свободного полета обновляем углы свободной камеры
            updateFreeCameraAngles();
        }
    }

    public CameraMode getMode() {
        return mode;
    }

    // Обновление параметров орбиты из текущей позиции камеры
    private void updateOrbitParametersFromPosition() {
        // Вычисляем вектор от цели к камере
        Vector3f delta = position.subtract(target);
        orbitDistance = delta.length();
        
        if (orbitDistance > 0) {
            // Нормализуем вектор
            delta = delta.normalize();
            
            // Вычисляем углы в сферических координатах
            // Yaw - угол в горизонтальной плоскости (XZ)
            orbitYaw = (float)Math.toDegrees(Math.atan2(delta.getZ(), delta.getX()));
            
            // Pitch - угол между вектором и горизонтальной плоскостью
            float horizontalDistance = (float)Math.sqrt(delta.getX() * delta.getX() + delta.getZ() * delta.getZ());
            orbitPitch = (float)Math.toDegrees(Math.atan2(delta.getY(), horizontalDistance));
        }
    }

    // Обновление углов свободной камеры из текущего направления взгляда
    private void updateFreeCameraAngles() {
        // Вычисляем направление взгляда (от камеры к цели)
        Vector3f direction = target.subtract(position);
        float length = direction.length();
        
        if (length > 0) {
            direction = direction.normalize();
            
            // Вычисляем углы в сферических координатах
            freeYaw = (float)Math.toDegrees(Math.atan2(direction.getZ(), direction.getX()));
            
            float horizontalDistance = (float)Math.sqrt(direction.getX() * direction.getX() + direction.getZ() * direction.getZ());
            freePitch = (float)Math.toDegrees(Math.atan2(direction.getY(), horizontalDistance));
        }
    }

    // Обновление позиции камеры для орбитального режима
    private void updateOrbitCameraPosition() {
        // Преобразуем углы из градусов в радианы
        float yawRad = (float)Math.toRadians(orbitYaw);
        float pitchRad = (float)Math.toRadians(orbitPitch);
        
        // Вычисляем позицию камеры в сферических координатах
        // Камера вращается ВОКРУГ цели (target)
        float x = orbitDistance * (float)Math.cos(pitchRad) * (float)Math.cos(yawRad);
        float y = orbitDistance * (float)Math.sin(pitchRad);
        float z = orbitDistance * (float)Math.cos(pitchRad) * (float)Math.sin(yawRad);
        
        // Устанавливаем позицию камеры относительно цели
        position = target.add(new Vector3f(x, y, z));
        
        // Камера всегда смотрит на цель, поэтому target не меняем
    }

    // Обновление направления взгляда для свободной камеры
    private void updateFreeCameraTarget() {
        // Преобразуем углы из градусов в радианы
        float yawRad = (float)Math.toRadians(freeYaw);
        float pitchRad = (float)Math.toRadians(freePitch);
        
        // Вычисляем направление взгляда
        float x = (float)Math.cos(pitchRad) * (float)Math.cos(yawRad);
        float y = (float)Math.sin(pitchRad);
        float z = (float)Math.cos(pitchRad) * (float)Math.sin(yawRad);
        
        Vector3f direction = new Vector3f(x, y, z).normalize();
        
        // Устанавливаем цель на расстоянии от камеры
        target = position.add(direction.multiply(10.0f));
    }

    // Обработка движения мыши для вращения камеры
    public void processMouseMovement(float xoffset, float yoffset) {
        if (mode == CameraMode.FIXED) return;
        
        xoffset *= mouseSensitivity;
        yoffset *= mouseSensitivity;
        
        if (mode == CameraMode.ORBIT) {
            // Вращение камеры ВОКРУГ цели (target)
            orbitYaw += xoffset;
            orbitPitch += yoffset;
            
            // Ограничиваем вертикальный угол, чтобы камера не переворачивалась
            if (orbitPitch > 89.0f) orbitPitch = 89.0f;
            if (orbitPitch < -89.0f) orbitPitch = -89.0f;
            
            // Обновляем позицию камеры
            updateOrbitCameraPosition();
            
        } else if (mode == CameraMode.FREE_FLIGHT) {
            // Вращение взгляда камеры на месте (как в FPS)
            freeYaw += xoffset;
            freePitch += yoffset;
            
            // Ограничиваем вертикальный угол
            if (freePitch > 89.0f) freePitch = 89.0f;
            if (freePitch < -89.0f) freePitch = -89.0f;
            
            // Обновляем направление взгляда
            updateFreeCameraTarget();
        }
    }

    // Обработка перемещения камеры с клавиатуры (только для FREE_FLIGHT)
    public void processKeyboard(CameraMovement direction, float deltaTime) {
        if (mode != Camera.CameraMode.FREE_FLIGHT) return;
        
        float velocity = movementSpeed * deltaTime;
        Vector3f front = target.subtract(position).normalize();
        Vector3f right = up.cross(front).normalize();
        Vector3f upVector = right.cross(front).normalize();
        
        switch (direction) {
            case FORWARD:
                position = position.add(front.multiply(velocity));
                break;
            case BACKWARD:
                position = position.subtract(front.multiply(velocity));
                break;
            case LEFT:
                position = position.subtract(right.multiply(velocity));
                break;
            case RIGHT:
                position = position.add(right.multiply(velocity));
                break;
            case UP:
                position = position.add(upVector.multiply(velocity));
                break;
            case DOWN:
                position = position.subtract(upVector.multiply(velocity));
                break;
        }
        
        // Обновляем цель после перемещения
        updateFreeCameraTarget();
    }

    // Обработка зума (колесико мыши)
    public void processMouseScroll(float deltaY) {
        if (mode == CameraMode.FIXED) return;
        
        if (mode == CameraMode.ORBIT) {
            // В режиме орбиты изменяем расстояние до цели
            float zoomChange = deltaY * zoomSensitivity;
            orbitDistance -= zoomChange;
            
            // Ограничиваем расстояние
            if (orbitDistance < 1.0f) orbitDistance = 1.0f;
            if (orbitDistance > 500.0f) orbitDistance = 500.0f;
            
            // Обновляем позицию камеры
            updateOrbitCameraPosition();
            
        } else if (mode == CameraMode.FREE_FLIGHT) {
            // В режиме свободного полета изменяем FOV
            float fovChange = deltaY * zoomSensitivity;
            fov -= fovChange;
            
            // Ограничиваем FOV
            if (fov < 1.0f) fov = 1.0f;
            if (fov > 120.0f) fov = 120.0f;
        }
    }

    // Методы из старой реализации для обратной совместимости
    public void setPosition(final Vector3f position) {
        this.position = position;
        if (mode == CameraMode.ORBIT) {
            updateOrbitParametersFromPosition();
        } else if (mode == CameraMode.FREE_FLIGHT) {
            updateFreeCameraAngles();
        }
    }

    public void setTarget(final Vector3f target) {
        this.target = target;
        if (mode == CameraMode.ORBIT) {
            updateOrbitParametersFromPosition();
        } else if (mode == CameraMode.FREE_FLIGHT) {
            updateFreeCameraAngles();
        }
    }

    public void setAspectRatio(final float aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getTarget() {
        return target;
    }
    
    // Методы movePosition и moveTarget из старой реализации
    public void movePosition(final Vector3f translation) {
        this.position = this.position.add(translation);
        this.target = this.target.add(translation);
    }

    public void moveTarget(final Vector3f translation) {
        this.target = this.target.add(translation);
        if (mode == CameraMode.ORBIT) {
            updateOrbitParametersFromPosition();
        }
    }

    // Методы для обновления FOV
    public float getFov() {
        return fov;
    }

    public void setFov(float fov) {
        this.fov = fov;
    }
    
    // Стандартные методы для получения матриц
    public Matrix4f getViewMatrix() {
        return GraphicConveyor.lookAt(position, target, up);
    }

    public Matrix4f getProjectionMatrix() {
        return GraphicConveyor.perspective(fov, aspectRatio, nearPlane, farPlane);
    }
    
    // Геттеры и сеттеры для параметров управления
    public float getMovementSpeed() {
        return movementSpeed;
    }
    
    public void setMovementSpeed(float speed) {
        this.movementSpeed = speed;
    }
    
    public float getMouseSensitivity() {
        return mouseSensitivity;
    }
    
    public void setMouseSensitivity(float sensitivity) {
        this.mouseSensitivity = sensitivity;
    }
    
    public float getZoomSensitivity() {
        return zoomSensitivity;
    }
    
    public void setZoomSensitivity(float zoomSensitivity) {
        this.zoomSensitivity = zoomSensitivity;
    }
    
    // Перечисление для направлений движения
    public enum CameraMovement {
        FORWARD,
        BACKWARD,
        LEFT,
        RIGHT,
        UP,
        DOWN
    }
}