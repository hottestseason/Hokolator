package com.hottestseason.hokolator.graph;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.hottestseason.hokolator.Map;
import com.hottestseason.hokolator.Map.Street;

public class GeometricGraphTest {
    @Test
    public void testGenerateGrid1() {
        Map map = new Map();
        map.generateGrid(1, 1, 1, 2, (index, point) -> map.new Intersection(index, point), (vertex, anotherVertex) -> map.new Street(1));
        assertEquals(map.getIntersections().size(), 2);
        assertEquals(map.getStreets().size(), 2);
        List<Street> streets = map.findShortestPathBetween(map.getIntersections().get(0), map.getIntersections().get(1));
        assertEquals(streets.size(), 1);
        assertEquals(streets.get(0).getSource().id, 0);
        assertEquals(streets.get(0).getTarget().id, 1);
    }

    @Test
    public void testGenerateGrid2() {
        Map map = new Map();
        map.generateGrid(1, 1, 2, 3, (index, point) -> map.new Intersection(index, point), (vertex, anotherVertex) -> map.new Street(1));
        assertEquals(map.getIntersections().size(), 6);
        assertEquals(map.getStreets().size(), 14);
        List<Street> streets = map.findShortestPathBetween(map.getIntersections().get(0), map.getIntersections().get(5));
        assertEquals(streets.size(), 3);
    }
}
