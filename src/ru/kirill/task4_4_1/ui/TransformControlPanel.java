package ru.kirill.task4_4_1.ui;


import ru.kirill.task4_4_1.utils.Model;
import ru.kirill.task4_4_1.utils.Vector3f;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;


/*
 * Панель управления трансформациями модели.
 * Предоставляет элементы управления для масштабирования, вращения и перемещения.
 */
public class TransformControlPanel extends JPanel implements ModelTransformController.TransformChangeListener {
    
    private ModelTransformController transformController;
    
    // Элементы управления перемещением
    private JSpinner translateXSpinner;
    private JSpinner translateYSpinner;
    private JSpinner translateZSpinner;
    
    // Элементы управления вращением
    private JSpinner rotateXSpinner;
    private JSpinner rotateYSpinner;
    private JSpinner rotateZSpinner;
    
    // Элементы управления масштабированием
    private JSpinner scaleXSpinner;
    private JSpinner scaleYSpinner;
    private JSpinner scaleZSpinner;
    private JSpinner uniformScaleSpinner;
    
    // Кнопки
    private JButton applyButton;
    private JButton resetButton;
    
    // Форматтер для чисел
    private DecimalFormat decimalFormat = new DecimalFormat("#0.00");
    
    public TransformControlPanel(ModelTransformController controller) {
        this.transformController = controller;
        controller.addTransformChangeListener(this);
        
        initUI();
        updateUIFromController();
    }
    
