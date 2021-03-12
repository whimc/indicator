package edu.whimc.indicator.spigot.command;

import edu.whimc.indicator.Indicator;
import edu.whimc.indicator.api.path.Path;
import edu.whimc.indicator.api.path.Step;
import edu.whimc.indicator.spigot.command.common.CommandNode;
import edu.whimc.indicator.spigot.destination.EndpointImpl;
import edu.whimc.indicator.spigot.search.IndicatorSearch;
import edu.whimc.indicator.spigot.path.CellImpl;
import edu.whimc.indicator.spigot.search.mode.ModeTypes;
import edu.whimc.indicator.spigot.util.Format;
import edu.whimc.indicator.spigot.util.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public class TrailCommand extends CommandNode {
  public TrailCommand() {
    super(null,
        Permissions.TRAIL_PERMISSION,
        "Provide a trail to your destination",
        "trail");
  }

  @Override
  public boolean onWrappedCommand(@NotNull CommandSender sender,
                                  @NotNull Command command,
                                  @NotNull String label,
                                  @NotNull String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage(Format.error("Only players may perform this command"));
      return false;
    }
    Player player = (Player) sender;
    EndpointImpl destination;
    if (Indicator.getInstance()
        .getEndpointManager()
        .containsKey(player.getUniqueId())) {
      destination = Indicator.getInstance().getEndpointManager().get(player.getUniqueId());
    } else {
      sender.sendMessage(Format.warn("You don't have a destination set!"));
      sender.sendMessage(Format.warn("Defaulting to your world's spawn."));
      destination = new EndpointImpl(Indicator.getInstance(), new CellImpl(player.getWorld().getSpawnLocation()));
      Indicator.getInstance().getEndpointManager().put(player.getUniqueId(), destination);
    }

    Bukkit.getScheduler().runTaskAsynchronously(Indicator.getInstance(), () -> {
      IndicatorSearch search = new IndicatorSearch(player);
//      search.setLocalSearchVisitationCallback(loc -> Indicator.getInstance().getLogger().info("Pathfinding - Visited: " + loc.print()));

      // Set up a "Working..." message if it takes too long
      BukkitTask workingNotification = Bukkit.getScheduler().runTaskLater(Indicator.getInstance(), () ->
          sender.sendMessage(Format.info("Finding a path to your destination...")), 10);

      Path<CellImpl, World> path = search.findPath(
          new CellImpl(player.getLocation()),
          destination.getLocation());

      // Cancel working... message if it hasn't happened yet
      workingNotification.cancel();

      if (path == null) {
        sender.sendMessage(Format.error("A path to your destination could not be found."));
        return;
      }

      sender.sendMessage(Format.success("Showing a trail to your destination"));

      Random rand = new Random();
      BukkitTask illumination = Bukkit.getScheduler().runTaskTimer(Indicator.getInstance(), () -> {
        List<Step<CellImpl, World>> allSteps = path.getAllSteps();
        for (int i = 0; i < allSteps.size() - 1; i++) {
          for (int p = 0; p < 6; p++) {
            Particle particle;
            if (allSteps.get(i+1).getModeType().equals(ModeTypes.WALK)) {
              particle = Particle.FLAME;
            } else {
              particle = Particle.HEART;
            }
            allSteps.get(i).getLocatable().getDomain().spawnParticle(particle,
                allSteps.get(i).getLocatable().getX() + rand.nextDouble(),
                allSteps.get(i).getLocatable().getY() + 0.2f,
                allSteps.get(i).getLocatable().getZ() + rand.nextDouble(),
                1,
                0, 0, 0,
                0);
          }
        }
      }, 0, 20);

      Bukkit.getScheduler().runTaskLater(Indicator.getInstance(), illumination::cancel, 100);

    });
    return true;

  }
}