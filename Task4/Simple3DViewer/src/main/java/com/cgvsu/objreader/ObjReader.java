package com.cgvsu.objreader;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.util.ArrayList;
import java.util.Scanner;

public class ObjReader {

	private static final String OBJ_VERTEX_TOKEN = "v";
	private static final String OBJ_TEXTURE_TOKEN = "vt";
	private static final String OBJ_NORMAL_TOKEN = "vn";
	private static final String OBJ_FACE_TOKEN = "f";

	public static Model read(String fileContent) {
		Model result = new Model();
		ArrayList<Integer> polygonLine = new ArrayList<>(); // исправлено

		int lineInd = 0;
		Scanner scanner = new Scanner(fileContent);
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();

			int y = line.indexOf('#');
			if (line.indexOf('#') !=  -1){
				line = line.substring(0, y);
			}
			line = line.trim();
			if (line.isEmpty()){
				continue;
			}

			++lineInd;
			String[] tokens = line.split("\\s+");

			ArrayList<String> wordsInLine = new ArrayList<>();
			for (String token : tokens) {

				if (!token.isEmpty()) {
					wordsInLine.add(token);
				}
			}
			if (wordsInLine.isEmpty()) {
				continue;
			}

			final String token = wordsInLine.get(0);

			wordsInLine.remove(0);

			try {//исправлено


				switch (token) {
					// Для структур типа вершин методы написаны так, чтобы ничего не знать о внешней среде.
					// Они принимают только то, что им нужно для работы, а возвращают только то, что могут создать.
					// Исключение - индекс строки. Он прокидывается, чтобы выводить сообщение об ошибке.
					// Могло быть иначе. Например, метод parseVertex мог вместо возвращения вершины принимать вектор вершин
					// модели или сам класс модели, работать с ним.
					// Но такой подход может привести к большему количеству ошибок в коде. Например, в нем что-то может
					// тайно сделаться с классом модели.
					// А еще это портит читаемость
					// И не стоит забывать про тесты. Чем проще вам задать данные для теста, проверить, что метод рабочий,
					// тем лучше.
					case OBJ_VERTEX_TOKEN -> result.vertices.add(parseVertex(wordsInLine, lineInd));
					case OBJ_TEXTURE_TOKEN -> result.textureVertices.add(parseTextureVertex(wordsInLine, lineInd));
					case OBJ_NORMAL_TOKEN -> result.normals.add(parseNormal(wordsInLine, lineInd));
					case OBJ_FACE_TOKEN -> {
						Polygon p = parseFace(wordsInLine, lineInd, result.vertices.size(), result.textureVertices.size(), result.normals.size());
						result.polygons.add(p);
						polygonLine.add(lineInd);
					}
					default -> {
					}
				}
			}catch (ObjReaderException e){
				System.err.println("Error at line " + lineInd + ": " + e.getMessage());
			}
		}
		try {//исправлено
			validateModel(result, polygonLine);
		} catch (ObjReaderException e){
			throw e;
		}
//		for (int i = 0; i < result.polygons.size(); i++) {
//			validatePolygon(result, result.polygons.get(i), i);
//		}