    private void initUI() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder("Управление трансформациями"));
        
        // Панель перемещения
        JPanel translatePanel = new JPanel(new GridLayout(2, 4, 5, 5));
        translatePanel.setBorder(BorderFactory.createTitledBorder("Перемещение"));
        
        translatePanel.add(new JLabel("X:", SwingConstants.RIGHT));
        translateXSpinner = createSpinner(-100.0, 100.0, 0.0, 0.1);
        translatePanel.add(translateXSpinner);
        
        translatePanel.add(new JLabel("Y:", SwingConstants.RIGHT));
        translateYSpinner = createSpinner(-100.0, 100.0, 0.0, 0.1);
        translatePanel.add(translateYSpinner);
        
        translatePanel.add(new JLabel("Z:", SwingConstants.RIGHT));
        translateZSpinner = createSpinner(-100.0, 100.0, 0.0, 0.1);
        translatePanel.add(translateZSpinner);
        
        JButton translateApplyButton = new JButton("Применить");
        translateApplyButton.addActionListener(e -> applyTranslation());
        translatePanel.add(translateApplyButton);
        
        // Панель вращения
        JPanel rotatePanel = new JPanel(new GridLayout(2, 4, 5, 5));
        rotatePanel.setBorder(BorderFactory.createTitledBorder("Вращение (градусы)"));
        
        rotatePanel.add(new JLabel("X:", SwingConstants.RIGHT));
        rotateXSpinner = createSpinner(-180.0, 180.0, 0.0, 1.0);
        rotatePanel.add(rotateXSpinner);
        
        rotatePanel.add(new JLabel("Y:", SwingConstants.RIGHT));
        rotateYSpinner = createSpinner(-180.0, 180.0, 0.0, 1.0);
        rotatePanel.add(rotateYSpinner);
        
        rotatePanel.add(new JLabel("Z:", SwingConstants.RIGHT));
        rotateZSpinner = createSpinner(-180.0, 180.0, 0.0, 1.0);
        rotatePanel.add(rotateZSpinner);
        
        JButton rotateApplyButton = new JButton("Применить");
        rotateApplyButton.addActionListener(e -> applyRotation());
        rotatePanel.add(rotateApplyButton);
        
        // Панель масштабирования
        JPanel scalePanel = new JPanel(new GridLayout(3, 3, 5, 5));
        scalePanel.setBorder(BorderFactory.createTitledBorder("Масштабирование"));
        
        scalePanel.add(new JLabel("X:", SwingConstants.RIGHT));
        scaleXSpinner = createSpinner(0.01, 10.0, 1.0, 0.1);
        scalePanel.add(scaleXSpinner);
        
        scalePanel.add(new JLabel("Y:", SwingConstants.RIGHT));
        scaleYSpinner = createSpinner(0.01, 10.0, 1.0, 0.1);
        scalePanel.add(scaleYSpinner);
        
        scalePanel.add(new JLabel("Z:", SwingConstants.RIGHT));
        scaleZSpinner = createSpinner(0.01, 10.0, 1.0, 0.1);
        scalePanel.add(scaleZSpinner);
        
        JButton scaleApplyButton = new JButton("Применить");
        scaleApplyButton.addActionListener(e -> applyScale());
        scalePanel.add(scaleApplyButton);
        
        // Единое масштабирование
        scalePanel.add(new JLabel("Единое:", SwingConstants.RIGHT));
        uniformScaleSpinner = createSpinner(0.01, 10.0, 1.0, 0.1);
        scalePanel.add(uniformScaleSpinner);
        
        JButton uniformScaleButton = new JButton("Применить");
        uniformScaleButton.addActionListener(e -> applyUniformScale());
        scalePanel.add(uniformScaleButton);
        
        // Панель кнопок управления
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        
        applyButton = new JButton("Применить трансформации");
        applyButton.setToolTipText("Финализировать трансформации (применить к вершинам модели)");
        applyButton.addActionListener(e -> transformController.applyTransformation());
        
        resetButton = new JButton("Сбросить");
        resetButton.addActionListener(e -> transformController.resetTransformation());
        
        controlPanel.add(applyButton);
        controlPanel.add(resetButton);
        
        // Добавление всех панелей
        add(translatePanel);
        add(Box.createVerticalStrut(10));
        add(rotatePanel);
        add(Box.createVerticalStrut(10));
        add(scalePanel);
        add(Box.createVerticalStrut(10));
        add(controlPanel);
    }
    
    private JSpinner createSpinner(double min, double max, double initial, double step) {
        SpinnerNumberModel model = new SpinnerNumberModel(initial, min, max, step);
        JSpinner spinner = new JSpinner(model);
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, "#0.00");
        spinner.setEditor(editor);
        return spinner;
    }
    
    private void applyTranslation() {
        double x = (Double) translateXSpinner.getValue();
        double y = (Double) translateYSpinner.getValue();
        double z = (Double) translateZSpinner.getValue();
        
        transformController.setTranslation((float)x, (float)y, (float)z);
    }
    
    private void applyRotation() {
        double x = (Double) rotateXSpinner.getValue();
        double y = (Double) rotateYSpinner.getValue();
        double z = (Double) rotateZSpinner.getValue();
        
        transformController.setRotation((float)x, (float)y, (float)z);
    }
    
    private void applyScale() {
        double x = (Double) scaleXSpinner.getValue();
        double y = (Double) scaleYSpinner.getValue();
        double z = (Double) scaleZSpinner.getValue();
        
        transformController.setScale((float)x, (float)y, (float)z);
    }
    
    private void applyUniformScale() {
        double scale = (Double) uniformScaleSpinner.getValue();
        transformController.scaleUniform((float)scale);
    }
    
    @Override
    public void onTransformChanged(ModelTransformController controller) {
        updateUIFromController();
    }
    
    @Override
    public void onTransformApplied(Model model) {
        // Обновляем UI после применения трансформаций
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, 
                "Трансформации применены к модели. Модель обновлена.",
                "Трансформация", 
                JOptionPane.INFORMATION_MESSAGE);
        });
    }
    
    private void updateUIFromController() {
        Vector3f translation = transformController.getTranslation();
        Vector3f rotation = transformController.getRotation();
        Vector3f scale = transformController.getScale();
        
        // Обновляем значения в спиннерах
        translateXSpinner.setValue(translation.getX());
        translateYSpinner.setValue(translation.getY());
        translateZSpinner.setValue(translation.getZ());
        
        rotateXSpinner.setValue(rotation.getX());
        rotateYSpinner.setValue(rotation.getY());
        rotateZSpinner.setValue(rotation.getZ());
        
        scaleXSpinner.setValue(scale.getX());
        scaleYSpinner.setValue(scale.getY());
        scaleZSpinner.setValue(scale.getZ());
        
        // Для единого масштабирования используем среднее значение
        float uniformScale = (scale.getX() + scale.getY() + scale.getZ()) / 3.0f;
        uniformScaleSpinner.setValue(uniformScale);
    }
    
    public void setTransformController(ModelTransformController controller) {
        if (this.transformController != null) {
            this.transformController.removeTransformChangeListener(this);
        }
        
        this.transformController = controller;
        if (controller != null) {
            controller.addTransformChangeListener(this);
        }
        
        updateUIFromController();
    }
}
