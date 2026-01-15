package com.cgvsu.ui;

import com.cgvsu.graphics.Camera;
import com.cgvsu.input.CameraController;
import com.cgvsu.math.Vector3f;

import javax.swing.*;
import java.awt.*;


/*
 * Панель управления настройками камеры.
 */
public class CameraControlPanel extends JPanel {
    
    private Camera camera;
    private CameraController cameraController;
    
    private JComboBox<String> modeComboBox;
    private JCheckBox invertYCheckBox;
    private JSlider rotationSpeedSlider;
    private JSlider moveSpeedSlider;
    private JSlider zoomSpeedSlider;
    
    public CameraControlPanel(Camera camera, CameraController controller) {
        this.camera = camera;
        this.cameraController = controller;
        
        initUI();
        updateUIFromController();
    }
    
    private void initUI() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder("Управление камерой"));
        
        // Выбор режима управления
        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        modePanel.add(new JLabel("Режим:"));
        
        modeComboBox = new JComboBox<>(new String[]{"Вращение вокруг цели", "Свободное перемещение", "От первого лица"});
        modeComboBox.addActionListener(e -> {
            int index = modeComboBox.getSelectedIndex();
            switch (index) {
                case 0: cameraController.setControlMode(CameraController.ControlMode.ORBIT); break;
                case 1: cameraController.setControlMode(CameraController.ControlMode.FLY); break;
                case 2: cameraController.setControlMode(CameraController.ControlMode.FPS); break;
            }
        });
        modePanel.add(modeComboBox);
        
        // Настройки управления
        JPanel settingsPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        
        invertYCheckBox = new JCheckBox("Инвертировать ось Y");
        invertYCheckBox.addActionListener(e -> 
            cameraController.setInvertYAxis(invertYCheckBox.isSelected()));
        
        settingsPanel.add(new JLabel("Скорость вращения:"));
        rotationSpeedSlider = createSlider(1, 100, (int)(cameraController.getRotationSpeed() * 10));
        rotationSpeedSlider.addChangeListener(e -> 
            cameraController.setRotationSpeed(rotationSpeedSlider.getValue() / 10.0f));
        
        settingsPanel.add(new JLabel("Скорость перемещения:"));
        moveSpeedSlider = createSlider(1, 100, (int)(cameraController.getMoveSpeed() * 10));
        moveSpeedSlider.addChangeListener(e -> 
            cameraController.setMoveSpeed(moveSpeedSlider.getValue() / 10.0f));
        
        settingsPanel.add(new JLabel("Скорость приближения:"));
        zoomSpeedSlider = createSlider(1, 100, (int)(cameraController.getZoomSpeed() * 10));
        zoomSpeedSlider.addChangeListener(e -> 
            cameraController.setZoomSpeed(zoomSpeedSlider.getValue() / 10.0f));
        
        // Кнопки управления камерой
        JPanel buttonPanel = new JPanel(new GridLayout(2, 3, 5, 5));
        
        JButton resetButton = new JButton("Сбросить");
        resetButton.addActionListener(e -> cameraController.resetCamera());
        
        JButton toggleProjButton = new JButton("Переключить проекцию");
        toggleProjButton.addActionListener(e -> cameraController.toggleProjection());
        
        JButton orbitButton = new JButton("Режим Orbit");
        orbitButton.addActionListener(e -> cameraController.setOrbitMode());
        
        JButton flyButton = new JButton("Режим Fly");
        flyButton.addActionListener(e -> cameraController.setFlyMode());
        
        JButton fpsButton = new JButton("Режим FPS");
        fpsButton.addActionListener(e -> cameraController.setFPSCamera());
        
        buttonPanel.add(resetButton);
        buttonPanel.add(toggleProjButton);
        buttonPanel.add(new JLabel()); // Пустая ячейка
        buttonPanel.add(orbitButton);
        buttonPanel.add(flyButton);
        buttonPanel.add(fpsButton);
        
        // Позиция камеры
        JPanel positionPanel = new JPanel(new GridLayout(3, 3, 5, 5));
        positionPanel.setBorder(BorderFactory.createTitledBorder("Позиция камеры"));
        
        JSpinner posXSpinner = createPositionSpinner(camera.getPosition().getX(), -100, 100, 0.1);
        JSpinner posYSpinner = createPositionSpinner(camera.getPosition().getY(), -100, 100, 0.1);
        JSpinner posZSpinner = createPositionSpinner(camera.getPosition().getZ(), -100, 100, 0.1);
        
        JSpinner targetXSpinner = createPositionSpinner(camera.getTarget().getX(), -100, 100, 0.1);
        JSpinner targetYSpinner = createPositionSpinner(camera.getTarget().getY(), -100, 100, 0.1);
        JSpinner targetZSpinner = createPositionSpinner(camera.getTarget().getZ(), -100, 100, 0.1);
        
        positionPanel.add(new JLabel("Pos X:"));
        positionPanel.add(new JLabel("Pos Y:"));
        positionPanel.add(new JLabel("Pos Z:"));
        positionPanel.add(posXSpinner);
        positionPanel.add(posYSpinner);
        positionPanel.add(posZSpinner);
        
        positionPanel.add(new JLabel("Target X:"));
        positionPanel.add(new JLabel("Target Y:"));
        positionPanel.add(new JLabel("Target Z:"));
        positionPanel.add(targetXSpinner);
        positionPanel.add(targetYSpinner);
        positionPanel.add(targetZSpinner);
        
        // Кнопка применения позиции
        JButton applyPositionButton = new JButton("Применить позицию");
        applyPositionButton.addActionListener(e -> {
            // Vector3f newPos = new Vector3f(
            //     (Double) posXSpinner.getValue(),
            //     (Double) posYSpinner.getValue(),
            //     (Double) posZSpinner.getValue()
            // );
            
            // Vector3f newTarget = new Vector3f(
            //     (Double) targetXSpinner.getValue(),
            //     (Double) targetYSpinner.getValue(),
            //     (Double) targetZSpinner.getValue()
            // );

            Vector3f newPos = new Vector3f(
                (Float) posXSpinner.getValue(),
                (Float) posYSpinner.getValue(),
                (Float) posZSpinner.getValue()
            );
            
            Vector3f newTarget = new Vector3f(
                (Float) targetXSpinner.getValue(),
                (Float) targetYSpinner.getValue(),
                (Float) targetZSpinner.getValue()
            );
            
            camera.setPosition(newPos);
            camera.setTarget(newTarget);
        });
        
        // Добавляем все компоненты
        add(modePanel);
        add(Box.createVerticalStrut(10));
        add(invertYCheckBox);
        add(Box.createVerticalStrut(10));
        add(settingsPanel);
        add(Box.createVerticalStrut(10));
        add(buttonPanel);
        add(Box.createVerticalStrut(10));
        add(positionPanel);
        add(Box.createVerticalStrut(10));
        add(applyPositionButton);
    }
    
    private JSlider createSlider(int min, int max, int value) {
        JSlider slider = new JSlider(JSlider.HORIZONTAL, min, max, value);
        slider.setMajorTickSpacing(20);
        slider.setMinorTickSpacing(5);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        return slider;
    }
    
    private JSpinner createPositionSpinner(double value, double min, double max, double step) {
        SpinnerNumberModel model = new SpinnerNumberModel(value, min, max, step);
        JSpinner spinner = new JSpinner(model);
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, "#0.0");
        spinner.setEditor(editor);
        return spinner;
    }
    
    private void updateUIFromController() {
        // Обновляем выпадающий список режимов
        switch (cameraController.getControlMode()) {
            case ORBIT: modeComboBox.setSelectedIndex(0); break;
            case FLY: modeComboBox.setSelectedIndex(1); break;
            case FPS: modeComboBox.setSelectedIndex(2); break;
        }
        
        // Обновляем чекбоксы и слайдеры
        invertYCheckBox.setSelected(cameraController.isInvertYAxis());
        rotationSpeedSlider.setValue((int)(cameraController.getRotationSpeed() * 10));
        moveSpeedSlider.setValue((int)(cameraController.getMoveSpeed() * 10));
        zoomSpeedSlider.setValue((int)(cameraController.getZoomSpeed() * 10));
    }
    
    public void updateFromCamera() {
        // Этот метод может быть вызван для обновления UI при изменении камеры извне
        updateUIFromController();
    }
}
