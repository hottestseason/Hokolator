package com.hottestseason.hokolator.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;

public class Dijkstra<V, E extends Weighted> implements Callable<List<E>> {
    private final Graph<V, E> graph;
    private final V start, end;
    private final Map<V, E> previouseEdgeMap = new HashMap<>();

    public Dijkstra(Graph<V, E> graph, V start, V end) {
        this.graph = graph;
        this.start = start; this.end = end;
    }

    @Override
    public List<E> call() {
        Map<V, Double> costs = new HashMap<>();
        PriorityQueue<V> priorityQueue = new PriorityQueue<>((vertex1, vertex2) -> Double.compare(costs.get(vertex1), costs.get(vertex2)));

        costs.put(start, 0.0);
        priorityQueue.add(start);

        while (!priorityQueue.isEmpty()) {
            V done = priorityQueue.poll();
            if (done == end) return computePath();
            for (E edge : graph.getEdgesOf(done)) {
                V to = graph.getTargetOf(edge);
                double newCost = costs.get(done) + edge.getWeight();
                if (costs.get(to) == null || newCost < costs.get(to)) {
                    priorityQueue.remove(to);
                    costs.put(to, newCost);
                    previouseEdgeMap.put(to, edge);
                    priorityQueue.add(to);
                }
            }
        }

        return new ArrayList<>();
    }

    private List<E> computePath() {
        List<E> path = new ArrayList<>();
        V current = end;
        while (current != start) {
            E edge = previouseEdgeMap.get(current);
            path.add(edge);
            current = graph.getSourceOf(edge);
        }
        Collections.reverse(path);
        return path;
    }
}