package com.cgvsu.ui;


import com.cgvsu.graphics.Scene;
import com.cgvsu.input.GameLoop;
import com.cgvsu.input.InputManager;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


/*
 * Главное окно приложения с поддержкой управления камерой и моделями.
 */
public class MainApplicationWindow extends JFrame {
    
    // Компоненты
    private Scene scene;
    private InputManager inputManager;
    private GameLoop gameLoop;
    
    // Панели управления
    private TransformControlPanel transformPanel;
    private CameraControlPanel cameraPanel;
    
    // Панель отрисовки
    private RenderPanel renderPanel;
    
    // Текущая модель
    private Model currentModel;
    private ModelTransformController modelController;
    
    public MainApplicationWindow() {
        super("3D Model Viewer - Управление камерой");
        
        initializeComponents();
        setupUI();
        setupInput();
        startGameLoop();
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopGameLoop();
            }
        });
    }
    
    private void initializeComponents() {
        // Создаем сцену с камерой
        scene = new Scene(800, 600);
        
        // Создаем контроллер модели
        modelController = new ModelTransformController();
        
        // Создаем менеджер ввода
        inputManager = new InputManager(scene.getCamera(), modelController);
        
        // Создаем игровой цикл
        gameLoop = new GameLoop(inputManager, this::repaintScene, this::updateScene);
        
        // Создаем панели управления
        transformPanel = new TransformControlPanel(modelController);
        cameraPanel = new CameraControlPanel(scene.getCamera(), inputManager.getCameraController());
        
        // Создаем панель отрисовки
        renderPanel = new RenderPanel(scene);
    }
    
    private void setupUI() {
        setLayout(new BorderLayout());
        
        // Левая панель - управление
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        controlPanel.setPreferredSize(new Dimension(350, 0));
        
        // Панель загрузки модели
        JPanel loadPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton loadButton = new JButton("Загрузить модель");
        loadButton.addActionListener(this::loadModel);
        
        JButton saveButton = new JButton("Сохранить модель");
        saveButton.addActionListener(this::saveModel);
        
        loadPanel.add(loadButton);
        loadPanel.add(saveButton);
        
        // Добавляем все панели
        controlPanel.add(loadPanel);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(transformPanel);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(cameraPanel);
        
        // Основная панель - отрисовка
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(renderPanel, BorderLayout.CENTER);
        
        // Панель статуса
        JPanel statusPanel = new JPanel(new BorderLayout());
        JLabel statusLabel = new JLabel(" Готов к работе | F1 - Справка");
        statusPanel.add(statusLabel, BorderLayout.WEST);
        
        // Добавляем все на форму
        add(controlPanel, BorderLayout.WEST);
        add(mainPanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
        
        // Меню
        setupMenuBar();
    }
    
    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // Меню Файл
        JMenu fileMenu = new JMenu("Файл");
        JMenuItem loadItem = new JMenuItem("Загрузить модель");
        loadItem.addActionListener(this::loadModel);
        
        JMenuItem saveItem = new JMenuItem("Сохранить модель");
        saveItem.addActionListener(this::saveModel);
        
        JMenuItem exitItem = new JMenuItem("Выход");
        exitItem.addActionListener(e -> System.exit(0));
        
        fileMenu.add(loadItem);
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // Меню Вид
        JMenu viewMenu = new JMenu("Вид");
        JCheckBoxMenuItem wireframeItem = new JCheckBoxMenuItem("Каркасный режим");
        JCheckBoxMenuItem lightingItem = new JCheckBoxMenuItem("Освещение", true);
        
        viewMenu.add(wireframeItem);
        viewMenu.add(lightingItem);
        
        // Меню Камера
        JMenu cameraMenu = new JMenu("Камера");
        JMenuItem resetCameraItem = new JMenuItem("Сбросить камеру");
        resetCameraItem.addActionListener(e -> inputManager.getCameraController().resetCamera());
        
        JMenuItem toggleProjectionItem = new JMenuItem("Переключить проекцию");
        toggleProjectionItem.addActionListener(e -> inputManager.getCameraController().toggleProjection());
        
        cameraMenu.add(resetCameraItem);
        cameraMenu.add(toggleProjectionItem);
        
        // Меню Справка
        JMenu helpMenu = new JMenu("Справка");
        JMenuItem helpItem = new JMenuItem("Показать справку");
        helpItem.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, 
                "Управление:\n" +
                "• ЛКМ + перемещение - Вращение камеры\n" +
                "• ПКМ + перемещение - Панорамирование\n" +
                "• Колесо мыши - Приближение\n" +
                "• WASD - Перемещение\n" +
                "• Q/E - Вверх/Вниз\n" +
                "• R - Сброс камеры\n" +
                "• F1 - Полная справка\n\n" +
                "Текущий режим: " + inputManager.getCameraController().getControlMode(),
                "Краткая справка", 
                JOptionPane.INFORMATION_MESSAGE);
        });
        
        helpMenu.add(helpItem);
        
        // Добавляем меню
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(cameraMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    private void setupInput() {
        // Привязываем менеджер ввода к панели отрисовки
        inputManager.attachToComponent(renderPanel);
        
        // Фокусируем панель отрисовки для захвата ввода
        renderPanel.setFocusable(true);
        renderPanel.requestFocusInWindow();
    }
    
    private void startGameLoop() {
        gameLoop.start();
    }
    
    private void stopGameLoop() {
        if (gameLoop != null && gameLoop.isRunning()) {
            gameLoop.stop();
        }
    }
    
    private void repaintScene() {
        renderPanel.repaint();
    }
    
    private void updateScene() {
        // Обновление сцены (например, анимации)
        // В данном случае ничего не делаем, так как все обновляется через ввод
    }
    
    private void loadModel(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Выберите модель OBJ");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(java.io.File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".obj");
            }
            
            @Override
            public String getDescription() {
                return "OBJ Files (*.obj)";
            }
        });
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                // Здесь должна быть логика загрузки модели
                // Для примера создадим простую модель
                currentModel = createSampleModel();
                currentModel.setName(fileChooser.getSelectedFile().getName());
                
                // Добавляем модель в сцену
                scene.addObject(currentModel.getName(), currentModel);
                
                // Устанавливаем модель в контроллер
                modelController.setModel(currentModel);
                
                // Обновляем UI
                transformPanel.setTransformController(modelController);
                
                JOptionPane.showMessageDialog(this, 
                    "Модель загружена: " + currentModel.getName(), 
                    "Загрузка", 
                    JOptionPane.INFORMATION_MESSAGE);
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Ошибка при загрузке модели: " + ex.getMessage(), 
                    "Ошибка", 
                    JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
    
    private void saveModel(ActionEvent e) {
        if (currentModel == null) {
            JOptionPane.showMessageDialog(this, 
                "Нет загруженной модели для сохранения", 
                "Ошибка", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        ModelSaveDialog.showSaveDialog(this, currentModel, modelController);
    }
    
    // Создание тестовой модели (для демонстрации)
    private Model createSampleModel() {
        Model model = new Model("Sample Cube");
        
        // Вершины куба
        model.getVertices().add(new Vector3f(-1, -1, -1));
        model.getVertices().add(new Vector3f(1, -1, -1));
        model.getVertices().add(new Vector3f(1, 1, -1));
        model.getVertices().add(new Vector3f(-1, 1, -1));
        model.getVertices().add(new Vector3f(-1, -1, 1));
        model.getVertices().add(new Vector3f(1, -1, 1));
        model.getVertices().add(new Vector3f(1, 1, 1));
        model.getVertices().add(new Vector3f(-1, 1, 1));
        
        // Создание полигонов (граней куба)
        // Здесь должна быть логика создания полигонов
        // Для простоты оставляем пустым
        
        return model;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Устанавливаем Look and Feel системы
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            MainApplicationWindow window = new MainApplicationWindow();
            window.setVisible(true);
        });
    }
}
