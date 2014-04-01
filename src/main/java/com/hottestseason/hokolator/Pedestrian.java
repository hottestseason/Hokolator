package com.hottestseason.hokolator;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Pedestrian implements Agent {
    public final int id;
    public final Map.Intersection goal;
    public double time = 0;
    private double speed;
    private Place place;
    private Place nextPlace;

    public Pedestrian(int id, Map.Intersection goal) {
        this.id = id;
        this.goal = goal;
        speed = id;
        speed = calcSpeed();
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }

    @Override
    public void update(double time) throws InterruptedException {
        if (!isAtGoal()) {
            speed = calcSpeed();
            nextPlace = calcNextPlace(time);
            Set<Pedestrian> neighborPedestrians = nextPlace.street.getNeighborPedestrians();
            if (place.street != nextPlace.street) {
                AgentsScheduler.finished("nextSpeedCalculated", this);
                AgentsScheduler.waitOthers("nextSpeedCalculated", this, neighborPedestrians);
                Set<Pedestrian> conflictingPedestrians = neighborPedestrians.stream().filter(pedestrian -> pedestrian.nextPlace.street == nextPlace.street || pedestrian.place.street == nextPlace.street && pedestrian.nextPlace.street != pedestrian.place.street).collect(Collectors.toSet());
                AgentsScheduler.orderIf("nextSpeedCalculated", this, conflictingPedestrians, (agent1, agent2) -> Double.compare(((Pedestrian) agent1).calcLinkLeftTime(), ((Pedestrian) agent2).calcLinkLeftTime()), () -> moveTo(nextPlace));
            } else {
                AgentsScheduler.finished("nextSpeedCalculated", this);
                moveTo(nextPlace);
           }
           this.time += time;
        } else {
            AgentsScheduler.finished("nextSpeedCalculated", this);
        }
    }

    public Place getPlace() {
        return place;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double calcSpeed() {
        return (speed + 1) % 10;
    }

    public Place calcNextPlace(double time) {
        double nextPosition = speed * time + place.position;
        if (nextPosition < place.street.getLength()) {
            return new Place(place.street, nextPosition);
        } else {
            List<Map.Street> path = place.street.getTarget().findShortestPathTo(goal);
            if (!path.isEmpty()) {
                return new Place(path.get(0), Math.min(nextPosition - place.street.getLength(), path.get(0).getLength()));
            } else {
                return new Place(place.street, place.street.getLength());
            }
        }
    }

    public boolean moveTo(Place nextPlace) {
        if (place == null) {
            if (nextPlace.street.accept(this)) {
                place = nextPlace;
                return true;
            } else {
                return false;
            }
        } else {
            if (place.street == nextPlace.street) {
                place = nextPlace;
                return true;
            } else {
                Optional<Map.Intersection> connectedNode = place.street.getIntersectionWith(nextPlace.street);
                if (connectedNode.isPresent() && connectedNode.get() == place.street.getTarget()) {
                    if (nextPlace.street.accept(this)) {
                        place = nextPlace;
                        return true;
                    } else {
                        place = new Place(place.street, place.street.getLength());
                        return false;
                    }
                } else {
                    throw new RuntimeException(id + ": invalid destination (from: " + place.street + ", to: " + nextPlace.street + ")");
                }
            }
        }
    }

    public boolean isAtGoal() {
        return place.street.getTarget() == goal && place.position == place.street.getLength();
    }

    private double calcLinkLeftTime() {
        if (speed == 0) {
            return Double.MAX_VALUE;
        } else {
            return (place.street.getLength() - place.position) / speed;
        }
    }
}
