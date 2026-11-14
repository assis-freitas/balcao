package dev.assisfreitas.balcao.ledger.application.port.in;

import dev.assisfreitas.balcao.ledger.domain.EntryId;

public final class EntryNotFound extends RuntimeException {

  public EntryNotFound(EntryId id) {
    super("Entry " + id.value() + " not found");
  }
}
