package edu.whimc.indicator.spigot.search.mode;

import edu.whimc.indicator.api.path.Mode;
import edu.whimc.indicator.api.path.ModeType;
import edu.whimc.indicator.spigot.path.CellImpl;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;

public class WalkMode implements Mode<CellImpl, World> {
  @Override
  public Map<CellImpl, Float> getDestinations(CellImpl origin) {
    Map<CellImpl, Float> locations = new HashMap<>();
    if (origin.getBlockAtOffset(0, -1, 0).isPassable()) {
      return locations;
    }
    if (!origin.getBlockAtOffset(0, 0, 0).isPassable()) {
      return locations;
    }
    if (!origin.getBlockAtOffset(0, 1, 0).isPassable()) {
      return locations;
    }

    // 1 block away
    for (int offX = -1; offX <= 1; offX++) {
      outerZ:
      for (int offZ = -1; offZ <= 1; offZ++) {
        // For the diagonal points, check that the path is clear in both
        //  lateral directions as well as diagonally
        for (int offXIn = Math.abs(offX) /* normalize sign */; offXIn >= 0; offXIn--) {
          for (int offZIn = Math.abs(offX) /* normalize sign */; offZIn >= 0; offZIn--) {
            if (offXIn == 0 && offZIn == 0) continue;
            for (int offY = 0; offY <= 1; offY++) { // Check two blocks tall
              if (!origin.getBlockAtOffset(offXIn * offX /* get sign back */, offY, offZIn * offZ).isPassable()) {
                continue outerZ;
              }
            }
          }
        }
        for (int offY = -1; offY >= -4; offY--) {  // Check for floor anywhere up to a 3 block fall
          if (!origin.getBlockAtOffset(offX, offY, offZ).isPassable()) {
            locations.put(origin.createLocatableAtOffset(offX, offY+1, offZ), 0f + offX*offX + (offY+1)*(offY+1) + offZ*offZ);
            break;
          }
        }
      }
    }
    return locations;
  }

  @Override
  public ModeType getType() {
    return ModeTypes.WALK;
  }
}