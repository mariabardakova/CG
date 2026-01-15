package com.cgvsu.removers.utils.functional;


import com.cgvsu.model.Polygon;

import java.util.List;

/**
 * Функциональный интерфейс для извлечения данных из полигона.
 */
@FunctionalInterface
public interface PolygonDataExtractor {
    /**
     * Извлекает список индексов из полигона.
     *
     * @param polygon Полигон для извлечения данных
     * @return Список индексов из полигона
     */
    List<Integer> extract(Polygon polygon);
}