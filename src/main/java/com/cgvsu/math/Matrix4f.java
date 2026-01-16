package com.cgvsu.math;


/*
 * Класс для работы с матрицами 4x4.
 * Используется для аффинных преобразований в 3D-графике.
 * Векторы представляются как столбцы.
 */
public class Matrix4f {
    private final float[][] m;
    
    public Matrix4f() {
        m = new float[4][4];
        setIdentity();
    }
    
    public Matrix4f(float[][] matrix) {
        if (matrix.length != 4 || matrix[0].length != 4) {
            throw new IllegalArgumentException("Matrix must be 4x4");
        }
        m = new float[4][4];
        for (int i = 0; i < 4; i++) {
            System.arraycopy(matrix[i], 0, m[i], 0, 4);
        }
    }
    
    // Копирующий конструктор
    public Matrix4f(Matrix4f other) {
        m = new float[4][4];
        for (int i = 0; i < 4; i++) {
            System.arraycopy(other.m[i], 0, m[i], 0, 4);
        }
    }
    
    public static Matrix4f identity() {
        return new Matrix4f();
    }
    
    public void setIdentity() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                m[i][j] = (i == j) ? 1.0f : 0.0f;
            }
        }
    }
    
    // Умножение матриц (C = A * B)
    public Matrix4f mul(Matrix4f other) {
        float[][] result = new float[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                float sum = 0;
                for (int k = 0; k < 4; k++) {
                    sum += m[i][k] * other.m[k][j];
                }
                result[i][j] = sum;
            }
        }
        return new Matrix4f(result);
    }
    
    // Умножение матрицы на вектор-столбец (v' = M * v)
    public ColumnVector mul(ColumnVector vec) {
        if (vec.size() != 4) {
            throw new IllegalArgumentException("Vector must have 4 components");
        }
        
        float[] result = new float[4];
        for (int i = 0; i < 4; i++) {
            float sum = 0;
            for (int j = 0; j < 4; j++) {
                sum += m[i][j] * vec.get(j);
            }
            result[i] = sum;
        }
        
        return new ColumnVector(result);
    }
    
    // Умножение матрицы на Vector3f (с преобразованием в гомогенные координаты)
    public Vector3f mul(Vector3f vec) {
        ColumnVector columnVec = new ColumnVector(vec);
        ColumnVector result = mul(columnVec);
        return result.toVector3f();
    }
    
    // Транспонирование матрицы
    public Matrix4f transpose() {
        float[][] result = new float[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[i][j] = m[j][i];
            }
        }
        return new Matrix4f(result);
    }
    
    // Сложение матриц
    public Matrix4f add(Matrix4f other) {
        float[][] result = new float[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[i][j] = m[i][j] + other.m[i][j];
            }
        }
        return new Matrix4f(result);
    }
    
    // Вычитание матриц
    public Matrix4f subtract(Matrix4f other) {
        float[][] result = new float[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[i][j] = m[i][j] - other.m[i][j];
            }
        }
        return new Matrix4f(result);
    }
    
    // Умножение на скаляр
    public Matrix4f multiply(float scalar) {
        float[][] result = new float[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[i][j] = m[i][j] * scalar;
            }
        }
        return new Matrix4f(result);
    }
    
    // Статические методы для создания матриц преобразований
    
    public static Matrix4f translation(float tx, float ty, float tz) {
        Matrix4f mat = new Matrix4f();
        mat.set(0, 3, tx);
        mat.set(1, 3, ty);
        mat.set(2, 3, tz);
        return mat;
    }
    
    public static Matrix4f scaling(float sx, float sy, float sz) {
        Matrix4f mat = new Matrix4f();
        mat.set(0, 0, sx);
        mat.set(1, 1, sy);
        mat.set(2, 2, sz);
        return mat;
    }
    
    public static Matrix4f rotationX(float angleDegrees) {
        float angleRad = (float) Math.toRadians(angleDegrees);
        float cos = (float) Math.cos(angleRad);
        float sin = (float) Math.sin(angleRad);
        
        Matrix4f mat = new Matrix4f();
        mat.set(1, 1, cos);
        mat.set(1, 2, -sin);
        mat.set(2, 1, sin);
        mat.set(2, 2, cos);
        return mat;
    }
    
    public static Matrix4f rotationY(float angleDegrees) {
        float angleRad = (float) Math.toRadians(angleDegrees);
        float cos = (float) Math.cos(angleRad);
        float sin = (float) Math.sin(angleRad);
        
        Matrix4f mat = new Matrix4f();
        mat.set(0, 0, cos);
        mat.set(0, 2, sin);
        mat.set(2, 0, -sin);
        mat.set(2, 2, cos);
        return mat;
    }
    
    public static Matrix4f rotationZ(float angleDegrees) {
        float angleRad = (float) Math.toRadians(angleDegrees);
        float cos = (float) Math.cos(angleRad);
        float sin = (float) Math.sin(angleRad);
        
        Matrix4f mat = new Matrix4f();
        mat.set(0, 0, cos);
        mat.set(0, 1, -sin);
        mat.set(1, 0, sin);
        mat.set(1, 1, cos);
        return mat;
    }
    
    // Создание матрицы перспективной проекции
    public static Matrix4f perspective(float fovDegrees, float aspectRatio, float near, float far) {
        Matrix4f result = new Matrix4f();
        float tangentMinusOnDegree = (float) (1.0F / (Math.tan(fovDegrees * 0.5F)));
        result.set(0, 0, tangentMinusOnDegree / aspectRatio);
        result.set(1, 1, tangentMinusOnDegree);
        result.set(2, 2, (far + near) / (far - near));
        result.set(2, 3, 1.0F);
        result.set(3, 2, 2 * (near * far) / (near - far));
        return result;
    }
    
    // Создание матрицы ортографической проекции
    public static Matrix4f orthographic(float left, float right, float bottom, float top, float near, float far) {
        Matrix4f mat = new Matrix4f();
        mat.set(0, 0, 2.0f / (right - left));
        mat.set(0, 3, -(right + left) / (right - left));
        mat.set(1, 1, 2.0f / (top - bottom));
        mat.set(1, 3, -(top + bottom) / (top - bottom));
        mat.set(2, 2, -2.0f / (far - near));
        mat.set(2, 3, -(far + near) / (far - near));
        
        return mat;
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f target, Vector3f up) {
        Vector3f f = target.subtract(eye).normalize();
        Vector3f s = up.cross(f).normalize();  // Изменено: up × f
        Vector3f u = f.cross(s);               // Изменено: f × s
        
        Matrix4f mat = new Matrix4f();
        mat.set(0, 0, s.getX());
        mat.set(0, 1, s.getY());
        mat.set(0, 2, s.getZ());
        mat.set(1, 0, u.getX());
        mat.set(1, 1, u.getY());
        mat.set(1, 2, u.getZ());
        mat.set(2, 0, f.getX());  // Без минуса для lookAt
        mat.set(2, 1, f.getY());
        mat.set(2, 2, f.getZ());
        mat.set(3, 0, -s.dot(eye));
        mat.set(3, 1, -u.dot(eye));
        mat.set(3, 2, -f.dot(eye));
        
        return mat;
    }
    
    // Геттеры и сеттеры
    public float get(int row, int col) {
        return m[row][col];
    }
    
    public void set(int row, int col, float value) {
        m[row][col] = value;
    }
    
    public float[][] toArray() {
        float[][] copy = new float[4][4];
        for (int i = 0; i < 4; i++) {
            System.arraycopy(m[i], 0, copy[i], 0, 4);
        }
        return copy;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            sb.append("[");
            for (int j = 0; j < 4; j++) {
                sb.append(String.format("%8.4f", m[i][j]));
                if (j < 3) sb.append(" ");
            }
            sb.append("]\n");
        }
        return sb.toString();
    }
}