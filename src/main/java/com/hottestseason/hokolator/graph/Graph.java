package com.hottestseason.hokolator.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Graph<V, E> {
    protected final Map<V, VertexContainer> vertexContainerMap = new HashMap<>();
    protected final Map<E, EdgeContainer> edgeContainerMap = new HashMap<>();

    public void add(V vertex) {
        VertexContainer vertexContainer = new VertexContainer(vertex);
        vertexContainerMap.put(vertex, vertexContainer);
    }

    public List<V> getVertexes() {
        return vertexContainerMap.values().stream().map(container -> container.vertex).collect(Collectors.toList());
    }

    public V getSourceOf(E edge) {
        return edgeContainerMap.get(edge).source;
    }

    public V getTargetOf(E edge) {
        return edgeContainerMap.get(edge).target;
    }

    public Collection<E> getEdges() {
        return edgeContainerMap.values().stream().map(container -> container.edge).collect(Collectors.toList());
    }

    public List<E> getEdgesOf(V vertex) {
        return vertexContainerMap.get(vertex).edgeContainers.stream().map(container -> container.edge).collect(Collectors.toList());
    }

    public Optional<V> getConnectionBetween(E edge1, E edge2) {
        return edgeContainerMap.get(edge1).getConnectionWith(edgeContainerMap.get(edge2));
    }

    public boolean connect(V source, V target, E edge){
        if (vertexContainerMap.get(source).isConnectedTo(target)) {
            return false;
        } else {
            EdgeContainer edgeContainer = new EdgeContainer(edge, source, target);
            vertexContainerMap.get(source).edgeContainers.add(edgeContainer);
            edgeContainerMap.put(edge, edgeContainer);
            return true;
        }
    }

    public boolean connectBidrectionally(V source, V target, E edge1, E edge2){
        return connect(source, target, edge1) && connect(target, source, edge2);
    }

    class VertexContainer {
        public final List<EdgeContainer> edgeContainers = new ArrayList<>();
        private final V vertex;

        VertexContainer(V v) {
            this.vertex = v;
        }

        @Override
        public int hashCode() {
            return vertex.hashCode();
        }

        public boolean isConnectedTo(V vertex) {
            for (EdgeContainer edgeContainer : edgeContainers) {
                if (edgeContainer.target.equals(vertex)) {
                    return true;
                }
            }
            return false;
        }
    }

    public class EdgeContainer {
        public final E edge;
        public final V source, target;

        EdgeContainer(E edge, V source, V target) {
            this.edge = edge;
            this.source = source; this.target = target;
        }

        public Optional<V> getConnectionWith(EdgeContainer edgeContainer) {
            if (target == edgeContainer.source || target == edgeContainer.target) {
                return Optional.of(target);
            } else if (source == edgeContainer.source || source == edgeContainer.target) {
                return Optional.of(source);
            } else {
                return Optional.empty();
            }
        }
    }
}