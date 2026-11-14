package dev.assisfreitas.balcao.ledger;

import dev.assisfreitas.balcao.shared.BusinessId;
import java.util.UUID;

/** Published after an entry is cancelled by a reversal entry. */
public record EntryReversed(UUID reversalId, UUID originalId, BusinessId businessId) {}
