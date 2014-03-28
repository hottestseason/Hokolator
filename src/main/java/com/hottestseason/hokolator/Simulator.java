package com.hottestseason.hokolator;

import java.util.HashSet;
import java.util.Set;

public class Simulator {
    public final Map map;
    public final Set<Pedestrian> pedestrians;

    public static void main(String[] args) {
        int numOfPedestrians = 500;

        Map map = new Map();
        map.generateGrid(10, 10, 50, 50, (index, point) -> map.new Intersection(index, point), (vertex, anotherVertex) -> map.new Street(2));
        Set<Pedestrian> pedestrians = new HashSet<>();
        for (int i = 0; i < numOfPedestrians; i++) {
            Map.Intersection goal;
            if (i % 2 == 0) {
                goal = map.getIntersections().get(0);
            } else {
                goal = map.getIntersections().get(map.getIntersections().size() - 1);
            }
            Pedestrian pedestrian = new Pedestrian(i, goal);
            pedestrians.add(pedestrian);
            while (true) {
                Map.Intersection intersection =  Utils.getRandomlyFrom(map.getIntersections());
                if (!intersection.getStreets().isEmpty()) {
                    Map.Street street = Utils.getRandomlyFrom(intersection.getStreets());
                    if (!street.getSource().findShortestPathTo(pedestrian.goal).isEmpty() && pedestrian.moveTo(new Place(street, 0))) {
                        break;
                    }
                }
            }
        }
        Simulator simulator = new Simulator(map, pedestrians);
        long start = System.currentTimeMillis();
        simulator.run();
        System.out.println(System.currentTimeMillis() - start);
    }

    public Simulator(Map map, Set<Pedestrian> pedestrians) {
        this.map = map;
        this.pedestrians = pedestrians;
    }

    public void run() {
        while (true) {
            pedestrians.stream().parallel().forEach(pedestrian -> pedestrian.update(1.0));
            int numOfFinishedPedestrians = 0;
            for (Pedestrian pedestrian : pedestrians) {
                if (pedestrian.isAtGoal()) numOfFinishedPedestrians++;
            }
            System.out.println(numOfFinishedPedestrians);
            if (numOfFinishedPedestrians == pedestrians.size()) break;
        }
        for (Pedestrian pedestrian : pedestrians) {
            System.out.println(pedestrian.id + ": " + pedestrian.time);
        }
    }
}
