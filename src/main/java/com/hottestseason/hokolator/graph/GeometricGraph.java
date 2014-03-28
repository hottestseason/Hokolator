package com.hottestseason.hokolator.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import com.hottestseason.geom.Point2D;

public interface GeometricGraph<V extends Point2D, E> {
    void add(V vertex);
    void connect(V source, V target, E edge);

    default void generateGrid(double width, double height, int row, int column, BiFunction<Integer, Point2D, V> vertexFactory, BiFunction<V, V, E> edgeFactory) {
        List<V> vertices = new ArrayList<>();
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                V vertex = vertexFactory.apply(row * i + j, new Point2D(width / column * j, height / row * i));
                add(vertex);
                vertices.add(vertex);
                if (i > 0) {
                    V anotherVertex = vertices.get(row * (i - 1) + j);
                    connect(vertex, anotherVertex, edgeFactory.apply(vertex, anotherVertex));
                    connect(anotherVertex, vertex, edgeFactory.apply(anotherVertex, vertex));
                }
                if (j > 0) {
                    V anotherVertex = vertices.get(row * i + j - 1);
                    connect(vertex, anotherVertex, edgeFactory.apply(vertex, anotherVertex));
                    connect(anotherVertex, vertex, edgeFactory.apply(anotherVertex, vertex));
                }
            }
        }
    }
}
