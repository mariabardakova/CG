package ru.kirill.task4_4_1.input;


import ru.kirill.task4_4_1.ui.ModelTransformController;
import ru.kirill.task4_4_1.graphics.Camera;
import ru.kirill.task4_4_1.utils.Vector3f;

import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;


/*
 * Менеджер ввода, координирующий работу контроллеров камеры и модели.
 */
public class InputManager implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {
    
    private CameraController cameraController;
    private ModelTransformController modelController;
    
    // Флаги состояния
    private boolean cameraControlEnabled = true;
    private boolean modelControlEnabled = false;
    
    // Горячие клавиши
    private Map<Integer, Runnable> hotkeyActions;
    
    // Компонент для захвата фокуса
    private Component focusComponent;
    
    // Состояние для переключения между режимами
    private ControlFocus currentFocus = ControlFocus.CAMERA;
    
    public enum ControlFocus {
        CAMERA,
        MODEL,
        UI
    }
    
    public InputManager(Camera camera) {
        this.cameraController = new CameraController(camera);
        this.hotkeyActions = new HashMap<>();
        setupHotkeys();
    }
    
    public InputManager(Camera camera, ModelTransformController modelController) {
        this(camera);
        this.modelController = modelController;
    }
    
    private void setupHotkeys() {
        // Горячие клавиши для управления камерой
        hotkeyActions.put(KeyEvent.VK_C, () -> {
            cameraControlEnabled = !cameraControlEnabled;
            System.out.println("Управление камерой: " + (cameraControlEnabled ? "ВКЛ" : "ВЫКЛ"));
        });
        
        hotkeyActions.put(KeyEvent.VK_M, () -> {
            modelControlEnabled = !modelControlEnabled;
            System.out.println("Управление моделью: " + (modelControlEnabled ? "ВКЛ" : "ВЫКЛ"));
        });
        
        hotkeyActions.put(KeyEvent.VK_TAB, () -> {
            switchControlFocus();
        });
        
        hotkeyActions.put(KeyEvent.VK_ESCAPE, () -> {
            resetAllControls();
        });
        
        hotkeyActions.put(KeyEvent.VK_F1, () -> {
            showHelp();
        });
        
        hotkeyActions.put(KeyEvent.VK_F2, () -> {
            saveViewpoint();
        });
        
        hotkeyActions.put(KeyEvent.VK_F3, () -> {
            loadViewpoint();
        });
        
        // Горячие клавиши для управления моделью (если есть контроллер)
        if (modelController != null) {
            hotkeyActions.put(KeyEvent.VK_R, () -> {
                modelController.resetTransformation();
                System.out.println("Трансформации модели сброшены");
            });
            
            hotkeyActions.put(KeyEvent.VK_ENTER, () -> {
                modelController.applyTransformation();
                System.out.println("Трансформации модели применены");
            });
        }
    }
    
    // Переключение фокуса управления
    private void switchControlFocus() {
        switch (currentFocus) {
            case CAMERA:
                currentFocus = ControlFocus.MODEL;
                System.out.println("Фокус управления: МОДЕЛЬ");
                break;
            case MODEL:
                currentFocus = ControlFocus.CAMERA;
                System.out.println("Фокус управления: КАМЕРА");
                break;
            case UI:
                currentFocus = ControlFocus.CAMERA;
                System.out.println("Фокус управления: КАМЕРА");
                break;
        }
        
        updateControlStates();
    }
    
    private void updateControlStates() {
        switch (currentFocus) {
            case CAMERA:
                cameraControlEnabled = true;
                modelControlEnabled = false;
                break;
            case MODEL:
                cameraControlEnabled = false;
                modelControlEnabled = true;
                break;
            case UI:
                cameraControlEnabled = false;
                modelControlEnabled = false;
                break;
        }
    }
    
    // Сброс всех контролов
    private void resetAllControls() {
        cameraController.resetCamera();
        if (modelController != null) {
            modelController.resetTransformation();
        }
        System.out.println("Все контролы сброшены");
    }
    
    // Сохранение/загрузка точки обзора
    private static class Viewpoint {
        Vector3f position;
        Vector3f target;
        Vector3f up;
        
        Viewpoint(Vector3f position, Vector3f target, Vector3f up) {
            this.position = position;
            this.target = target;
            this.up = up;
        }
    }
    
    private Viewpoint savedViewpoint;
    
    private void saveViewpoint() {
        savedViewpoint = new Viewpoint(
            cameraController.getCamera().getPosition(),
            cameraController.getCamera().getTarget(),
            cameraController.getCamera().getUp()
        );
        System.out.println("Точка обзора сохранена");
    }
    
