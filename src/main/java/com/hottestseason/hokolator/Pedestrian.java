package com.hottestseason.hokolator;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public class Pedestrian {
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

    public Place getPlace() {
        return place;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double calcSpeed() {
        return new Random((long) speed).nextDouble() * 5 + 0.1;
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

    public void update(double time) {
        if (!isAtGoal()) {
            speed = calcSpeed();
            nextPlace = calcNextPlace(time);
            moveTo(nextPlace);
            this.time += time;
        }
    }
}
