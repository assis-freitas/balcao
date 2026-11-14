package dev.assisfreitas.balcao.ledger.domain;

import java.util.Objects;
import java.util.UUID;

public record EntryId(UUID value) {

  public EntryId {
    Objects.requireNonNull(value, "EntryId needs a value");
  }

  public static EntryId generate() {
    return new EntryId(UUID.randomUUID());
  }
}
