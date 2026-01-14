package ru.kirill.task4_4_1.input;


import ru.kirill.task4_4_1.graphics.Camera;
import ru.kirill.task4_4_1.utils.Vector3f;

import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;

import javax.swing.SwingUtilities;


/*
 * Контроллер для управления камерой с помощью мыши и клавиатуры.
 * Поддерживает режимы вращения, панорамирования и масштабирования.
 */
public class CameraController implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {
    
    private Camera camera;
    
    // Состояние мыши
    private int lastMouseX;
    private int lastMouseY;
    private boolean isDragging = false;
    private boolean isRotating = false;
    private boolean isPanning = false;
    
    // Состояние клавиш
    private Set<Integer> pressedKeys;
    
    // Скорости управления
    private float rotationSpeed = 0.5f;
    private float panSpeed = 0.005f;
    private float zoomSpeed = 0.5f;
    private float moveSpeed = 0.1f;
    
    // Режимы управления
    private ControlMode controlMode = ControlMode.FLY;
    
    // Флаги для особых действий
    private boolean invertYAxis = false;
    private boolean smoothMovement = true;
    
    public enum ControlMode {
        ORBIT,      // Вращение вокруг цели
        FLY,        // Свободное перемещение
        FPS         // От первого лица
    }
    
    public CameraController(Camera camera) {
        this.camera = camera;
        this.pressedKeys = new HashSet<>();
    }
    
    // Методы обработки мыши
    
    @Override
    public void mousePressed(MouseEvent e) {
        lastMouseX = e.getX();
        lastMouseY = e.getY();
        isDragging = true;
        
        // Определяем тип операции по кнопке мыши
        if (SwingUtilities.isLeftMouseButton(e)) {
            if (controlMode == ControlMode.ORBIT) {
                isRotating = true;
            } else {
                isRotating = true;
            }
        } else if (SwingUtilities.isMiddleMouseButton(e) || SwingUtilities.isRightMouseButton(e)) {
            isPanning = true;
        }
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {
        isDragging = false;
        isRotating = false;
        isPanning = false;
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
        if (!isDragging) return;
        
        int dx = e.getX() - lastMouseX;
        int dy = e.getY() - lastMouseY;
        
        // Обработка вращения
        if (isRotating) {
            handleRotation(dx, dy);
        }
        
        // Обработка панорамирования
        if (isPanning) {
            handlePanning(dx, dy);
        }
        
        lastMouseX = e.getX();
        lastMouseY = e.getY();
    }
    
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int notches = e.getWheelRotation();
        handleZoom(notches);
    }
    
    // Методы обработки клавиатуры
    
    @Override
    public void keyPressed(KeyEvent e) {
        pressedKeys.add(e.getKeyCode());
        handleKeyPress(e.getKeyCode());
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        pressedKeys.remove(e.getKeyCode());
    }
    
    // Обработка вращения камеры
    private void handleRotation(int dx, int dy) {
        float yaw = dx * rotationSpeed;
        float pitch = dy * rotationSpeed * (invertYAxis ? -1 : 1);
        
        switch (controlMode) {
            case ORBIT:
                camera.rotateAroundTarget(-yaw, -pitch);
                break;
            case FLY:
            case FPS:
                // Для свободной камеры вращаем камеру и цель вместе
                Vector3f forward = camera.getTarget().subtract(camera.getPosition()).normalize();
                Vector3f right = camera.getUp().cross(forward).normalize();
                
                // Вращение по рысканью (yaw)
                float yawRad = (float) Math.toRadians(yaw);
                Vector3f yawAxis = camera.getUp();
                forward = rotateVector(forward, yawAxis, yawRad);
                
                // Вращение по тангажу (pitch)
                float pitchRad = (float) Math.toRadians(pitch);
                Vector3f pitchAxis = right;
                forward = rotateVector(forward, pitchAxis, pitchRad);
                
                // Обновляем цель камеры
                Vector3f newTarget = camera.getPosition().add(forward);
                camera.setTarget(newTarget);
                break;
        }
    }
    
