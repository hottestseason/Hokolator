package com.hottestseason.hokolator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Simulator {
    public final Map map;
    public final Set<Pedestrian> pedestrians;
    public double timeLimit = Double.MAX_VALUE;

    public static void main(String[] args) {
        int numOfPedestrians = Integer.parseInt(args[2]);

        Map map = new Map();
        map.generateGrid(10, 10, Integer.parseInt(args[0]), Integer.parseInt(args[1]), (index, point) -> map.new Intersection(index, point), (vertex, anotherVertex) -> map.new Street(2));
        List<Map.Intersection> intersections = map.getIntersections();
        Map.Intersection goal1 = intersections.get(0);
        Map.Intersection goal2 = intersections.get(intersections.size() - 1);
        Set<Pedestrian> pedestrians = new HashSet<>();
        for (int i = 0; i < numOfPedestrians; i++) {
            Map.Intersection goal = i % 2 == 0 ? goal1 : goal2;
            Pedestrian pedestrian = new Pedestrian(i, goal);
            pedestrians.add(pedestrian);
            while (true) {
                Map.Intersection intersection =  Utils.getRandomlyFrom(intersections);
                if (!intersection.getStreets().isEmpty()) {
                    Map.Street street = Utils.getRandomlyFrom(intersection.getStreets());
                    if (pedestrian.moveTo(new Place(street, 0))) {
                        break;
                    }
                }
            }
        }
        Simulator simulator = new Simulator(map, pedestrians);
        simulator.timeLimit = Double.parseDouble(args[3]);
        long start = System.currentTimeMillis();
        simulator.run();
        System.out.println(System.currentTimeMillis() - start);
    }

    public Simulator(Map map, Set<Pedestrian> pedestrians) {
        this.map = map;
        this.pedestrians = pedestrians;
    }

    public void run() {
        double time = 0;
        while (time <= timeLimit) {
            time += 1.0;
            pedestrians.stream().parallel().forEach(pedestrian -> pedestrian.update(1.0));
            int numOfFinishedPedestrians = 0;
            for (Pedestrian pedestrian : pedestrians) {
                if (pedestrian.isAtGoal()) numOfFinishedPedestrians++;
            }
            System.out.println(time + ": " + numOfFinishedPedestrians);
            if (numOfFinishedPedestrians == pedestrians.size()) break;
        }
        for (Pedestrian pedestrian : pedestrians) {
            System.out.println(pedestrian.id + ": " + pedestrian.time);
        }
    }
}
