package objwritter;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import com.cgvsu.objwriter.ObjWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class ObjWriterTest {

    private Model validModel;

    @BeforeEach
    void setUp() {
        validModel = createValidModel();
    }

    private Model createValidModel() {
        Model model = new Model();

        model.getVertices().add(new Vector3f(1.0f, 2.0f, 3.0f));
        model.getVertices().add(new Vector3f(4.5f, 5.5f, 6.5f));
        model.getVertices().add(new Vector3f(7.0f, 8.0f, 9.0f));

        model.getTextureVertices().add(new Vector2f(0.1f, 0.2f));
        model.getTextureVertices().add(new Vector2f(0.3f, 0.4f));

        model.getNormals().add(new Vector3f(0.0f, 0.0f, 1.0f));
        model.getNormals().add(new Vector3f(0.0f, 1.0f, 0.0f));

        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        polygon.setTextureVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 0)));
        polygon.setNormalIndices(new ArrayList<>(Arrays.asList(0, 1, 0)));
        model.getPolygons().add(polygon);

        return model;
    }

    // Тесты записи файлов.
    @Test
    void testWrite_ValidModel_Success(@TempDir Path tempDir) throws IOException {
        Path outputPath = tempDir.resolve("test.obj");

        assertDoesNotThrow(() -> ObjWriter.write(validModel, outputPath));
        assertTrue(Files.exists(outputPath));

        String content = Files.readString(outputPath);
        assertTrue(content.contains("v 1 2 3"));
        assertTrue(content.contains("v 4.5 5.5 6.5"));
        assertTrue(content.contains("vt 0.1 0.2"));
        assertTrue(content.contains("vn 0 0 1"));
        assertTrue(content.contains("f 1/1/1 2/2/2 3/1/1"));
    }

    @Test
    void testWrite_ValidModelWithStringPath_Success(@TempDir Path tempDir) throws IOException {
        Path outputPath = tempDir.resolve("test_string.obj");

        assertDoesNotThrow(() -> ObjWriter.write(validModel, outputPath.toString()));
        assertTrue(Files.exists(outputPath));
    }

    @Test
    void testWrite_NullModel_ThrowsException(@TempDir Path tempDir) {
        Path outputPath = tempDir.resolve("test.obj");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ObjWriter.write(null, outputPath)
        );
        assertEquals("Model cannot be null", exception.getMessage());
    }

    @Test
    void testWrite_NullPath_ThrowsException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ObjWriter.write(validModel, (Path) null)
        );
        assertEquals("Output path cannot be null", exception.getMessage());
    }

    // Тесты преобразования в строку.
    @Test
    void testModelToString_ValidModel_ReturnsCorrectFormat() {
        String result = ObjWriter.modelToString(validModel);

        assertNotNull(result);
        assertTrue(result.contains("v "), "Should contain vertices");
        assertTrue(result.contains("vt "), "Should contain texture vertices");
        assertTrue(result.contains("vn "), "Should contain normals");
        assertTrue(result.contains("f "), "Should contain faces");
        assertTrue(result.contains("/"), "Faces should contain indices separators");
    }

    @Test
    void testModelToString_WithComment_IncludesComment() {
        String result = ObjWriter.modelToString(validModel, "Test Comment");

        assertTrue(result.startsWith("# Test Comment"));
        assertTrue(result.contains("v "));
    }

    @Test
    void testModelToString_WithNullComment_NoCommentInOutput() {
        String result = ObjWriter.modelToString(validModel, null);

        assertFalse(result.startsWith("#"));
        assertTrue(result.contains("v "));
    }

    @Test
    void testModelToString_DefaultComment_IncludesDefaultComment() {
        String result = ObjWriter.modelToString(validModel);

        assertTrue(result.contains("# Exported by Team CG&Geom"));
    }

    // Тесты различных комбинаций данных.
    @Test
    void testModelToString_ModelWithOnlyVertices_CorrectFormat() {
        Model model = new Model();
        model.getVertices().add(new Vector3f(1.0f, 2.0f, 3.0f));
        model.getVertices().add(new Vector3f(4.0f, 5.0f, 6.0f));
        model.getVertices().add(new Vector3f(7.0f, 8.0f, 9.0f));

        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        model.getPolygons().add(polygon);

        String result = ObjWriter.modelToString(model);

        assertTrue(result.contains("v 1 2 3"));
        assertTrue(result.contains("v 4 5 6"));
        assertFalse(result.contains("vt"));
        assertFalse(result.contains("vn"));
        assertTrue(result.contains("f 1 2 3"));
    }

    @Test
    void testModelToString_ModelWithoutTextureVertices_CorrectFormat() {
        Model model = new Model();
        model.getVertices().add(new Vector3f(1.0f, 2.0f, 3.0f));
        model.getVertices().add(new Vector3f(4.0f, 5.0f, 6.0f));
        model.getVertices().add(new Vector3f(7.0f, 8.0f, 9.0f));

        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));

        model.getPolygons().add(polygon);

        String result = ObjWriter.modelToString(model);

        assertFalse(result.contains("vt"), "Should not contain texture vertices");
        assertTrue(result.contains("f 1 2 3"), "Faces should contain only vertex indices");
    }

    @Test
    void testModelToString_ModelWithoutNormals_CorrectFormat() {
        Model model = new Model();
        model.getVertices().add(new Vector3f(1.0f, 2.0f, 3.0f));
        model.getVertices().add(new Vector3f(4.0f, 5.0f, 6.0f));
        model.getVertices().add(new Vector3f(7.0f, 8.0f, 9.0f));

        model.getTextureVertices().add(new Vector2f(0.1f, 0.2f));
        model.getTextureVertices().add(new Vector2f(0.3f, 0.4f));

        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        polygon.setTextureVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 0)));

        model.getPolygons().add(polygon);

        String result = ObjWriter.modelToString(model);

        assertFalse(result.contains("vn"), "Should not contain normals");
        assertTrue(result.contains("f 1/1 2/2 3/1"), "Faces should contain vertex and texture indices");
    }

    // Тесты форматирования чисел.
    @Test
    void testModelToString_FloatFormatting_RemovesTrailingZeros() {
        Model model = new Model();
        model.getVertices().add(new Vector3f(1.0f, 2.500000f, 3.120000f));
        model.getVertices().add(new Vector3f(0.0f, -0.000001f, 1000000.0f));

        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 0)));
        model.getPolygons().add(polygon);

        String result = ObjWriter.modelToString(model);

        assertTrue(result.contains("1 2.5"), "Should remove trailing zeros from 2.500000");
        assertTrue(result.contains("3.12"), "Should remove trailing zeros from 3.120000");
        assertTrue(result.contains("-0.000001"), "Should preserve small values");
    }

    @Test
    void testModelToString_WithZeroValues_CorrectFormat() {
        Model model = new Model();
        model.getVertices().add(new Vector3f(0.0f, 0.0f, 0.0f));
        model.getVertices().add(new Vector3f(-0.0f, 0.0f, 0.0f));

        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 0)));
        model.getPolygons().add(polygon);

        String result = ObjWriter.modelToString(model);

        assertTrue(result.contains("v 0 0 0"));
        assertTrue(result.contains("v 0 0 0"));
    }

    // Тесты специфических форматов полигонов.
    @Test
    void testModelToString_PolygonWithVertexAndTextureIndices_CorrectFormat() {
        Model model = new Model();
        model.getVertices().add(new Vector3f(1.0f, 2.0f, 3.0f));
        model.getTextureVertices().add(new Vector2f(0.1f, 0.2f));

        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(Arrays.asList(0, 0, 0)));
        polygon.setTextureVertexIndices(new ArrayList<>(Arrays.asList(0, 0, 0)));
        model.getPolygons().add(polygon);

        String result = ObjWriter.modelToString(model);

        assertTrue(result.contains("f 1/1 1/1 1/1"));
    }

    @Test
    void testModelToString_PolygonWithVertexAndNormalIndices_CorrectFormat() {
        Model model = new Model();
        model.getVertices().add(new Vector3f(1.0f, 2.0f, 3.0f));
        model.getNormals().add(new Vector3f(0.0f, 0.0f, 1.0f));

        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(Arrays.asList(0, 0, 0)));
        polygon.setNormalIndices(new ArrayList<>(Arrays.asList(0, 0, 0)));
        model.getPolygons().add(polygon);

        String result = ObjWriter.modelToString(model);

        assertTrue(result.contains("f 1//1 1//1 1//1"));
    }

    @Test
    void testModelToString_EmptyModel_ValidOutput() {
        Model emptyModel = new Model();
        emptyModel.getVertices().add(new Vector3f(0.0f, 0.0f, 0.0f));

        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(Arrays.asList(0, 0, 0)));
        emptyModel.getPolygons().add(polygon);

        String result = ObjWriter.modelToString(emptyModel, "Empty Model");

        assertTrue(result.contains("# Empty Model"));
        assertTrue(result.contains("v 0 0 0"));
        assertTrue(result.contains("f 1 1 1"));
    }
}