    private void loadViewpoint() {
        if (savedViewpoint != null) {
            cameraController.getCamera().setPosition(savedViewpoint.position);
            cameraController.getCamera().setTarget(savedViewpoint.target);
            cameraController.getCamera().setUp(savedViewpoint.up);
            System.out.println("Точка обзора загружена");
        } else {
            System.out.println("Нет сохраненной точки обзора");
        }
    }
    
    // Показать справку
    private void showHelp() {
        String helpMessage = 
            "=== Горячие клавиши ===\n" +
            "Управление камерой:\n" +
            "  WASD/QE - Перемещение камеры\n" +
            "  ЛКМ + перемещение - Вращение камеры\n" +
            "  ПКМ + перемещение - Панорамирование\n" +
            "  Колесо мыши - Приближение/отдаление\n" +
            "  R - Сброс камеры\n" +
            "  P - Переключение проекции (перспективная/ортографическая)\n" +
            "  O/F/1 - Режимы камеры (Orbit/Fly/FPS)\n" +
            "  +/- - Быстрое приближение/отдаление\n" +
            "  Стрелки - Вращение камеры\n\n" +
            "Управление моделью:\n" +
            "  R - Сброс трансформаций модели\n" +
            "  Enter - Применить трансформации модели\n\n" +
            "Общие:\n" +
            "  C - Вкл/выкл управление камерой\n" +
            "  M - Вкл/выкл управление моделью\n" +
            "  Tab - Переключение фокуса управления\n" +
            "  Esc - Сброс всех контролов\n" +
            "  F1 - Показать справку\n" +
            "  F2 - Сохранить точку обзора\n" +
            "  F3 - Загрузить точку обзора\n\n" +
            "Текущий фокус: " + currentFocus;
        
        JOptionPane.showMessageDialog(focusComponent, helpMessage, "Справка", JOptionPane.INFORMATION_MESSAGE);
    }
    
    // Методы для привязки к компоненту
    public void attachToComponent(Component component) {
        this.focusComponent = component;
        
        component.addMouseListener(this);
        component.addMouseMotionListener(this);
        component.addMouseWheelListener(this);
        component.addKeyListener(this);
        
        // Запрашиваем фокус для компонента
        component.setFocusable(true);
        component.requestFocusInWindow();
    }
    
    public void detachFromComponent(Component component) {
        component.removeMouseListener(this);
        component.removeMouseMotionListener(this);
        component.removeMouseWheelListener(this);
        component.removeKeyListener(this);
    }
    
    // Методы обработки событий (делегирование контроллерам)
    
    @Override
    public void mousePressed(MouseEvent e) {
        if (cameraControlEnabled) {
            cameraController.mousePressed(e);
        }
        
        // Запрашиваем фокус при клике
        if (focusComponent != null) {
            focusComponent.requestFocusInWindow();
        }
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {
        if (cameraControlEnabled) {
            cameraController.mouseReleased(e);
        }
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
        if (cameraControlEnabled) {
            cameraController.mouseDragged(e);
        }
    }
    
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (cameraControlEnabled) {
            cameraController.mouseWheelMoved(e);
        }
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        // Проверяем горячие клавиши
        Runnable action = hotkeyActions.get(e.getKeyCode());
        if (action != null) {
            action.run();
            e.consume();
        }
        
        // Передаем событие контроллеру камеры
        if (cameraControlEnabled) {
            cameraController.keyPressed(e);
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        if (cameraControlEnabled) {
            cameraController.keyReleased(e);
        }
    }
    
    // Обновление состояния (вызывается в игровом цикле)
    public void update() {
        if (cameraControlEnabled) {
            cameraController.update();
        }
    }
    
    // Геттеры и сеттеры
    
    public CameraController getCameraController() { return cameraController; }
    
    public ModelTransformController getModelController() { return modelController; }
    public void setModelController(ModelTransformController modelController) { 
        this.modelController = modelController; 
        setupHotkeys(); // Перестраиваем горячие клавиши
    }
    
    public boolean isCameraControlEnabled() { return cameraControlEnabled; }
    public void setCameraControlEnabled(boolean enabled) { 
        this.cameraControlEnabled = enabled; 
        if (enabled) currentFocus = ControlFocus.CAMERA;
    }
    
    public boolean isModelControlEnabled() { return modelControlEnabled; }
    public void setModelControlEnabled(boolean enabled) { 
        this.modelControlEnabled = enabled; 
        if (enabled) currentFocus = ControlFocus.MODEL;
    }
    
    public ControlFocus getCurrentFocus() { return currentFocus; }
    public void setCurrentFocus(ControlFocus focus) { 
        this.currentFocus = focus; 
        updateControlStates();
    }
    
    public void addHotkey(int keyCode, Runnable action) {
        hotkeyActions.put(keyCode, action);
    }
    
    public void removeHotkey(int keyCode) {
        hotkeyActions.remove(keyCode);
    }
    
    // Неиспользуемые методы интерфейсов
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseMoved(MouseEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}