		return result;
	}

	// Всем методам кроме основного я поставил модификатор доступа protected, чтобы обращаться к ним в тестах
	protected static Vector3f parseVertex(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {

		try {//исправлено
			float x = Float.parseFloat(wordsInLineWithoutToken.get(0));
			float y = Float.parseFloat(wordsInLineWithoutToken.get(1));
			float z = Float.parseFloat(wordsInLineWithoutToken.get(2));
			return new Vector3f(x, y, z);

		} catch(NumberFormatException e) {
			throw new ObjReaderException("Failed to parse float value in vertex coordinates", lineInd);

		} catch(IndexOutOfBoundsException e) {
			throw new ObjReaderException("Too few arguments for vertex definition.", lineInd);
		}
	}

	protected static Vector2f parseTextureVertex(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		try {
			return new Vector2f(
					Float.parseFloat(wordsInLineWithoutToken.get(0)),
					Float.parseFloat(wordsInLineWithoutToken.get(1)));

		} catch(NumberFormatException e) {
			throw new ObjReaderException("Failed to parse float value in texture coordinate.", lineInd);

		} catch(IndexOutOfBoundsException e) {
			throw new ObjReaderException("Too few arguments for texture vertex..", lineInd);
		}
	}

	protected static Vector3f parseNormal(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		try {
			return new Vector3f(
					Float.parseFloat(wordsInLineWithoutToken.get(0)),
					Float.parseFloat(wordsInLineWithoutToken.get(1)),
					Float.parseFloat(wordsInLineWithoutToken.get(2)));

		} catch(NumberFormatException e) {
			throw new ObjReaderException("Failed to parse float value in normal vector.", lineInd);

		} catch(IndexOutOfBoundsException e) {
			throw new ObjReaderException("Too few arguments for normal vector.", lineInd);
		}
	}

	protected static Polygon parseFace(final ArrayList<String> wordsInLineWithoutToken, int lineInd, int vertCnt, int tVertCnt, int normalsCnt) {
		ArrayList<Integer> onePolygonVertexIndices = new ArrayList<Integer>();
		ArrayList<Integer> onePolygonTextureVertexIndices = new ArrayList<Integer>();
		ArrayList<Integer> onePolygonNormalIndices = new ArrayList<Integer>();

		for (String s : wordsInLineWithoutToken) {
			parseFaceWord(s, onePolygonVertexIndices, onePolygonTextureVertexIndices, onePolygonNormalIndices, lineInd, vertCnt, tVertCnt, normalsCnt);
		}

		Polygon result = new Polygon();
		result.setVertexIndices(onePolygonVertexIndices);
		result.setTextureVertexIndices(onePolygonTextureVertexIndices);
		result.setNormalIndices(onePolygonNormalIndices);
		return result;
	}

	// Обратите внимание, что для чтения полигонов я выделил еще один вспомогательный метод.
	// Это бывает очень полезно и с точки зрения структурирования алгоритма в голове, и с точки зрения тестирования.
	// В радикальных случаях не бойтесь выносить в отдельные методы и тестировать код из одной-двух строчек.
	protected static void parseFaceWord(
			String wordInLine,
			ArrayList<Integer> onePolygonVertexIndices,
			ArrayList<Integer> onePolygonTextureVertexIndices,
			ArrayList<Integer> onePolygonNormalIndices,
			int lineInd,
			int vertCnt,
			int texVertCnt,
			int normalsCnt
	) {
		try {
			String[] wordIndices = wordInLine.split("/");
			switch (wordIndices.length) {
				case 1 -> {
					int vertexIndex = parseIndex(wordIndices[0], vertCnt, lineInd);
					onePolygonVertexIndices.add(vertexIndex);				}
				case 2 -> {
					int vertexIndex = parseIndex(wordIndices[0],vertCnt,  lineInd);
					int textureIndex = parseIndex(wordIndices[1], texVertCnt, lineInd);
					onePolygonVertexIndices.add(vertexIndex);
					onePolygonTextureVertexIndices.add(textureIndex);
				}
				case 3 -> {//изменено. Текстура и  нормали теперь не обязатеельны
					int vertexIndex = parseIndex(wordIndices[0], vertCnt, lineInd);
					onePolygonVertexIndices.add(vertexIndex);


					if (!wordIndices[1].isEmpty()) {
						if (texVertCnt == 0) {
							throw new ObjReaderException("Texture index used, but no 'vt' defined.", lineInd);
						}
						int textureIndex = parseIndex(wordIndices[1], texVertCnt, lineInd);
						onePolygonTextureVertexIndices.add(textureIndex);
					}

					if (!wordIndices[2].isEmpty()) {
						if (normalsCnt == 0) {
							throw new ObjReaderException("Normal index used, but no 'vn' defined.", lineInd);
						}
						int normalIndex = parseIndex(wordIndices[2], normalsCnt, lineInd);
						onePolygonNormalIndices.add(normalIndex);
					}
				}
				default -> {
					throw new ObjReaderException("Invalid face element.", lineInd);
				}
			}

		} catch(NumberFormatException e) {
			throw new ObjReaderException("Invalid integer in face definition.", lineInd);

		} catch(IndexOutOfBoundsException e) {
			throw new ObjReaderException("Too few arguments in face definition.", lineInd);
		}
	}

	protected static int parseIndex(String indexStr, int arrSize, int lineInd){
		try {
			int index =Integer.parseInt(indexStr);
			if(index < 0){
				if (arrSize == 0) {//справлено
					throw new ObjReaderException("Index 0 is invalid in OBJ format.", lineInd);
				}
				index = arrSize + index;
				if (index < 0){
					throw new ObjReaderException("Invalid index format." ,lineInd);
				}
			}else {
				index = index - 1;
			}

			if(index >= arrSize){
				throw new ObjReaderException("Invalid index.", lineInd);
			}
			if(index < 0){
				throw new ObjReaderException("Incorrect index.", lineInd);
			}
			return index;
		}catch (NumberFormatException e){
			throw new ObjReaderException("Failed to parse int value.", lineInd);
		}
	}
	private static void validateModel(Model model, ArrayList<Integer> polygonLineNumbers){ //test2
		if (model.vertices.isEmpty()){
			throw new ObjReaderException("Model has no vertices.", -1);
		}
		if(model.polygons.isEmpty()){
			throw new ObjReaderException("Model has no polygons.", -1);
		}
		for (int i = 0; i < model.polygons.size(); i++) {//исправлено
			Polygon polygon = model.polygons.get(i);
			int lineInd = polygonLineNumbers.get(i);
			validatePolygon(model, polygon, lineInd);
		}
	}
	private static void validatePolygon(Model model, Polygon polygon, int lineInd){
		if(polygon.getVertexIndices().size() < 3){
			throw new ObjReaderException("Polygon has less than 3 vertices.", lineInd);
		}
		for(int vertInd : polygon.getVertexIndices()){
			if (vertInd < 0 || vertInd >= model.vertices.size()){
				throw new ObjReaderException("Invalid vertex index.", lineInd);
			}
		}

		if (!polygon.getTextureVertexIndices().isEmpty()){
			if (polygon.getTextureVertexIndices().size() != polygon.getVertexIndices().size()){
				throw new ObjReaderException("Not all vertices have textures.", lineInd);
			}
		}

		for(int texInd : polygon.getTextureVertexIndices()){
			if (texInd < 0 || texInd >= model.textureVertices.size()){
				throw new ObjReaderException("Invalid texture index.", lineInd);
			}
		}

		if (!polygon.getNormalIndices().isEmpty()){
			if(polygon.getNormalIndices().size() != polygon.getVertexIndices().size()){
				throw  new ObjReaderException("The number of normals and vertices does not match.", lineInd);
			}
			for(int normalInd : polygon.getNormalIndices()){
				if (normalInd < 0 || normalInd >= model.normals.size()){
					throw new ObjReaderException("Invalid normals index.", lineInd);
				}
			}
		}


	}
}
