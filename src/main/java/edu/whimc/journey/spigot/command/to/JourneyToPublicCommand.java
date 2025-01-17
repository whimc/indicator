/*
 * MIT License
 *
 * Copyright 2021 Pieter Svenson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package edu.whimc.journey.spigot.command.to;

import edu.whimc.journey.common.data.DataAccessException;
import edu.whimc.journey.common.data.PublicEndpointManager;
import edu.whimc.journey.common.tools.BufferedSupplier;
import edu.whimc.journey.common.util.Validator;
import edu.whimc.journey.spigot.JourneySpigot;
import edu.whimc.journey.spigot.command.JourneyCommand;
import edu.whimc.journey.spigot.command.common.CommandError;
import edu.whimc.journey.spigot.command.common.CommandNode;
import edu.whimc.journey.spigot.command.common.Parameter;
import edu.whimc.journey.spigot.command.common.PlayerCommandNode;
import edu.whimc.journey.spigot.navigation.LocationCell;
import edu.whimc.journey.spigot.util.Format;
import edu.whimc.journey.spigot.util.Permissions;
import java.util.List;
import java.util.Map;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * The command to allow players to journey to a public destination.
 */
public class JourneyToPublicCommand extends PlayerCommandNode {

  /**
   * General constructor.
   *
   * @param parent the parent
   */
  public JourneyToPublicCommand(@NotNull CommandNode parent) {
    super(parent,
        Permissions.JOURNEY_TO_PUBLIC_USE,
        "Journey to a public destination",
        "public");

    BufferedSupplier<List<String>> serverLocationSupplier = JourneyCommand.bufferedPublicEndpointSupplier();
    addSubcommand(Parameter.builder()
        .supplier(Parameter.ParameterSupplier.builder()
            .usage("<name>")
            .allowedEntries((src, prev) -> serverLocationSupplier.get())
            .strict(false)
            .build())
        .build(), "Use a name");

  }

  @Override
  public boolean onWrappedPlayerCommand(@NotNull Player player,
                                        @NotNull Command command,
                                        @NotNull String label,
                                        @NotNull String[] args,
                                        @NotNull Map<String, String> flags) throws DataAccessException {

    if (args.length == 0) {
      sendCommandUsageError(player, CommandError.FEW_ARGUMENTS);
      return false;
    }

    LocationCell endLocation;
    PublicEndpointManager<LocationCell, World> publicEndpointManager = JourneySpigot.getInstance()
        .getDataManager()
        .getPublicEndpointManager();
    try {
      endLocation = publicEndpointManager.getPublicEndpoint(args[0]);

      if (endLocation == null) {
        player.spigot().sendMessage(Format.error("The server location ",
            Format.toPlain(Format.note(args[0])),
            " could not be found."));
        return false;
      }
    } catch (IllegalArgumentException e) {
      player.spigot().sendMessage(Format.error("Your numbers could not be read."));
      return false;
    }

    JourneyCommand.journeyTo(player, endLocation, flags);

    // Check if we should save a server endpoint
    if (args.length >= 5) {
      if (publicEndpointManager.hasPublicEndpoint(endLocation)) {
        player.spigot().sendMessage(Format.error("A server location already exists at that location!"));
        return false;
      }
      if (publicEndpointManager.hasPublicEndpoint(args[4])) {
        player.spigot().sendMessage(Format.error("A server location already exists with that name!"));
        return false;
      }
      if (Validator.isInvalidDataName(args[4])) {
        player.spigot().sendMessage(Format.error("Your server name ",
            Format.toPlain(Format.note(args[4])),
            " contains illegal characters."));
        return false;
      }
      // Save it!
      publicEndpointManager.addPublicEndpoint(endLocation, args[4]);
      player.spigot().sendMessage(Format.success("Saved your server location with name ",
          Format.toPlain(Format.note(args[4])),
          "!"));
    }

    return true;

  }
}