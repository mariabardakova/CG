package ru.kirill.task4_4_1.math;


/*
 * Утилитарный класс с математическими функциями.
 */
public class MathUtils {

    public static final float PI = (float) Math.PI;
    public static final float EPSILON = 1e-7f;
    
    private MathUtils() {
        // Утилитарный класс
    }
    
    public static float toRadians(float degrees) {
        return degrees * PI / 180.0f;
    }
    
    public static float toDegrees(float radians) {
        return radians * 180.0f / PI;
    }
    
    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
    
    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
    
    public static boolean floatEquals(float a, float b) {
        return Math.abs(a - b) < EPSILON;
    }
    
    // Линейная интерполяция между двумя векторами
    public static float[] lerpVector(float[] a, float[] b, float t) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Vectors must have same length");
        }
        
        float[] result = new float[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = lerp(a[i], b[i], t);
        }
        return result;
    }
}
