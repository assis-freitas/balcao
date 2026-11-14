package dev.assisfreitas.balcao.ledger.application.port.in;

import dev.assisfreitas.balcao.ledger.domain.EntryId;

public interface ReverseEntryUseCase {

  /** Cancels an entry by recording its reversal. Returns the reversal's id. */
  EntryId reverse(EntryId entryId, String reason);
}
