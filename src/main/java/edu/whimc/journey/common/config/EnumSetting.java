package edu.whimc.journey.common.config;

import com.google.common.base.Enums;
import org.jetbrains.annotations.NotNull;

/**
 * A setting that holds an enum value.
 *
 * @param <E> the enum type
 */
public class EnumSetting<E extends Enum<E>> extends Setting<E> {
  EnumSetting(@NotNull String path, @NotNull E defaultValue, @NotNull Class<E> clazz) {
    super(path, defaultValue, clazz);
  }

  @Override
  public E parseValue(@NotNull String string) {
    return Enums.getIfPresent(this.clazz, string.toUpperCase()).orNull();
  }

  @Override
  @NotNull
  public String printValue() {
    return getValue().name().toLowerCase();
  }
}
