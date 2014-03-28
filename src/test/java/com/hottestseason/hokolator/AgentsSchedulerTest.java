package com.hottestseason.hokolator;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
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
    public void testUpdate() throws InterruptedException {
        Map map = new Map();
        map.generateGrid(1, 1.6, 1, 3, (index, point) -> map.new Intersection(index, point), (vertex, anotherVertex) -> map.new Street(1));
        Map.Street startStreet = map.getIntersections().get(0).getStreets().get(0);
        Map.Intersection goal = map.getIntersections().get(2);
        startStreet.width = 2;

        Set<Pedestrian> pedestrians = new HashSet<>();
        Pedestrian pedestrian1 = new MockPedestrian(1, goal);
        Pedestrian pedestrian2 = new MockPedestrian(2, goal);
        pedestrians.add(pedestrian1);
        pedestrians.add(pedestrian2);
        pedestrian1.moveTo(new Place(startStreet, 0));
        pedestrian2.moveTo(new Place(startStreet, 0));

        AgentsScheduler.update(map.getStreets(), 1.0);
        AgentsScheduler.update(pedestrians, 1.0);
        assertEquals(pedestrian1.getPlace().street.getSource().id, 0);
        assertEquals(pedestrian2.getPlace().street.getSource().id, 1);
    }

    @Test
    public void testOrdered() throws InterruptedException {
        List<String> result = new ArrayList<>();
        List<Agent> ordered = new ArrayList<>();
        Comparator<Agent> comparator = (a1, a2) -> { return ordered.indexOf(a1) - ordered.indexOf(a2); };
        Set<Agent> agents = new HashSet<>(), abc = new HashSet<>(), bcd = new HashSet<>(), cde = new HashSet<>(), ad = new HashSet<>();
        Agent a = new Agent() { @Override public void update(double time) throws InterruptedException { AgentsScheduler.ordered("ordered", this, abc, comparator, () -> result.add("a")); } };
        Agent b = new Agent() { @Override public void update(double time) throws InterruptedException { AgentsScheduler.ordered("ordered", this, bcd, comparator, () -> result.add("b")); } };
        Agent c = new Agent() { @Override public void update(double time) throws InterruptedException { AgentsScheduler.ordered("ordered", this, cde, comparator, () -> result.add("c")); } };
        Agent d = new Agent() { @Override public void update(double time) throws InterruptedException { AgentsScheduler.ordered("ordered", this, ad, comparator, () -> result.add("d")); } };
        Agent e = new Agent() { @Override public void update(double time) throws InterruptedException { AgentsScheduler.ordered("ordered", this, cde, comparator, () -> result.add("e")); } };
        ordered.add(b); ordered.add(c); ordered.add(e); ordered.add(d); ordered.add(a);
        abc.add(a); abc.add(b); abc.add(c);
        bcd.add(b); bcd.add(c); bcd.add(d);
        cde.add(c); cde.add(d); cde.add(e);
        ad.add(a); ad.add(d);
        agents.add(a); agents.add(b); agents.add(c); agents.add(d); agents.add(e);
        AgentsScheduler.update(agents, 0);
        assertEquals(Arrays.asList("b", "c", "e", "d", "a"), result);
    }
}
