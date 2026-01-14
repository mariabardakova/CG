package ru.kirill.task4_4_1.math;


import ru.kirill.task4_4_1.utils.Vector3f;


/*
 * Класс для управления аффинными преобразованиями.
 * Обеспечивает правильный порядок преобразований для векторов-столбцов.
 */
public class Transformation {
    private Matrix4f transformationMatrix;
    
    public Transformation() {
        transformationMatrix = Matrix4f.identity();
    }
    
    // Применение преобразований в правильном порядке для векторов-столбцов
    // Порядок: M = Translation * Rotation * Scaling
    public void applyTranslation(float tx, float ty, float tz) {
        Matrix4f translation = Matrix4f.translation(tx, ty, tz);
        // Для векторов-столбцов новые преобразования умножаются слева
        transformationMatrix = translation.mul(transformationMatrix);
    }
    
    public void applyScaling(float sx, float sy, float sz) {
        Matrix4f scaling = Matrix4f.scaling(sx, sy, sz);
        // Масштабирование применяется первым (умножается справа)
        transformationMatrix = transformationMatrix.mul(scaling);
    }
    
    public void applyRotationX(float angleDegrees) {
        Matrix4f rotation = Matrix4f.rotationX(angleDegrees);
        // Вращение применяется после масштабирования, но до переноса
        transformationMatrix = transformationMatrix.mul(rotation);
    }
    
    public void applyRotationY(float angleDegrees) {
        Matrix4f rotation = Matrix4f.rotationY(angleDegrees);
        transformationMatrix = transformationMatrix.mul(rotation);
    }
    
    public void applyRotationZ(float angleDegrees) {
        Matrix4f rotation = Matrix4f.rotationZ(angleDegrees);
        transformationMatrix = transformationMatrix.mul(rotation);
    }
    
    // Комбинированное вращение по осям XYZ (в градусах)
    public void applyRotation(float xDeg, float yDeg, float zDeg) {
        applyRotationX(xDeg);
        applyRotationY(yDeg);
        applyRotationZ(zDeg);
    }
    
    // Применение произвольной матрицы преобразования
    public void applyTransformation(Matrix4f matrix) {
        transformationMatrix = matrix.mul(transformationMatrix);
    }
    
    // Преобразование вектора
    public Vector3f transform(Vector3f vector) {
        return transformationMatrix.mul(vector);
    }
    
    // Преобразование нормали (использует специальную матрицу для нормалей)
    public Vector3f transformNormal(Vector3f normal) {
        Matrix3f normalMat = Matrix3f.normalMatrix(transformationMatrix);
        return normalMat.mul(normal);
    }
    
    // Получение текущей матрицы преобразования
    public Matrix4f getMatrix() {
        return new Matrix4f(transformationMatrix);
    }
    
    // Сброс к единичной матрице
    public void reset() {
        transformationMatrix = Matrix4f.identity();
    }
    
    // Комбинирование преобразований (умножение матриц)
    public void combine(Transformation other) {
        transformationMatrix = other.transformationMatrix.mul(transformationMatrix);
    }
    
    @Override
    public String toString() {
        return transformationMatrix.toString();
    }
}
