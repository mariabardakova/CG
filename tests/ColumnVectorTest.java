import com.cgvsu.math.ColumnVector;
import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Transformation;
import com.cgvsu.math.Vector3f;

/**
 * Тестовый класс для проверки работы с векторами-столбцами.
 */
public class ColumnVectorTest {
    
    public static void main(String[] args) {
        System.out.println("=== Тестирование векторов-столбцов ===\n");
        
        // Тест 1: Создание векторов
        ColumnVector vec1 = new ColumnVector(1.0f, 2.0f, 3.0f, 4.0f);
        System.out.println("Вектор 1: " + vec1);
        
        Vector3f v3f = new Vector3f(5.0f, 6.0f, 7.0f);
        ColumnVector vec2 = new ColumnVector(v3f);
        System.out.println("Вектор 2 (из Vector3f): " + vec2);
        
        // Тест 2: Матрица преобразований
        Matrix4f translation = Matrix4f.translation(10.0f, 20.0f, 30.0f);
        System.out.println("\nМатрица переноса:");
        System.out.println(translation);
        
        // Тест 3: Умножение матрицы на вектор
        ColumnVector result = translation.mul(vec2);
        System.out.println("Результат умножения: " + result);
        System.out.println("В виде Vector3f: " + result.toVector3f());
        
        // Тест 4: Комбинирование преобразований
        Transformation transform = new Transformation();
        transform.applyScaling(2.0f, 2.0f, 2.0f);
        transform.applyRotationY(90.0f);
        transform.applyTranslation(5.0f, 0.0f, 0.0f);
        
        System.out.println("\nМатрица преобразования:");
        System.out.println(transform);
        
        Vector3f testPoint = new Vector3f(1.0f, 0.0f, 0.0f);
        Vector3f transformedPoint = transform.transform(testPoint);
        System.out.println("Точка " + testPoint + " после преобразования: " + transformedPoint);
        
        // Тест 5: Проверка порядка преобразований
        System.out.println("\n=== Проверка порядка преобразований ===");
        
        Transformation orderTest = new Transformation();
        System.out.println("Начальная матрица:");
        System.out.println(orderTest.getMatrix());
        
        orderTest.applyTranslation(2.0f, 0.0f, 0.0f);
        System.out.println("После переноса на (2,0,0):");
        System.out.println(orderTest.getMatrix());
        
        orderTest.applyRotationZ(45.0f);
        System.out.println("После вращения на 45 градусов вокруг Z:");
        System.out.println(orderTest.getMatrix());
        
        orderTest.applyScaling(3.0f, 1.0f, 1.0f);
        System.out.println("После масштабирования в 3 раза по X:");
        System.out.println(orderTest.getMatrix());
        
        Vector3f testVec = new Vector3f(1.0f, 0.0f, 0.0f);
        Vector3f resultVec = orderTest.transform(testVec);
        System.out.println("\nВектор " + testVec + " после всех преобразований: " + resultVec);
        
        System.out.println("\n=== Тестирование завершено ===");
    }
}
