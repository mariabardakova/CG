package ru.kirill.task4_4_1.math;


import ru.kirill.task4_4_1.utils.Vector3f;
import ru.kirill.task4_4_1.utils.Vector2f;


/*
 * Класс для представления векторов-столбцов.
 * Используется для матричных операций в 3D-графике.
 */
public class ColumnVector {
    private final float[] data;
    private final int size;
    
    public ColumnVector(int size) {
        this.size = size;
        this.data = new float[size];
    }
    
    public ColumnVector(float... values) {
        this.size = values.length;
        this.data = values.clone();
    }
    
    public ColumnVector(Vector3f vec) {
        this.size = 4; // Гомогенные координаты
        this.data = new float[] {vec.getX(), vec.getY(), vec.getZ(), 1.0f};
    }
    
    public ColumnVector(Vector2f vec) {
        this.size = 3; // Гомогенные координаты для 2D
        this.data = new float[] {vec.getX(), vec.getY(), 1.0f};
    }
    
    // Создание вектора для нормалей (w=0)
    public static ColumnVector forNormal(Vector3f normal) {
        return new ColumnVector(new float[] {normal.getX(), normal.getY(), normal.getZ(), 0.0f});
    }
    
    // Умножение матрицы на вектор-столбец
    public ColumnVector multiply(Matrix4f matrix) {
        if (this.size != 4) {
            throw new IllegalArgumentException("Vector must have size 4 for 4x4 matrix multiplication");
        }
        
        float[] result = new float[4];
        for (int i = 0; i < 4; i++) {
            float sum = 0;
            for (int j = 0; j < 4; j++) {
                sum += matrix.get(i, j) * data[j];
            }
            result[i] = sum;
        }
        
        // Перспективное деление
        if (result[3] != 0.0f && result[3] != 1.0f) {
            for (int i = 0; i < 3; i++) {
                result[i] /= result[3];
            }
            result[3] = 1.0f;
        }
        
        return new ColumnVector(result);
    }
    
    // Умножение матрицы 3x3 на вектор-столбец 3
    public ColumnVector multiply(Matrix3f matrix) {
        if (this.size != 3) {
            throw new IllegalArgumentException("Vector must have size 3 for 3x3 matrix multiplication");
        }
        
        float[] result = new float[3];
        for (int i = 0; i < 3; i++) {
            float sum = 0;
            for (int j = 0; j < 3; j++) {
                sum += matrix.get(i, j) * data[j];
            }
            result[i] = sum;
        }
        
        return new ColumnVector(result);
    }
    
    // Преобразование в Vector3f (из гомогенных координат)
    public Vector3f toVector3f() {
        if (size < 3) {
            throw new IllegalStateException("Vector must have at least 3 components");
        }
        return new Vector3f(data[0], data[1], data[2]);
    }
    
    // Преобразование в Vector2f (из гомогенных координат)
    public Vector2f toVector2f() {
        if (size < 2) {
            throw new IllegalStateException("Vector must have at least 2 components");
        }
        return new Vector2f(data[0], data[1]);
    }
    
    // Скалярное произведение
    public float dot(ColumnVector other) {
        if (this.size != other.size) {
            throw new IllegalArgumentException("Vectors must have same size");
        }
        
        float sum = 0;
        for (int i = 0; i < size; i++) {
            sum += data[i] * other.data[i];
        }
        return sum;
    }
    
    // Сложение векторов
    public ColumnVector add(ColumnVector other) {
        if (this.size != other.size) {
            throw new IllegalArgumentException("Vectors must have same size");
        }
        
        float[] result = new float[size];
        for (int i = 0; i < size; i++) {
            result[i] = data[i] + other.data[i];
        }
        return new ColumnVector(result);
    }
    
    // Вычитание векторов
    public ColumnVector subtract(ColumnVector other) {
        if (this.size != other.size) {
            throw new IllegalArgumentException("Vectors must have same size");
        }
        
        float[] result = new float[size];
        for (int i = 0; i < size; i++) {
            result[i] = data[i] - other.data[i];
        }
        return new ColumnVector(result);
    }
    
    // Умножение на скаляр
    public ColumnVector multiply(float scalar) {
        float[] result = new float[size];
        for (int i = 0; i < size; i++) {
            result[i] = data[i] * scalar;
        }
        return new ColumnVector(result);
    }
    
    // Получение значения по индексу
    public float get(int index) {
        return data[index];
    }
    
    public int size() {
        return size;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < size; i++) {
            sb.append(String.format("%.4f", data[i]));
            if (i < size - 1) sb.append(", ");
        }
        sb.append("]^T");
        return sb.toString();
    }
}
