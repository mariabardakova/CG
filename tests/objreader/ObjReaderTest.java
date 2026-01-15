package objreader;

import com.cgvsu.math.Vector3f;
import com.cgvsu.objreader.ObjReaderException;
import com.cgvsu.objreader.ObjReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

class ObjReaderTest {

    @Test
    public void testParseVertex01() {
        final ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("1.01", "1.02", "1.03"));
        final Vector3f result = ObjReader.parseVertex(wordsInLineWithoutToken, 5);
        final Vector3f expectedResult = new Vector3f(1.01f, 1.02f, 1.03f);
        Assertions.assertTrue(result.equals(expectedResult));
    }

    @Test
    public void testParseVertex02() {
        final ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("ab", "o", "ba"));
        try {
            ObjReader.parseVertex(wordsInLineWithoutToken, 10);
            Assertions.fail();

        } catch (ObjReaderException exception) {
            String expectedError = "Error parsing OBJ file on line: 10. Failed to parse float value in vertex coordinates";
            Assertions.assertEquals(expectedError, exception.getMessage());
        }
    }
    @Test
    public void testParseVertex03() {
        final ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("1.0", "2.0"));
        try {
            ObjReader.parseVertex(wordsInLineWithoutToken, 10);
            Assertions.fail();

        } catch (ObjReaderException exception) {
            String expectedError = "Error parsing OBJ file on line: 10. Too few arguments for vertex definition.";
            Assertions.assertEquals(expectedError, exception.getMessage());
        }
    }
    @Test
    public void testParseFaceWord01() {
        ArrayList<Integer> v = new ArrayList<>();
        ArrayList<Integer> vt = new ArrayList<>();
        ArrayList<Integer> vn = new ArrayList<>();
        try {
            ObjReader.parseFaceWord("1/1/1", v, vt, vn, 5, 1, 0, 0);
            Assertions.fail();
        } catch (ObjReaderException e) {
            String expected = "Error parsing OBJ file on line: 5. Texture index used, but no 'vt' defined.";
            Assertions.assertEquals(expected, e.getMessage());
        }
    }
    @Test
    public void testParseFaceWord02() {
        ArrayList<Integer> v = new ArrayList<>();
        ArrayList<Integer> vt = new ArrayList<>();
        ArrayList<Integer> vn = new ArrayList<>();
        try {
            ObjReader.parseFaceWord("1//1", v, vt, vn, 7, 1, 1, 0);
            Assertions.fail();
        } catch (ObjReaderException e) {
            String expected = "Error parsing OBJ file on line: 7. Normal index used, but no 'vn' defined.";
            Assertions.assertEquals(expected, e.getMessage());
        }
    }
    @Test
    public void testParseIndex01() {
        try {
            ObjReader.parseIndex("-1", 0, 12);
            Assertions.fail();
        } catch (ObjReaderException e) {
            String expected = "Error parsing OBJ file on line: 12. Index 0 is invalid in OBJ format.";
            Assertions.assertEquals(expected, e.getMessage());
        }
    }
    @Test
    public void testRead01() {
        String objContent = "";
        try {
            ObjReader.read(objContent);
            Assertions.fail();
        } catch (ObjReaderException e) {
            String expected = "Error parsing OBJ file on line: -1. Model has no vertices.";
            Assertions.assertEquals(expected, e.getMessage());
        }
    }
}


