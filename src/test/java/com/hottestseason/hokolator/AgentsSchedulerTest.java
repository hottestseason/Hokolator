package com.hottestseason.hokolator;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.hottestseason.hokolator.Map.Intersection;

class MockPedestrian extends Pedestrian {
    public MockPedestrian(int id, Intersection goal) {
        super(id, goal);
    }

    @Override
    public double calcSpeed() {
        return id + 1;
    }
}

public class AgentsSchedulerTest {
    @Test
    public void testUpdateWithThreadPool() throws InterruptedException {
        Map map = new Map();
        map.generateGrid(1, 1.5, 1, 3, (index, point) -> map.new Intersection(index, point), (vertex, anotherVertex) -> map.new Street(1));
        map.getIntersections().get(0).getStreets().get(0).width = 2;

        Set<Pedestrian> pedestrians = new HashSet<>();
        Pedestrian pedestrian1 = new MockPedestrian(0, map.getIntersections().get(2));
        pedestrian1.moveTo(new Place(map.getIntersections().get(0).getStreets().get(0), 0));
        Pedestrian pedestrian2 = new Pedestrian(1, map.getIntersections().get(2));
        pedestrian2.moveTo(new Place(map.getIntersections().get(0).getStreets().get(0), 0));
        pedestrians.add(pedestrian1);
        pedestrians.add(pedestrian2);

        System.out.println(map.getIntersections().get(0).getStreets().get(0).getLength());
        System.out.println(map.getStreets());

        System.out.println(pedestrian1.getPlace());
        System.out.println(pedestrian2.getPlace());

        AgentsScheduler.update(pedestrians, 1.0);

        assertEquals(pedestrian1.getPlace().street.getSource().id, 0);
        assertEquals(pedestrian2.getPlace().street.getSource().id, 1);
    }
}
