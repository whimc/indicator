package edu.whimc.indicator.api.search;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

public class PathEdgeGraph<T extends Locatable<T, D>, D> {

    private final Table<Node, Node, List<T>> edges = HashBasedTable.create();

    public void addEdge(Node origin, Node destination, List<T> path) {
      this.edges.put(origin, destination, path);
    }

    public static class Node {
      @Setter @Getter
      private int distance = Integer.MAX_VALUE;
      @Setter @Getter
      private Node previous;
    }

    public List<List<T>> findMinimumPath(Node origin, Node destination) {
      Queue<Node> toVisit = new PriorityQueue<>(Comparator.comparingInt(Node::getDistance));
      Set<Node> visited = new HashSet<>();

      origin.setDistance(0);
      origin.setPrevious(null);
      visited.add(origin);
      edges.row(origin).forEach((node, path) -> {
        node.setDistance(path.size());
        node.setPrevious(origin);
        toVisit.add(node);
      });

      Node current;
      while (!toVisit.isEmpty()) {
        current = toVisit.poll();
        if (current.equals(destination)) {
          LinkedList<List<T>> paths = new LinkedList<>();
          while (current.getPrevious() != null) {
            paths.addFirst(edges.get(current.getPrevious(), current));
            current = current.getPrevious();
          }
          return paths;
        }

        // Not yet done
        for (Map.Entry<Node, List<T>> outlet : edges.row(current).entrySet()) {
          if (visited.contains(outlet.getKey())) {
            continue;
          }
          if (outlet.getKey().getDistance() > current.getDistance() + outlet.getValue().size()) {
            // A better path for this node would be to come from current.
            // We can assume that is already queued. Remove from waiting queue to update.
            toVisit.remove(outlet.getKey());
          }
          outlet.getKey().setDistance(current.distance + outlet.getValue().size());
          outlet.getKey().setPrevious(current);
          toVisit.add(outlet.getKey());
        }
      }

      return null;  // Could not find it

    }

}
