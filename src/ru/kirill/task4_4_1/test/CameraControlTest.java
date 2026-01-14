package ru.kirill.task4_4_1.test;


import ru.kirill.task4_4_1.graphics.Camera;
import ru.kirill.task4_4_1.input.CameraController;
import ru.kirill.task4_4_1.input.InputManager;
import ru.kirill.task4_4_1.ui.ModelTransformController;
import ru.kirill.task4_4_1.utils.Model;
import ru.kirill.task4_4_1.utils.Vector3f;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Тестовое приложение для проверки управления камерой.
 */
public class CameraControlTest extends JFrame {
    
    private Camera camera;
    private CameraController cameraController;
    private InputManager inputManager;
    
    private JTextArea logArea;
    
    public CameraControlTest() {
        super("Тест управления камерой");
        
        initializeComponents();
        setupUI();
        setupInput();
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("Приложение закрывается");
            }
        });
    }
    
    private void initializeComponents() {
        // Создаем камеру
        camera = new Camera();
        
        // Создаем контроллер камеры
        cameraController = new CameraController(camera);
        
        // Создаем менеджер ввода
        inputManager = new InputManager(camera);
        
        // Создаем область для логов
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
    }
    
    private void setupUI() {
        setLayout(new BorderLayout());
        
        // Панель управления
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Кнопки тестирования
        JButton testOrbitButton = new JButton("Тест режима Orbit");
        testOrbitButton.addActionListener(e -> testOrbitMode());
        
        JButton testFlyButton = new JButton("Тест режима Fly");
        testFlyButton.addActionListener(e -> testFlyMode());
        
        JButton testResetButton = new JButton("Тест сброса камеры");
        testResetButton.addActionListener(e -> testResetCamera());
        
        JButton testProjectionButton = new JButton("Тест переключения проекции");
        testProjectionButton.addActionListener(e -> testToggleProjection());
        
        JButton testMovementButton = new JButton("Тест движения WASD");
        testMovementButton.addActionListener(e -> testWASDMovement());
        
        JButton testHotkeysButton = new JButton("Тест горячих клавиш");
        testHotkeysButton.addActionListener(e -> testHotkeys());
        
        controlPanel.add(testOrbitButton);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(testFlyButton);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(testResetButton);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(testProjectionButton);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(testMovementButton);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(testHotkeysButton);
        
        // Панель информации
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("Информация о камере"));
        
        JTextArea infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        // Таймер для обновления информации
        Timer updateTimer = new Timer(100, e -> updateCameraInfo(infoArea));
        updateTimer.start();
        
        infoPanel.add(new JScrollPane(infoArea), BorderLayout.CENTER);
        
        // Панель логов
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createTitledBorder("Логи"));
        logPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        
        // Добавляем все на форму
        add(controlPanel, BorderLayout.WEST);
        add(infoPanel, BorderLayout.CENTER);
        add(logPanel, BorderLayout.SOUTH);
        
        // Устанавливаем размеры
        logPanel.setPreferredSize(new Dimension(0, 150));
    }
    
    private void setupInput() {
        // Привязываем менеджер ввода к основному окну
        inputManager.attachToComponent(this);
        
        // Добавляем слушатель для логирования
        inputManager.addHotkey(KeyEvent.VK_H, () -> {
            log("Нажата горячая клавиша H (тестовая)");
        });
    }
    
    private void testOrbitMode() {
        cameraController.setControlMode(CameraController.ControlMode.ORBIT);
        log("Установлен режим Orbit");
        log("Камера: " + camera);
    }
    
    private void testFlyMode() {
        cameraController.setControlMode(CameraController.ControlMode.FLY);
        log("Установлен режим Fly");
        log("Камера: " + camera);
    }
    
    private void testResetCamera() {
        cameraController.resetCamera();
        log("Камера сброшена");
        log("Камера: " + camera);
    }
    
    private void testToggleProjection() {
        cameraController.toggleProjection();
        log("Проекция переключена: " + camera.getCameraType());
    }
    
    private void testWASDMovement() {
        log("Тест движения WASD:");
        log("  Исходная позиция: " + camera.getPosition());
        
        // Симулируем нажатия WASD
        cameraController.moveForward();
        log("  После W (вперед): " + camera.getPosition());
        
        cameraController.moveLeft();
        log("  После A (влево): " + camera.getPosition());
        
        cameraController.moveBackward();
        log("  После S (назад): " + camera.getPosition());
        
        cameraController.moveRight();
        log("  После D (вправо): " + camera.getPosition());
    }
    
    private void testHotkeys() {
        log("Тест горячих клавиш:");
        log("  F1 - Справка");
        log("  R - Сброс камеры");
        log("  P - Переключение проекции");
        log("  O - Режим Orbit");
        log("  F - Режим Fly");
        log("  1 - Режим FPS");
        log("  C - Вкл/выкл управление камерой");
        log("  Tab - Переключение фокуса");
        log("  Esc - Сброс всех контролов");
    }
    
    private void updateCameraInfo(JTextArea infoArea) {
        String info = String.format(
            "Позиция камеры:\n" +
            "  X: %.2f\n" +
            "  Y: %.2f\n" +
            "  Z: %.2f\n\n" +
            "Цель камеры:\n" +
            "  X: %.2f\n" +
            "  Y: %.2f\n" +
            "  Z: %.2f\n\n" +
            "Параметры:\n" +
            "  Режим: %s\n" +
            "  Проекция: %s\n" +
            "  FOV: %.1f\n" +
            "  Ближняя плоскость: %.2f\n" +
            "  Дальняя плоскость: %.2f",
            camera.getPosition().getX(),
            camera.getPosition().getY(),
            camera.getPosition().getZ(),
            camera.getTarget().getX(),
            camera.getTarget().getY(),
            camera.getTarget().getZ(),
            cameraController.getControlMode(),
            camera.getCameraType(),
            camera.getFov(),
            camera.getNearPlane(),
            camera.getFarPlane()
        );
        
        infoArea.setText(info);
    }
    
    private void log(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
        System.out.println(message);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CameraControlTest testApp = new CameraControlTest();
            testApp.setVisible(true);
            
            testApp.log("Тестовое приложение запущено");
            testApp.log("Используйте мышь и клавиатуру для управления камерой");
            testApp.log("Нажмите F1 для справки");
        });
    }
}
