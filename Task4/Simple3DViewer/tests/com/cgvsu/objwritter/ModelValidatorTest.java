package com.cgvsu.objwritter;


import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import com.cgvsu.objwriter.ModelValidator;
import com.cgvsu.objwriter.ObjWriterException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class ModelValidatorTest {

    private Model validModel;

    @BeforeEach
    void setUp() {
        validModel = createValidModel();
    }

    private Model createValidModel() {
        Model model = new Model();

        model.getVertices().add(new Vector3f(1.0f, 2.0f, 3.0f));
        model.getVertices().add(new Vector3f(4.0f, 5.0f, 6.0f));
        model.getVertices().add(new Vector3f(7.0f, 8.0f, 9.0f));

        model.getTextureVertices().add(new Vector2f(0.1f, 0.2f));
        model.getTextureVertices().add(new Vector2f(0.3f, 0.4f));
        model.getTextureVertices().add(new Vector2f(0.5f, 0.6f));

        model.getNormals().add(new Vector3f(0.0f, 0.0f, 1.0f));
        model.getNormals().add(new Vector3f(0.0f, 1.0f, 0.0f));
        model.getNormals().add(new Vector3f(1.0f, 0.0f, 0.0f));

        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        polygon.setTextureVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        polygon.setNormalIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        model.getPolygons().add(polygon);

        return model;
    }

    // NULL и пустые данные.
    @Test
    void testValidate_NullModel_ThrowsException() {
        ObjWriterException exception = assertThrows(
                ObjWriterException.class,
                () -> ModelValidator.validate(null)
        );
        assertEquals("Model cannot be null", exception.getMessage());
    }

    @Test
    void testValidate_EmptyVertices_ThrowsException() {
        Model model = new Model();

        ObjWriterException exception = assertThrows(
                ObjWriterException.class,
                () -> ModelValidator.validate(model)
        );
        assertEquals("Model must have at least one vertex", exception.getMessage());
    }

    @Test
    void testValidate_EmptyPolygons_ThrowsException() {
        Model model = new Model();
        model.getVertices().add(new Vector3f(1, 1, 1));

        ObjWriterException exception = assertThrows(
                ObjWriterException.class,
                () -> ModelValidator.validate(model)
        );
        assertEquals("Model must have at least one polygon", exception.getMessage());
    }

    // Некорректные числовые значения.
    @Test
    void testValidate_VertexWithNaN_ThrowsException() {
        validModel.getVertices().set(0, new Vector3f(Float.NaN, 2.0f, 3.0f));

        ObjWriterException exception = assertThrows(
                ObjWriterException.class,
                () -> ModelValidator.validate(validModel)
        );
        assertEquals("Vertex 0 contains NaN values", exception.getMessage());
    }

    @Test
    void testValidate_VertexWithInfinite_ThrowsException() {
        validModel.getVertices().set(0, new Vector3f(Float.POSITIVE_INFINITY, 2.0f, 3.0f));

        ObjWriterException exception = assertThrows(
                ObjWriterException.class,
                () -> ModelValidator.validate(validModel)
        );
        assertEquals("Vertex 0 contains infinite values", exception.getMessage());
    }

    @Test
    void testValidate_TextureVertexWithNaN_ThrowsException() {
        validModel.getTextureVertices().set(0, new Vector2f(Float.NaN, 0.2f));

        ObjWriterException exception = assertThrows(
                ObjWriterException.class,
                () -> ModelValidator.validate(validModel)
        );
        assertEquals("Texture vertex 0 contains NaN values", exception.getMessage());
    }

    @Test
    void testValidate_NormalWithInfinite_ThrowsException() {
        validModel.getNormals().set(0, new Vector3f(0.0f, Float.NEGATIVE_INFINITY, 1.0f));

        ObjWriterException exception = assertThrows(
                ObjWriterException.class,
                () -> ModelValidator.validate(validModel)
        );
        assertEquals("Normal 0 contains infinite values", exception.getMessage());
    }

    // Некорректные индексы в полигонах.
    @Test
    void testValidate_PolygonWithInvalidVertexIndex_ThrowsException() {
        Model model = new Model();
        model.getVertices().add(new Vector3f(1, 1, 1));
        model.getVertices().add(new Vector3f(2, 2, 2));

        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        model.getPolygons().add(polygon);

        ObjWriterException exception = assertThrows(
                ObjWriterException.class,
                () -> ModelValidator.validate(model)
        );
        assertTrue(exception.getMessage().contains("invalid vertex index 2"));
    }

    @Test
    void testValidate_PolygonWithInvalidTextureIndex_ThrowsException() {
        Model model = new Model();
        model.getVertices().add(new Vector3f(1, 1, 1));
        model.getVertices().add(new Vector3f(2, 2, 2));
        model.getVertices().add(new Vector3f(3, 3, 3));

        model.getTextureVertices().add(new Vector2f(0.1f, 0.1f));
        model.getTextureVertices().add(new Vector2f(0.2f, 0.2f));

        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        polygon.setTextureVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        model.getPolygons().add(polygon);

        ObjWriterException exception = assertThrows(
                ObjWriterException.class,
                () -> ModelValidator.validate(model)
        );
        assertTrue(exception.getMessage().contains("invalid texture index 2"));
    }

    @Test
    void testValidate_PolygonWithInvalidNormalIndex_ThrowsException() {
        Model model = new Model();
        model.getVertices().add(new Vector3f(1, 1, 1));
        model.getVertices().add(new Vector3f(2, 2, 2));
        model.getVertices().add(new Vector3f(3, 3, 3));

        model.getNormals().add(new Vector3f(0.0f, 0.0f, 1.0f));
        model.getNormals().add(new Vector3f(0.0f, 1.0f, 0.0f));

        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        polygon.setNormalIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        model.getPolygons().add(polygon);

        ObjWriterException exception = assertThrows(
                ObjWriterException.class,
                () -> ModelValidator.validate(model)
        );
        assertTrue(exception.getMessage().contains("invalid normal index 2"));
    }

    // Несоответствие количества индексов.
    @Test
    void testValidate_PolygonWithDifferentTextureIndexCount_ThrowsException() {
        Model model = new Model();
        model.getVertices().add(new Vector3f(1, 1, 1));
        model.getVertices().add(new Vector3f(2, 2, 2));
        model.getVertices().add(new Vector3f(3, 3, 3));

        model.getTextureVertices().add(new Vector2f(0.1f, 0.1f));
        model.getTextureVertices().add(new Vector2f(0.2f, 0.2f));
        model.getTextureVertices().add(new Vector2f(0.3f, 0.3f));

        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));

        try {
            java.lang.reflect.Field textureIndicesField = Polygon.class.getDeclaredField("textureVertexIndices");
            textureIndicesField.setAccessible(true);
            textureIndicesField.set(polygon, new ArrayList<>(Arrays.asList(0, 1)));
        } catch (Exception e) {
            fail("Failed to set texture indices via reflection: " + e.getMessage());
        }

        model.getPolygons().add(polygon);

        ObjWriterException exception = assertThrows(
                ObjWriterException.class,
                () -> ModelValidator.validate(model)
        );
        assertTrue(exception.getMessage().contains("vertex and texture index counts differ"));
    }

    @Test
    void testValidate_PolygonWithDifferentNormalIndexCount_ThrowsException() {
        Model model = new Model();
        model.getVertices().add(new Vector3f(1, 1, 1));
        model.getVertices().add(new Vector3f(2, 2, 2));
        model.getVertices().add(new Vector3f(3, 3, 3));

        model.getNormals().add(new Vector3f(0.0f, 0.0f, 1.0f));
        model.getNormals().add(new Vector3f(0.0f, 1.0f, 0.0f));
        model.getNormals().add(new Vector3f(1.0f, 0.0f, 0.0f));

        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));

        try {
            java.lang.reflect.Field normalIndicesField = Polygon.class.getDeclaredField("normalIndices");
            normalIndicesField.setAccessible(true);
            normalIndicesField.set(polygon, new ArrayList<>(Arrays.asList(0, 1)));
        } catch (Exception e) {
            fail("Failed to set normal indices via reflection: " + e.getMessage());
        }

        model.getPolygons().add(polygon);

        ObjWriterException exception = assertThrows(
                ObjWriterException.class,
                () -> ModelValidator.validate(model)
        );
        assertTrue(exception.getMessage().contains("vertex and normal index counts differ"));
    }

    // Null элементы в списках.
    @Test
    void testValidate_NullVertexInList_ThrowsException() {
        validModel.getVertices().set(1, null);

        ObjWriterException exception = assertThrows(
                ObjWriterException.class,
                () -> ModelValidator.validate(validModel)
        );
        assertEquals("Vertex at index 1 is null", exception.getMessage());
    }

    @Test
    void testValidate_NullTextureVertexInList_ThrowsException() {
        validModel.getTextureVertices().set(0, null);

        ObjWriterException exception = assertThrows(
                ObjWriterException.class,
                () -> ModelValidator.validate(validModel)
        );
        assertEquals("Texture vertex at index 0 is null", exception.getMessage());
    }

    @Test
    void testValidate_NullNormalInList_ThrowsException() {
        validModel.getNormals().set(0, null);

        ObjWriterException exception = assertThrows(
                ObjWriterException.class,
                () -> ModelValidator.validate(validModel)
        );
        assertEquals("Normal at index 0 is null", exception.getMessage());
    }

    @Test
    void testValidate_NullPolygonInList_ThrowsException() {
        validModel.getPolygons().add(null);

        ObjWriterException exception = assertThrows(
                ObjWriterException.class,
                () -> ModelValidator.validate(validModel)
        );
        assertEquals("Polygon at index 1 is null", exception.getMessage());
    }

    // Позитивные тесты.
    @Test
    void testValidate_ValidModel_NoException() {
        assertDoesNotThrow(() -> ModelValidator.validate(validModel));
    }

    @Test
    void testValidate_ModelWithoutTextureVertices_NoException() {
        Model model = new Model();
        model.getVertices().add(new Vector3f(1, 1, 1));
        model.getVertices().add(new Vector3f(2, 2, 2));
        model.getVertices().add(new Vector3f(3, 3, 3));

        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        model.getPolygons().add(polygon);

        assertDoesNotThrow(() -> ModelValidator.validate(model));
    }

    @Test
    void testValidate_ModelWithoutNormals_NoException() {
        Model model = new Model();
        model.getVertices().add(new Vector3f(1, 1, 1));
        model.getVertices().add(new Vector3f(2, 2, 2));
        model.getVertices().add(new Vector3f(3, 3, 3));

        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        model.getPolygons().add(polygon);

        assertDoesNotThrow(() -> ModelValidator.validate(model));
    }

    @Test
    void testValidate_ModelWithTextureVerticesButPolygonWithoutThem_NoException() {
        Model model = new Model();
        model.getVertices().add(new Vector3f(1, 1, 1));
        model.getVertices().add(new Vector3f(2, 2, 2));
        model.getVertices().add(new Vector3f(3, 3, 3));

        model.getTextureVertices().add(new Vector2f(0.1f, 0.1f));

        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        model.getPolygons().add(polygon);

        assertDoesNotThrow(() -> ModelValidator.validate(model));
    }

    @Test
    void testValidate_ModelWithNormalsButPolygonWithoutThem_NoException() {
        Model model = new Model();
        model.getVertices().add(new Vector3f(1, 1, 1));
        model.getVertices().add(new Vector3f(2, 2, 2));
        model.getVertices().add(new Vector3f(3, 3, 3));

        model.getNormals().add(new Vector3f(0, 0, 1));

        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        model.getPolygons().add(polygon);

        assertDoesNotThrow(() -> ModelValidator.validate(model));
    }
}