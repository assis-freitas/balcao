package dev.assisfreitas.balcao.shared;

import java.util.Objects;
import java.util.UUID;

/** Identifies a shop (the tenant). Every record in every context belongs to exactly one. */
public record BusinessId(UUID value) {

  public BusinessId {
    Objects.requireNonNull(value, "BusinessId needs a value");
  }

  public static BusinessId of(String value) {
    return new BusinessId(UUID.fromString(value));
  }
}
