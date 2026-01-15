package com.cgvsu.math;


/*
 * Класс для работы с матрицами 3x3.
 * Используется для преобразований нормалей.
 * Векторы представляются как столбцы.
 */
public class Matrix3f {
    private final float[][] m;
    
    public Matrix3f() {
        m = new float[3][3];
        setIdentity();
    }
    
    public Matrix3f(float[][] matrix) {
        if (matrix.length != 3 || matrix[0].length != 3) {
            throw new IllegalArgumentException("Matrix must be 3x3");
        }
        m = new float[3][3];
        for (int i = 0; i < 3; i++) {
            System.arraycopy(matrix[i], 0, m[i], 0, 3);
        }
    }
    
    // Копирующий конструктор
    public Matrix3f(Matrix3f other) {
        m = new float[3][3];
        for (int i = 0; i < 3; i++) {
            System.arraycopy(other.m[i], 0, m[i], 0, 3);
        }
    }
    
    public static Matrix3f identity() {
        return new Matrix3f();
    }
    
    public void setIdentity() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                m[i][j] = (i == j) ? 1.0f : 0.0f;
            }
        }
    }
    
    // Умножение матриц (C = A * B)
    public Matrix3f mul(Matrix3f other) {
        float[][] result = new float[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                float sum = 0;
                for (int k = 0; k < 3; k++) {
                    sum += m[i][k] * other.m[k][j];
                }
                result[i][j] = sum;
            }
        }
        return new Matrix3f(result);
    }
    
    // Умножение матрицы на вектор-столбец (v' = M * v)
    public ColumnVector mul(ColumnVector vec) {
        if (vec.size() != 3) {
            throw new IllegalArgumentException("Vector must have 3 components");
        }
        
        float[] result = new float[3];
        for (int i = 0; i < 3; i++) {
            float sum = 0;
            for (int j = 0; j < 3; j++) {
                sum += m[i][j] * vec.get(j);
            }
            result[i] = sum;
        }
        
        return new ColumnVector(result);
    }
    
    // Умножение матрицы на Vector3f
    public Vector3f mul(Vector3f vec) {
        ColumnVector columnVec = new ColumnVector(vec.getX(), vec.getY(), vec.getZ());
        ColumnVector result = mul(columnVec);
        return result.toVector3f();
    }
    
    // Транспонирование матрицы
    public Matrix3f transpose() {
        float[][] result = new float[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result[i][j] = m[j][i];
            }
        }
        return new Matrix3f(result);
    }
    
    // Вычисление обратной матрицы (для преобразования нормалей)
    public Matrix3f inverse() {
        float det = determinant();
        if (Math.abs(det) < 1e-10f) {
            throw new ArithmeticException("Matrix is singular, cannot compute inverse");
        }
        
        float invDet = 1.0f / det;
        float[][] result = new float[3][3];
        
        result[0][0] = (m[1][1] * m[2][2] - m[1][2] * m[2][1]) * invDet;
        result[0][1] = (m[0][2] * m[2][1] - m[0][1] * m[2][2]) * invDet;
        result[0][2] = (m[0][1] * m[1][2] - m[0][2] * m[1][1]) * invDet;
        result[1][0] = (m[1][2] * m[2][0] - m[1][0] * m[2][2]) * invDet;
        result[1][1] = (m[0][0] * m[2][2] - m[0][2] * m[2][0]) * invDet;
        result[1][2] = (m[0][2] * m[1][0] - m[0][0] * m[1][2]) * invDet;
        result[2][0] = (m[1][0] * m[2][1] - m[1][1] * m[2][0]) * invDet;
        result[2][1] = (m[0][1] * m[2][0] - m[0][0] * m[2][1]) * invDet;
        result[2][2] = (m[0][0] * m[1][1] - m[0][1] * m[1][0]) * invDet;
        
        return new Matrix3f(result);
    }
    
    // Вычисление определителя
    public float determinant() {
        return m[0][0] * (m[1][1] * m[2][2] - m[1][2] * m[2][1])
             - m[0][1] * (m[1][0] * m[2][2] - m[1][2] * m[2][0])
             + m[0][2] * (m[1][0] * m[2][1] - m[1][1] * m[2][0]);
    }
    
    // Извлечение верхней левой 3x3 части из матрицы 4x4
    public static Matrix3f fromMatrix4f(Matrix4f mat4) {
        float[][] m3 = new float[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                m3[i][j] = mat4.get(i, j);
            }
        }
        return new Matrix3f(m3);
    }
    
    // Создание матрицы для преобразования нормалей
    // Для нормалей используется транспонированная обратная матрица верхней левой 3x3 части
    public static Matrix3f normalMatrix(Matrix4f modelMatrix) {
        Matrix3f upperLeft = fromMatrix4f(modelMatrix);
        return upperLeft.inverse().transpose();
    }
    
    // Геттеры
    public float get(int row, int col) {
        return m[row][col];
    }
    
    public void set(int row, int col, float value) {
        m[row][col] = value;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            sb.append("[");
            for (int j = 0; j < 3; j++) {
                sb.append(String.format("%8.4f", m[i][j]));
                if (j < 2) sb.append(" ");
            }
            sb.append("]\n");
        }
        return sb.toString();
    }
}
