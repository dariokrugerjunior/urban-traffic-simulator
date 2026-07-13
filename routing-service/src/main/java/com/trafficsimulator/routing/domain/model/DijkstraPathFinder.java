package com.trafficsimulator.routing.domain.model;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/** Pure Dijkstra shortest-path over a RoadNetwork's current edge weights. */
public class DijkstraPathFinder {

    public Route findShortest(RoadNetwork network, String start, String end) {
        if (!network.hasNode(start) || !network.hasNode(end)) {
            return Route.notFound();
        }
        Map<String, Double> dist = new HashMap<>();
        Map<String, RouteStreet> incoming = new HashMap<>();
        dist.put(start, 0.0);

        PriorityQueue<String> queue = new PriorityQueue<>(
                Comparator.comparingDouble(n -> dist.getOrDefault(n, Double.POSITIVE_INFINITY)));
        queue.add(start);
        Set<String> settled = new HashSet<>();

        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (!settled.add(current)) {
                continue;
            }
            if (current.equals(end)) {
                break;
            }
            double currentDist = dist.getOrDefault(current, Double.POSITIVE_INFINITY);
            for (RouteStreet edge : network.outgoing(current)) {
                double newDist = currentDist + edge.weight();
                if (newDist < dist.getOrDefault(edge.to(), Double.POSITIVE_INFINITY)) {
                    dist.put(edge.to(), newDist);
                    incoming.put(edge.to(), edge);
                    queue.add(edge.to());
                }
            }
        }

        if (!dist.containsKey(end)) {
            return Route.notFound();
        }
        return reconstruct(start, end, dist.get(end), incoming);
    }

    private Route reconstruct(String start, String end, double cost, Map<String, RouteStreet> incoming) {
        LinkedList<String> streetIds = new LinkedList<>();
        LinkedList<String> nodePath = new LinkedList<>();
        nodePath.addFirst(end);
        String node = end;
        while (!node.equals(start)) {
            RouteStreet edge = incoming.get(node);
            streetIds.addFirst(edge.id());
            node = edge.from();
            nodePath.addFirst(node);
        }
        return new Route(streetIds, nodePath, cost, true);
    }
}
