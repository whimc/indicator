package edu.whimc.indicator.api.path;

import lombok.Data;

@Data
public class Endpoint<P, T extends Locatable<T, D>, D> {

  private final P purpose;
  private final T location;

}