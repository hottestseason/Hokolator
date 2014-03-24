package com.hottestseason.hokolator.graph;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

class Weight implements Weighted {
    private final double value;

    public Weight(double value) {
        this.value = value;
    }

    @Override
    public double getWeight() {
        return value;
    }
}


public class DijkstraTest {
    @Test
    // http://www.deqnotes.net/acmicpc/dijkstra/
    public void test() {
        Graph<Integer, Weight> graph = new Graph<>();
        graph.add(1);
        graph.add(2);
        graph.add(3);
        graph.add(4);
        graph.add(5);
        graph.add(6);
        graph.connectBidrectionally(1, 2, new Weight(5), new Weight(5));
        graph.connectBidrectionally(1, 3, new Weight(4), new Weight(4));
        graph.connectBidrectionally(1, 4, new Weight(2), new Weight(2));
        graph.connectBidrectionally(2, 3, new Weight(5), new Weight(5));
        graph.connectBidrectionally(2, 5, new Weight(6), new Weight(6));
        graph.connectBidrectionally(3, 4, new Weight(3), new Weight(3));
        graph.connectBidrectionally(3, 6, new Weight(2), new Weight(2));
        graph.connectBidrectionally(4, 6, new Weight(6), new Weight(6));
        graph.connectBidrectionally(5, 6, new Weight(4), new Weight(4));

        List<Weight> path = new Dijkstra<>(graph, 1, 5).call();
        assertEquals(path.size(), 3);
        assertEquals(graph.getTargetOf(path.get(0)).intValue(), 3);
        assertEquals(graph.getTargetOf(path.get(1)).intValue(), 6);
        assertEquals(graph.getTargetOf(path.get(2)).intValue(), 5);
    }

    @Test
    public void testIfNoPathFound() {
        Graph<Integer, Weight> graph = new Graph<>();
        graph.add(1);
        graph.add(2);
        graph.connect(1, 2, new Weight(1));
        assertEquals(new Dijkstra<>(graph, 2, 1).call().size(), 0);
    }

    @Test
    public void testIfStartAndGoalAreSame() {
        Graph<Integer, Weight> graph = new Graph<>();
        graph.add(1);
        graph.add(2);
        graph.connect(1, 2, new Weight(1));
        assertEquals(new Dijkstra<>(graph, 1, 1).call().size(), 0);
    }
}