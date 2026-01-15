package com.cgvsu.ui;

import com.cgvsu.graphics.Camera;
import com.cgvsu.graphics.Scene;

import javax.swing.*;
import java.awt.*;


/*
 * Панель для отрисовки 3D-сцены.
 */
public class RenderPanel extends JPanel {
    
    private Scene scene;
    
    // Настройки отрисовки
    private boolean showWireframe = true;
    private boolean showAxes = true;
    private boolean showGrid = false;
    
    // Цвета
    private Color backgroundColor = new Color(30, 30, 40);
    private Color modelColor = new Color(100, 150, 255);
    private Color wireframeColor = new Color(200, 200, 200, 150);
    private Color axisXColor = new Color(255, 50, 50);
    private Color axisYColor = new Color(50, 255, 50);
    private Color axisZColor = new Color(50, 100, 255);
    private Color gridColor = new Color(100, 100, 100, 50);
    
    public RenderPanel(Scene scene) {
        this.scene = scene;
        setBackground(backgroundColor);
        setDoubleBuffered(true);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Очистка фона
        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        // Обновляем размеры в конвейере
        scene.updateViewport(getWidth(), getHeight());
        
        // Рисуем сетку (если включена)
        if (showGrid) {
            drawGrid(g2d);
        }
        
        // Рисуем оси координат (если включены)
        if (showAxes) {
            drawAxes(g2d);
        }
        
        // Рисуем модели из сцены
        drawScene(g2d);
        
        // Рисуем информацию о камере
        drawCameraInfo(g2d);
    }
    
    private void drawGrid(Graphics2D g2d) {
        g2d.setColor(gridColor);
        g2d.setStroke(new BasicStroke(1));
        
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int gridSize = 200;
        int step = 20;
        
        // Вертикальные линии
        for (int x = -gridSize; x <= gridSize; x += step) {
            int screenX = centerX + x;
            g2d.drawLine(screenX, centerY - gridSize, screenX, centerY + gridSize);
        }
        
        // Горизонтальные линии
        for (int y = -gridSize; y <= gridSize; y += step) {
            int screenY = centerY + y;
            g2d.drawLine(centerX - gridSize, screenY, centerX + gridSize, screenY);
        }
    }
    
    private void drawAxes(Graphics2D g2d) {
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int axisLength = 100;
        
        // Ось X (красная)
        g2d.setColor(axisXColor);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(centerX, centerY, centerX + axisLength, centerY);
        g2d.drawString("X", centerX + axisLength + 5, centerY);
        
        // Ось Y (зеленая)
        g2d.setColor(axisYColor);
        g2d.drawLine(centerX, centerY, centerX, centerY - axisLength);
        g2d.drawString("Y", centerX, centerY - axisLength - 5);
        
        // Ось Z (синяя)
        g2d.setColor(axisZColor);
        g2d.drawLine(centerX, centerY, centerX - axisLength / 2, centerY + axisLength / 2);
        g2d.drawString("Z", centerX - axisLength / 2 - 15, centerY + axisLength / 2);
    }
    
    private void drawScene(Graphics2D g2d) {
        // Здесь должна быть логика отрисовки моделей из сцены
        // Для демонстрации рисуем простую фигуру
        
        if (showWireframe) {
            drawWireframeCube(g2d);
        } else {
            drawSolidCube(g2d);
        }
    }
    
    private void drawWireframeCube(Graphics2D g2d) {
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int size = 100;
        
        // Вершины куба в 2D (упрощенно)
        int[] xPoints = {
            centerX - size, centerX + size, centerX + size, centerX - size, // Передняя грань
            centerX - size, centerX + size, centerX + size, centerX - size  // Задняя грань
        };
        
        int[] yPoints = {
            centerY - size, centerY - size, centerY + size, centerY + size, // Передняя грань
            centerY - size, centerY - size, centerY + size, centerY + size  // Задняя грань
        };
        
        // Рисуем ребра
        g2d.setColor(wireframeColor);
        g2d.setStroke(new BasicStroke(2));
        
        // Передняя грань
        for (int i = 0; i < 4; i++) {
            int next = (i + 1) % 4;
            g2d.drawLine(xPoints[i], yPoints[i], xPoints[next], yPoints[next]);
        }
        
        // Задняя грань
        for (int i = 4; i < 8; i++) {
            int next = 4 + (i + 1) % 4;
            g2d.drawLine(xPoints[i], yPoints[i], xPoints[next], yPoints[next]);
        }
        
        // Соединяем переднюю и заднюю грани
        for (int i = 0; i < 4; i++) {
            g2d.drawLine(xPoints[i], yPoints[i], xPoints[i + 4], yPoints[i + 4]);
        }
    }
    
    private void drawSolidCube(Graphics2D g2d) {
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int size = 100;
        
        // Рисуем переднюю грань
        g2d.setColor(modelColor);
        g2d.fillRect(centerX - size, centerY - size, size * 2, size * 2);
        
        // Рисуем боковые грани с перспективой
        Polygon side1 = new Polygon();
        side1.addPoint(centerX - size, centerY - size);
        side1.addPoint(centerX - size - size/2, centerY - size + size/2);
        side1.addPoint(centerX - size - size/2, centerY + size + size/2);
        side1.addPoint(centerX - size, centerY + size);
        
        g2d.setColor(modelColor.darker());
        g2d.fillPolygon(side1);
        
        Polygon side2 = new Polygon();
        side2.addPoint(centerX + size, centerY - size);
        side2.addPoint(centerX + size + size/2, centerY - size + size/2);
        side2.addPoint(centerX + size + size/2, centerY + size + size/2);
        side2.addPoint(centerX + size, centerY + size);
        
        g2d.setColor(modelColor.brighter());
        g2d.fillPolygon(side2);
    }
    
    private void drawCameraInfo(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        String cameraInfo = String.format("Камера: Pos(%.1f, %.1f, %.1f) Target(%.1f, %.1f, %.1f)",
            scene.getCamera().getPosition().getX(),
            scene.getCamera().getPosition().getY(),
            scene.getCamera().getPosition().getZ(),
            scene.getCamera().getTarget().getX(),
            scene.getCamera().getTarget().getY(),
            scene.getCamera().getTarget().getZ());
        
        String modeInfo = String.format("Режим: %s, Проекция: %s",
            scene.getCamera().getCameraType(),
            scene.getCamera().getCameraType() == Camera.CameraType.PERSPECTIVE ? "Перспективная" : "Ортографическая");
        
        g2d.drawString(cameraInfo, 10, 20);
        g2d.drawString(modeInfo, 10, 40);
        g2d.drawString("Управление: ЛКМ - вращение, ПКМ - панорамирование, Колесо - приближение", 10, getHeight() - 20);
    }
    
    // Геттеры и сеттеры
    
    public boolean isShowWireframe() { return showWireframe; }
    public void setShowWireframe(boolean showWireframe) { 
        this.showWireframe = showWireframe; 
        repaint();
    }
    
    public boolean isShowAxes() { return showAxes; }
    public void setShowAxes(boolean showAxes) { 
        this.showAxes = showAxes; 
        repaint();
    }
    
    public boolean isShowGrid() { return showGrid; }
    public void setShowGrid(boolean showGrid) { 
        this.showGrid = showGrid; 
        repaint();
    }
    
    public Color getBackgroundColor() { return backgroundColor; }
    public void setBackgroundColor(Color backgroundColor) { 
        this.backgroundColor = backgroundColor; 
        repaint();
    }
}
