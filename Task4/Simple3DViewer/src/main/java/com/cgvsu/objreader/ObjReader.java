package com.cgvsu.objreader;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class ObjReader {

	private static final String OBJ_VERTEX_TOKEN = "v";
	private static final String OBJ_TEXTURE_TOKEN = "vt";
	private static final String OBJ_NORMAL_TOKEN = "vn";
	private static final String OBJ_FACE_TOKEN = "f";

	public static Model read(String fileContent) {
		Model result = new Model();

		int lineInd = 0;
		Scanner scanner = new Scanner(fileContent);
		while (scanner.hasNextLine()) {
			final String line = scanner.nextLine();
			ArrayList<String> wordsInLine = new ArrayList<String>(Arrays.asList(line.split("\\s+")));
			if (wordsInLine.isEmpty()) {
				continue;
			}

			final String token = wordsInLine.get(0);
			wordsInLine.remove(0);

			++lineInd;
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
				case OBJ_FACE_TOKEN -> result.polygons.add(parseFace(wordsInLine, lineInd, result.vertices.size(),
						result.textureVertices.size(), result.normals.size()));
				default -> {}
			}
		}
		validateModel(result);
//		for (int i = 0; i < result.polygons.size(); i++) {
//			validatePolyqon(result, result.polygons.get(i), i);
//		}

		return result;
	}

	// Всем методам кроме основного я поставил модификатор доступа protected, чтобы обращаться к ним в тестах
	protected static Vector3f parseVertex(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {

		if (wordsInLineWithoutToken.size() > 3) {
			throw new ObjReaderException("Too many vertex arguments.", lineInd);
		}
		try {
			return new Vector3f(
					Float.parseFloat(wordsInLineWithoutToken.get(0)),
					Float.parseFloat(wordsInLineWithoutToken.get(1)),
					Float.parseFloat(wordsInLineWithoutToken.get(2)));

		} catch(NumberFormatException e) {
			throw new ObjReaderException("Failed to parse float value.", lineInd);

		} catch(IndexOutOfBoundsException e) {
			throw new ObjReaderException("Too few vertex arguments.", lineInd);
		}
	}

	protected static Vector2f parseTextureVertex(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		try {
			return new Vector2f(
					Float.parseFloat(wordsInLineWithoutToken.get(0)),
					Float.parseFloat(wordsInLineWithoutToken.get(1)));

		} catch(NumberFormatException e) {
			throw new ObjReaderException("Failed to parse float value.", lineInd);

		} catch(IndexOutOfBoundsException e) {
			throw new ObjReaderException("Too few texture vertex arguments.", lineInd);
		}
	}

	protected static Vector3f parseNormal(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		try {
			return new Vector3f(
					Float.parseFloat(wordsInLineWithoutToken.get(0)),
					Float.parseFloat(wordsInLineWithoutToken.get(1)),
					Float.parseFloat(wordsInLineWithoutToken.get(2)));

		} catch(NumberFormatException e) {
			throw new ObjReaderException("Failed to parse float value.", lineInd);

		} catch(IndexOutOfBoundsException e) {
			throw new ObjReaderException("Too few normal arguments.", lineInd);
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
				case 3 -> {
					int vertexIndex = parseIndex(wordIndices[0], vertCnt, lineInd);
					onePolygonVertexIndices.add(vertexIndex);
					int normalIndex = parseIndex(wordIndices[2], normalsCnt, lineInd);
					onePolygonNormalIndices.add(normalIndex);
					if (!wordIndices[1].equals("")) {
						int textureIndex = parseIndex(wordIndices[1], texVertCnt, lineInd);
						onePolygonTextureVertexIndices.add(textureIndex);
					}
				}
				default -> {
					throw new ObjReaderException("Invalid element size.", lineInd);
				}
			}

		} catch(NumberFormatException e) {
			throw new ObjReaderException("Failed to parse int value.", lineInd);

		} catch(IndexOutOfBoundsException e) {
			throw new ObjReaderException("Too few arguments.", lineInd);
		}
	}

	private static int parseIndex(String indexStr, int arrSize, int lineInd){ //test1.obj
		try {
			int index =Integer.parseInt(indexStr);
			if(index < 0){
				index = arrSize + index;
				if (index < 0){
					throw new ObjReaderException("Incorrect index." ,lineInd);
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
	private static void validateModel(Model model){ //test2
		if (model.vertices.isEmpty()){
			throw new ObjReaderException("Model has no vertices.", -1);
		}
		if(model.polygons.isEmpty()){
			throw new ObjReaderException("Model has no polygons.", -1);
		}
		for(int i =0; i < model.polygons.size(); i ++){
			validatePolygon(model, model.polygons.get(i), i);
		}
	}
	private static void validatePolygon(Model model, Polygon polygon, int index){
		if(polygon.getVertexIndices().size() < 3){
			throw new ObjReaderException("Polygon has less than 3 vertices.", index);//test 3
		}
		for(int vertInd : polygon.getVertexIndices()){
			if (vertInd < 0 || vertInd >= model.vertices.size()){
				throw new ObjReaderException("Invalid vertex index.", index);//4
			}
		}

		if (!polygon.getTextureVertexIndices().isEmpty()){
			if (polygon.getTextureVertexIndices().size() != polygon.getVertexIndices().size()){
				throw new ObjReaderException("Not all vertices have textures.", index);
			}
		}

		for(int texInd : polygon.getTextureVertexIndices()){
			if (texInd < 0 || texInd >= model.textureVertices.size()){
				throw new ObjReaderException("Invalid texture index.", index);
			}
		}

		if (!polygon.getNormalIndices().isEmpty()){
			if(polygon.getNormalIndices().size() != polygon.getVertexIndices().size()){
				throw  new ObjReaderException("The number of normals and vertices does not match.", index);
			}
			for(int normalInd : polygon.getNormalIndices()){
				if (normalInd < 0 || normalInd >= model.normals.size()){
					throw new ObjReaderException("Invalid normals index.", index);
				}
			}
		}


	}
}
