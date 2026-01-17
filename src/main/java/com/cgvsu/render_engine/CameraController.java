package com.cgvsu.render_engine;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

/**
 * Контроллер для управления камерой через мышь и клавиатуру
 */
public class CameraController {
    private Camera camera;
    private Scene scene;
    private Canvas canvas;
    
    // Состояние клавиш
    private boolean wPressed = false;
    private boolean aPressed = false;
    private boolean sPressed = false;
    private boolean dPressed = false;
    private boolean qPressed = false; // Вниз
    private boolean ePressed = false; // Вверх
    
    // Состояние мыши для вращения
    private double lastMouseX = 0;
    private double lastMouseY = 0;
    private boolean isMouseDragging = false;
    
    // Тайминг
    private long lastUpdateTime = System.currentTimeMillis();
    
    // Обработчики событий
    private EventHandler<KeyEvent> keyPressedHandler;
    private EventHandler<KeyEvent> keyReleasedHandler;
    private EventHandler<MouseEvent> mousePressedHandler;
    private EventHandler<MouseEvent> mouseReleasedHandler;
    private EventHandler<MouseEvent> mouseDraggedHandler;
    private EventHandler<ScrollEvent> scrollHandler;
    
    public CameraController(Camera camera, Scene scene, Canvas canvas) {
        this.camera = camera;
        this.scene = scene;
        this.canvas = canvas;
        setupEventHandlers();
    }
    
    private void setupEventHandlers() {
        // Обработка нажатия клавиш
        keyPressedHandler = event -> {
            KeyCode code = event.getCode();
            switch (code) {
                case W:
                    wPressed = true;
                    break;
                case A:
                    aPressed = true;
                    break;
                case S:
                    sPressed = true;
                    break;
                case D:
                    dPressed = true;
                    break;
                case Q:
                    qPressed = true;
                    break;
                case E:
                    ePressed = true;
                    break;
            }
        };
        
        // Обработка отпускания клавиш
        keyReleasedHandler = event -> {
            KeyCode code = event.getCode();
            switch (code) {
                case W:
                    wPressed = false;
                    break;
                case A:
                    aPressed = false;
                    break;
                case S:
                    sPressed = false;
                    break;
                case D:
                    dPressed = false;
                    break;
                case Q:
                    qPressed = false;
                    break;
                case E:
                    ePressed = false;
                    break;
            }
        };
        
        // Обработка нажатия кнопки мыши
        mousePressedHandler = event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                isMouseDragging = true;
                lastMouseX = event.getX();
                lastMouseY = event.getY();
            }
        };
        
        // Обработка отпускания кнопки мыши
        mouseReleasedHandler = event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                isMouseDragging = false;
            }
        };
        
        // Обработка перетаскивания мыши (вращение камеры)
        mouseDraggedHandler = event -> {
            if (isMouseDragging && (camera.getMode() == Camera.CameraMode.FREE_FLIGHT || 
                                    camera.getMode() == Camera.CameraMode.ORBIT)) {
                double currentX = event.getX();
                double currentY = event.getY();
                
                // Вычисляем смещение мыши
                float xoffset = (float)(currentX - lastMouseX);
                float yoffset = (float)(lastMouseY - currentY); // обратный порядок для правильного направления
                
                // Обновляем позицию мыши
                lastMouseX = currentX;
                lastMouseY = currentY;
                
                // Обрабатываем движение мыши в камере
                camera.processMouseMovement(xoffset, yoffset);
            }
        };
        
        // Обработка колесика мыши (зум)
        scrollHandler = event -> {
            if (camera.getMode() != Camera.CameraMode.FIXED) {
                double deltaY = event.getDeltaY();
                camera.processMouseScroll((float)deltaY);
            }
        };
        
        // Привязка обработчиков к сцене
        scene.addEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
        scene.addEventHandler(KeyEvent.KEY_RELEASED, keyReleasedHandler);
        scene.addEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        scene.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        scene.addEventHandler(MouseEvent.MOUSE_DRAGGED, mouseDraggedHandler);
        scene.addEventHandler(ScrollEvent.SCROLL, scrollHandler);
    }
    
    /**
     * Обновление состояния камеры на основе текущих нажатых клавиш
     */
    public void update() {
        if (camera.getMode() != Camera.CameraMode.FREE_FLIGHT) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastUpdateTime) / 1000.0f;
        lastUpdateTime = currentTime;
        
        // Обработка движения WASD
        if (wPressed) {
            camera.processKeyboard(Camera.CameraMovement.FORWARD, deltaTime);
        }
        if (sPressed) {
            camera.processKeyboard(Camera.CameraMovement.BACKWARD, deltaTime);
        }
        if (aPressed) {
            camera.processKeyboard(Camera.CameraMovement.LEFT, deltaTime);
        }
        if (dPressed) {
            camera.processKeyboard(Camera.CameraMovement.RIGHT, deltaTime);
        }
        if (qPressed) {
            camera.processKeyboard(Camera.CameraMovement.DOWN, deltaTime);
        }
        if (ePressed) {
            camera.processKeyboard(Camera.CameraMovement.UP, deltaTime);
        }
    }
    
    public void cleanup() {
        if (scene != null) {
            scene.removeEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
            scene.removeEventHandler(KeyEvent.KEY_RELEASED, keyReleasedHandler);
            scene.removeEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
            scene.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
            scene.removeEventHandler(MouseEvent.MOUSE_DRAGGED, mouseDraggedHandler);
            scene.removeEventHandler(ScrollEvent.SCROLL, scrollHandler);
        }
    }
    
    public void setSceneAndCanvas(Scene scene, Canvas canvas) {
        if (this.scene != null) {
            cleanup();
        }
        this.scene = scene;
        this.canvas = canvas;
        setupEventHandlers();
    }
}