package ru.kirill.task4_4_1.graphics;


import ru.kirill.task4_4_1.utils.Vector3f;


/**
 * Класс для управления освещением сцены.
 */
public class SceneLighting {
    private Vector3f ambientColor;
    private Vector3f diffuseColor;
    private Vector3f specularColor;
    private Vector3f lightPosition;
    
    private boolean enabled;
    private float ambientIntensity;
    private float diffuseIntensity;
    private float specularIntensity;
    private float shininess;
    
    public SceneLighting() {
        // Стандартные параметры освещения
        this.ambientColor = new Vector3f(1.0f, 1.0f, 1.0f); // Белый
        this.diffuseColor = new Vector3f(1.0f, 1.0f, 1.0f);
        this.specularColor = new Vector3f(1.0f, 1.0f, 1.0f);
        this.lightPosition = new Vector3f(10.0f, 10.0f, 10.0f);
        
        this.enabled = true;
        this.ambientIntensity = 0.2f;
        this.diffuseIntensity = 0.7f;
        this.specularIntensity = 0.5f;
        this.shininess = 32.0f;
    }
    
    // Вычисление освещенности для вершины
    public Vector3f calculateLighting(Vector3f position, Vector3f normal, Vector3f viewDirection) {
        if (!enabled) {
            return new Vector3f(1.0f, 1.0f, 1.0f); // Без освещения - белый цвет
        }
        
        // Нормализуем векторы
        normal = normal.normalize();
        viewDirection = viewDirection.normalize();
        Vector3f lightDir = lightPosition.subtract(position).normalize();
        
        // Ambient component
        Vector3f ambient = ambientColor.multiply(ambientIntensity);
        
        // Diffuse component
        float diff = Math.max(normal.dot(lightDir), 0.0f);
        Vector3f diffuse = diffuseColor.multiply(diffuseIntensity * diff);
        
        // Specular component
        Vector3f reflectDir = lightDir.multiply(-1.0f).add(normal.multiply(2.0f * normal.dot(lightDir)));
        float spec = (float)Math.pow(Math.max(viewDirection.dot(reflectDir), 0.0f), shininess);
        Vector3f specular = specularColor.multiply(specularIntensity * spec);
        
        // Combine results
        return new Vector3f(
            clamp(ambient.getX() + diffuse.getX() + specular.getX()),
            clamp(ambient.getY() + diffuse.getY() + specular.getY()),
            clamp(ambient.getZ() + diffuse.getZ() + specular.getZ())
        );
    }
    
    private float clamp(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }
    
    // Геттеры и сеттеры
    public boolean isEnabled() { return enabled; }
    public Vector3f getAmbientColor() { return ambientColor; }
    public Vector3f getDiffuseColor() { return diffuseColor; }
    public Vector3f getSpecularColor() { return specularColor; }
    public Vector3f getLightPosition() { return lightPosition; }
    public float getAmbientIntensity() { return ambientIntensity; }
    public float getDiffuseIntensity() { return diffuseIntensity; }
    public float getSpecularIntensity() { return specularIntensity; }
    public float getShininess() { return shininess; }
    
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setAmbientColor(Vector3f ambientColor) { this.ambientColor = ambientColor; }
    public void setDiffuseColor(Vector3f diffuseColor) { this.diffuseColor = diffuseColor; }
    public void setSpecularColor(Vector3f specularColor) { this.specularColor = specularColor; }
    public void setLightPosition(Vector3f lightPosition) { this.lightPosition = lightPosition; }
    public void setAmbientIntensity(float ambientIntensity) { this.ambientIntensity = ambientIntensity; }
    public void setDiffuseIntensity(float diffuseIntensity) { this.diffuseIntensity = diffuseIntensity; }
    public void setSpecularIntensity(float specularIntensity) { this.specularIntensity = specularIntensity; }
    public void setShininess(float shininess) { this.shininess = shininess; }
}
