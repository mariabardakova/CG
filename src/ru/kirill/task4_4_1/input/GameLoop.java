package ru.kirill.task4_4_1.input;


/*
 * Игровой цикл для непрерывного обновления состояния приложения.
 * Обеспечивает плавное движение и отзывчивое управление.
 */
public class GameLoop implements Runnable {
    
    private Thread gameThread;
    private volatile boolean running = false;
    
    // Компоненты для обновления
    private InputManager inputManager;
    private Runnable renderCallback;
    private Runnable updateCallback;
    
    // Настройки цикла
    private int targetFPS = 60;
    private int targetUPS = 60; // Обновлений в секунду
    
    // Статистика
    private int fps = 0;
    private int ups = 0;
    private long lastStatsTime = 0;
    
    public GameLoop(InputManager inputManager) {
        this.inputManager = inputManager;
    }
    
    public GameLoop(InputManager inputManager, Runnable renderCallback, Runnable updateCallback) {
        this(inputManager);
        this.renderCallback = renderCallback;
        this.updateCallback = updateCallback;
    }
    
    public void start() {
        if (running) return;
        
        running = true;
        gameThread = new Thread(this, "GameLoop");
        gameThread.start();
        
        System.out.println("Игровой цикл запущен");
    }
    
    public void stop() {
        running = false;
        try {
            if (gameThread != null) {
                gameThread.join(1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("Игровой цикл остановлен");
    }
    
    @Override
    public void run() {
        long lastTime = System.nanoTime();
        long timer = System.currentTimeMillis();
        
        // Интервалы времени
        double nsPerFrame = 1000000000.0 / targetFPS;
        double nsPerUpdate = 1000000000.0 / targetUPS;
        
        double deltaFrame = 0;
        double deltaUpdate = 0;
        
        int frames = 0;
        int updates = 0;
        
        while (running) {
            long now = System.nanoTime();
            long currentTime = System.currentTimeMillis();
            
            deltaFrame += (now - lastTime) / nsPerFrame;
            deltaUpdate += (now - lastTime) / nsPerUpdate;
            lastTime = now;
            
            // Обновление логики
            while (deltaUpdate >= 1) {
                update();
                updates++;
                deltaUpdate--;
            }
            
            // Отрисовка
            if (deltaFrame >= 1) {
                render();
                frames++;
                deltaFrame--;
            }
            
            // Статистика раз в секунду
            if (currentTime - timer >= 1000) {
                fps = frames;
                ups = updates;
                frames = 0;
                updates = 0;
                timer += 1000;
                
                // Вывод статистики (опционально)
                // System.out.printf("FPS: %d, UPS: %d\n", fps, ups);
            }
            
            // Небольшая пауза для снижения нагрузки на CPU
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running = false;
            }
        }
    }
    
    private void update() {
        // Обновляем менеджер ввода
        if (inputManager != null) {
            inputManager.update();
        }
        
        // Вызываем пользовательский колбэк
        if (updateCallback != null) {
            updateCallback.run();
        }
    }
    
    private void render() {
        // Вызываем пользовательский колбэк отрисовки
        if (renderCallback != null) {
            renderCallback.run();
        }
    }
    
    // Геттеры и сеттеры
    
    public int getTargetFPS() { return targetFPS; }
    public void setTargetFPS(int targetFPS) { this.targetFPS = targetFPS; }
    
    public int getTargetUPS() { return targetUPS; }
    public void setTargetUPS(int targetUPS) { this.targetUPS = targetUPS; }
    
    public int getFPS() { return fps; }
    public int getUPS() { return ups; }
    
    public boolean isRunning() { return running; }
    
    public InputManager getInputManager() { return inputManager; }
    public void setInputManager(InputManager inputManager) { this.inputManager = inputManager; }
    
    public Runnable getRenderCallback() { return renderCallback; }
    public void setRenderCallback(Runnable renderCallback) { this.renderCallback = renderCallback; }
    
    public Runnable getUpdateCallback() { return updateCallback; }
    public void setUpdateCallback(Runnable updateCallback) { this.updateCallback = updateCallback; }
}