    // Обработка панорамирования (сдвига камеры)
    private void handlePanning(int dx, int dy) {
        Vector3f position = camera.getPosition();
        Vector3f target = camera.getTarget();
        Vector3f forward = target.subtract(position).normalize();
        Vector3f right = camera.getUp().cross(forward).normalize();
        Vector3f up = forward.cross(right).normalize();
        
        // Вычисляем смещение
        Vector3f delta = right.multiply(-dx * panSpeed)
                          .add(up.multiply(dy * panSpeed));
        
        // Применяем смещение к позиции и цели
        camera.setPosition(position.add(delta));
        camera.setTarget(target.add(delta));
    }
    
    // Обработка масштабирования/приближения
    private void handleZoom(int notches) {
        float amount = notches * zoomSpeed;
        camera.zoom(amount);
    }
    
    // Обработка нажатий клавиш
    private void handleKeyPress(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_W:
                moveForward();
                break;
            case KeyEvent.VK_S:
                moveBackward();
                break;
            case KeyEvent.VK_A:
                moveLeft();
                break;
            case KeyEvent.VK_D:
                moveRight();
                break;
            case KeyEvent.VK_Q:
                moveDown();
                break;
            case KeyEvent.VK_E:
                moveUp();
                break;
            case KeyEvent.VK_R:
                resetCamera();
                break;
            case KeyEvent.VK_P:
                toggleProjection();
                break;
            case KeyEvent.VK_O:
                setOrbitMode();
                break;
            case KeyEvent.VK_F:
                setFlyMode();
                break;
            case KeyEvent.VK_1:
                setFPSCamera();
                break;
            case KeyEvent.VK_ADD:
            case KeyEvent.VK_PLUS:
            case KeyEvent.VK_EQUALS:
                camera.zoom(-zoomSpeed * 2);
                break;
            case KeyEvent.VK_SUBTRACT:
            case KeyEvent.VK_MINUS:
                camera.zoom(zoomSpeed * 2);
                break;
            case KeyEvent.VK_UP:
                camera.rotateAroundTarget(0, -rotationSpeed * 5);
                break;
            case KeyEvent.VK_DOWN:
                camera.rotateAroundTarget(0, rotationSpeed * 5);
                break;
            case KeyEvent.VK_LEFT:
                camera.rotateAroundTarget(rotationSpeed * 5, 0);
                break;
            case KeyEvent.VK_RIGHT:
                camera.rotateAroundTarget(-rotationSpeed * 5, 0);
                break;
        }
    }
    
    // Методы движения камеры
    public void moveForward() {
        Vector3f position = camera.getPosition();
        Vector3f target = camera.getTarget();
        Vector3f forward = target.subtract(position).normalize();
        
        Vector3f newPosition = position.add(forward.multiply(moveSpeed));
        Vector3f newTarget = target.add(forward.multiply(moveSpeed));
        
        camera.setPosition(newPosition);
        camera.setTarget(newTarget);
    }
    
    public void moveBackward() {
        Vector3f position = camera.getPosition();
        Vector3f target = camera.getTarget();
        Vector3f forward = target.subtract(position).normalize();
        
        Vector3f newPosition = position.subtract(forward.multiply(moveSpeed));
        Vector3f newTarget = target.subtract(forward.multiply(moveSpeed));
        
        camera.setPosition(newPosition);
        camera.setTarget(newTarget);
    }
    
    public void moveLeft() {
        Vector3f position = camera.getPosition();
        Vector3f target = camera.getTarget();
        Vector3f forward = target.subtract(position).normalize();
        Vector3f right = camera.getUp().cross(forward).normalize();
        
        Vector3f newPosition = position.subtract(right.multiply(moveSpeed));
        Vector3f newTarget = target.subtract(right.multiply(moveSpeed));
        
        camera.setPosition(newPosition);
        camera.setTarget(newTarget);
    }
    
    public void moveRight() {
        Vector3f position = camera.getPosition();
        Vector3f target = camera.getTarget();
        Vector3f forward = target.subtract(position).normalize();
        Vector3f right = camera.getUp().cross(forward).normalize();
        
        Vector3f newPosition = position.add(right.multiply(moveSpeed));
        Vector3f newTarget = target.add(right.multiply(moveSpeed));
        
        camera.setPosition(newPosition);
        camera.setTarget(newTarget);
    }
    
    private void moveUp() {
        Vector3f position = camera.getPosition();
        Vector3f target = camera.getTarget();
        
        Vector3f newPosition = position.add(camera.getUp().multiply(moveSpeed));
        Vector3f newTarget = target.add(camera.getUp().multiply(moveSpeed));
        
        camera.setPosition(newPosition);
        camera.setTarget(newTarget);
    }
    
    private void moveDown() {
        Vector3f position = camera.getPosition();
        Vector3f target = camera.getTarget();
        
        Vector3f newPosition = position.subtract(camera.getUp().multiply(moveSpeed));
        Vector3f newTarget = target.subtract(camera.getUp().multiply(moveSpeed));
        
        camera.setPosition(newPosition);
        camera.setTarget(newTarget);
    }
    
    // Вспомогательные методы
    
    private Vector3f rotateVector(Vector3f vector, Vector3f axis, float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        
        // Формула Родрига для вращения вектора
        Vector3f result = vector.multiply(cos)
                .add(axis.cross(vector).multiply(sin))
                .add(axis.multiply(axis.dot(vector) * (1 - cos)));
        
        return result;
    }
    
    public void resetCamera() {
        camera.setPosition(new Vector3f(0, 0, 5));
        camera.setTarget(new Vector3f(0, 0, 0));
        camera.setUp(new Vector3f(0, 1, 0));
    }
    
    public void toggleProjection() {
        if (camera.getCameraType() == Camera.CameraType.PERSPECTIVE) {
            camera.setCameraType(Camera.CameraType.ORTHOGRAPHIC);
            System.out.println("Переключено на ортографическую проекцию");
        } else {
            camera.setCameraType(Camera.CameraType.PERSPECTIVE);
            System.out.println("Переключено на перспективную проекцию");
        }
    }
    
    public void setOrbitMode() {
        controlMode = ControlMode.ORBIT;
        System.out.println("Режим камеры: Вращение вокруг цели (Orbit)");
    }
    
    public void setFlyMode() {
        controlMode = ControlMode.FLY;
        System.out.println("Режим камеры: Свободное перемещение (Fly)");
    }
    
    public void setFPSCamera() {
        controlMode = ControlMode.FPS;
        System.out.println("Режим камеры: От первого лица (FPS)");
    }
    
    // Обновление состояния (вызывается в игровом цикле)
    public void update() {
        // Обработка непрерывного нажатия клавиш
        for (int keyCode : pressedKeys) {
            handleKeyPress(keyCode);
        }
    }
    
    // Геттеры и сеттеры
    
    public Camera getCamera() { return camera; }

    public float getRotationSpeed() { return rotationSpeed; }
    public void setRotationSpeed(float rotationSpeed) { this.rotationSpeed = rotationSpeed; }
    
    public float getPanSpeed() { return panSpeed; }
    public void setPanSpeed(float panSpeed) { this.panSpeed = panSpeed; }
    
    public float getZoomSpeed() { return zoomSpeed; }
    public void setZoomSpeed(float zoomSpeed) { this.zoomSpeed = zoomSpeed; }
    
    public float getMoveSpeed() { return moveSpeed; }
    public void setMoveSpeed(float moveSpeed) { this.moveSpeed = moveSpeed; }
    
    public ControlMode getControlMode() { return controlMode; }
    public void setControlMode(ControlMode controlMode) { this.controlMode = controlMode; }
    
    public boolean isInvertYAxis() { return invertYAxis; }
    public void setInvertYAxis(boolean invertYAxis) { this.invertYAxis = invertYAxis; }
    
    public boolean isSmoothMovement() { return smoothMovement; }
    public void setSmoothMovement(boolean smoothMovement) { this.smoothMovement = smoothMovement; }
    
    // Неиспользуемые методы интерфейсов (оставляем пустыми)
    
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseMoved(MouseEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}
