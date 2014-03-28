package com.hottestseason.hokolator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.hottestseason.geom.Point2D;
import com.hottestseason.hokolator.Map.Intersection;
import com.hottestseason.hokolator.Map.Street;
import com.hottestseason.hokolator.graph.Dijkstra;
import com.hottestseason.hokolator.graph.GeometricGraph;
import com.hottestseason.hokolator.graph.Graph;
import com.hottestseason.hokolator.graph.Weighted;

public class Map implements GeometricGraph<Intersection, Street> {
    private final Graph<Intersection, Street> graph = new Graph<>();

    @Override
    public void add(Intersection intersection) {
        graph.add(intersection);
    }

    @Override
    public void connect(Intersection source, Intersection target, Street street) {
        graph.connect(source, target, street);
    }

    public static Street[] sortLinks(Street...links) {
        Arrays.sort(links);
        return links;
    }

    public List<Intersection> getIntersections() {
        return graph.getVertexes();
    }

    public Collection<Street> getStreets() {
        return graph.getEdges();
    }

    public List<Street> findShortestPathBetween(Intersection start, Intersection end) {
        return new Dijkstra<Intersection, Street>(graph, start, end).call();
    }

    public class Intersection extends Point2D {
        public final int id;

        public Intersection(int id, Point2D point) {
            super(point);
            this.id = id;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Intersection) {
                return ((Intersection) obj).id == id;
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return id;
        }

        @Override
        public String toString() {
            return String.valueOf(id);
        }

        public List<Street> getStreets() {
            return graph.getEdgesOf(this);
        }

        public List<Street> findShortestPathTo(Intersection end) {
            return findShortestPathBetween(this, end);
        }
    }

    public class Street implements Comparable<Street>, Weighted {
        public int width;
        private final UUID uuid = UUID.randomUUID();
        private final List<Pedestrian> pedestrians = new ArrayList<>();

        public Street(int width) {
            this.width = width;
        }

        @Override
        public String toString() {
            return getSource() + " => " + getTarget();
        }

        @Override
        public int compareTo(Street link) {
            return uuid.compareTo(link.uuid);
        }

        @Override
        public double getWeight() {
            return getLength();
        }

        public Intersection getSource() {
            return graph.getSourceOf(this);
        }

        public Intersection getTarget() {
            return graph.getTargetOf(this);
        }

        public double getLength() {
            return getSource().getDistanceFrom(getTarget());
        }

        public Optional<Intersection> getIntersectionWith(Street street) {
            return graph.getConnectionBetween(this, street);
        }

        public boolean canEnter() {
            synchronized (pedestrians) {
                return pedestrians.stream().filter(pedestrian -> !pedestrian.isAtGoal()).collect(Collectors.toList()).size() < width;
            }
        }

        public boolean accept(Pedestrian pedestrian) {
            if (pedestrian.getPlace() == null) {
                synchronized (pedestrians) {
                    if (canEnter()) {
                        return pedestrians.add(pedestrian);
                    } else {
                        return false;
                    }
                }
            } else {
                Street[] links = Map.sortLinks(pedestrian.getPlace().street, this);
                synchronized (links[0].pedestrians) {
                    synchronized (links[1].pedestrians) {
                        if (canEnter()) {
                            pedestrian.getPlace().street.pedestrians.remove(pedestrian);
                            pedestrians.add(pedestrian);
                            return true;
                        } else {
                            return false;
                        }
                    }
                }
            }
        }
    }
}
