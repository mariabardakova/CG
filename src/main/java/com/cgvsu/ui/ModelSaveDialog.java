package com.cgvsu.ui;


import com.cgvsu.model.Model;
import com.cgvsu.objwriter.ObjWriter;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Диалоговое окно для сохранения модели с выбором, учитывать ли трансформации.
 */
public class ModelSaveDialog extends JDialog {
    
    private Model model;
    private ModelTransformController transformController;
    
    private JCheckBox applyTransformationsCheckBox;
    private JTextField fileNameField;
    private JButton browseButton;
    private JButton saveButton;
    private JButton cancelButton;
    
    private boolean saved = false;
    private File selectedFile;
    
    public ModelSaveDialog(Frame owner, Model model, ModelTransformController controller) {
        super(owner, "Сохранить модель", true);
        this.model = model;
        this.transformController = controller;
        
        initUI();
        pack();
        setLocationRelativeTo(owner);
    }
    
    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        
        // Основная панель
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Выбор файла
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("Имя файла:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        fileNameField = new JTextField(20);
        fileNameField.setText("model.obj");
        mainPanel.add(fileNameField, gbc);
        
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        browseButton = new JButton("Обзор...");
        browseButton.addActionListener(e -> browseForFile());
        mainPanel.add(browseButton, gbc);
        
        // Опции сохранения
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        applyTransformationsCheckBox = new JCheckBox("Сохранить с примененными трансформациями", false);
        applyTransformationsCheckBox.setToolTipText("Если отмечено, модель будет сохранена с примененными трансформациями (масштабирование, вращение, перемещение)");
        mainPanel.add(applyTransformationsCheckBox, gbc);
        
        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("Сохранить");
        saveButton.addActionListener(e -> saveModel());
        
        cancelButton = new JButton("Отмена");
        cancelButton.addActionListener(e -> setVisible(false));
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Настройка диалога
        getRootPane().setDefaultButton(saveButton);
    }
    
    private void browseForFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Сохранить модель как...");
        fileChooser.setSelectedFile(new File(fileNameField.getText()));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".obj");
            }
            
            @Override
            public String getDescription() {
                return "OBJ Files (*.obj)";
            }
        });
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            // Добавляем расширение .obj если его нет
            if (!selectedFile.getName().toLowerCase().endsWith(".obj")) {
                selectedFile = new File(selectedFile.getAbsolutePath() + ".obj");
            }
            fileNameField.setText(selectedFile.getAbsolutePath());
        }
    }
    
    private void saveModel() {
        String filePath = fileNameField.getText().trim();
        if (filePath.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Пожалуйста, укажите имя файла", 
                "Ошибка", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        boolean applyTransformations = applyTransformationsCheckBox.isSelected();
        
        try {
            // Получаем модель с учетом или без учета трансформаций
            Model modelToSave = transformController.getTransformedModel(applyTransformations);
            
            if (modelToSave == null) {
                modelToSave = model;
            }
            
            // Сохраняем модель
            ObjWriter.write(modelToSave, filePath);
            
            saved = true;
            JOptionPane.showMessageDialog(this, 
                "Модель успешно сохранена!", 
                "Сохранение", 
                JOptionPane.INFORMATION_MESSAGE);
            
            setVisible(false);
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                "Ошибка при сохранении файла: " + e.getMessage(), 
                "Ошибка", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Ошибка: " + e.getMessage(), 
                "Ошибка", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    public boolean isSaved() {
        return saved;
    }
    
    public File getSelectedFile() {
        return selectedFile;
    }
    
    public static void showSaveDialog(Frame owner, Model model, ModelTransformController controller) {
        ModelSaveDialog dialog = new ModelSaveDialog(owner, model, controller);
        dialog.setVisible(true);
    }
}
