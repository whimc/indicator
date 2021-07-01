package edu.whimc.indicator.common.search;

import edu.whimc.indicator.common.path.Locatable;
import lombok.Data;

import java.util.function.Supplier;

@Data
public class LocalSearchRequest<T extends Locatable<T, D>, D> {
  private final T origin;
  private final T destination;
  private final PathEdgeWeightedGraph.Node originNode;
  private final PathEdgeWeightedGraph.Node destinationNode;
  private final Supplier<Boolean> cancellation;
  private final boolean cacheable;
}