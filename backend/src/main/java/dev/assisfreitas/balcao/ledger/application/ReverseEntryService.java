package dev.assisfreitas.balcao.ledger.application;

import dev.assisfreitas.balcao.ledger.EntryReversed;
import dev.assisfreitas.balcao.ledger.application.port.in.EntryNotFound;
import dev.assisfreitas.balcao.ledger.application.port.in.ReverseEntryUseCase;
import dev.assisfreitas.balcao.ledger.application.port.out.CurrentBusiness;
import dev.assisfreitas.balcao.ledger.application.port.out.EntryRepository;
import dev.assisfreitas.balcao.ledger.application.port.out.LedgerEventPublisher;
import dev.assisfreitas.balcao.ledger.domain.Entry;
import dev.assisfreitas.balcao.ledger.domain.EntryId;
import dev.assisfreitas.balcao.ledger.domain.LedgerRuleViolation;
import java.time.Clock;
import java.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

public class ReverseEntryService implements ReverseEntryUseCase {

  private final EntryRepository entries;
  private final CurrentBusiness business;
  private final LedgerEventPublisher events;
  private final Clock clock;

  public ReverseEntryService(
      EntryRepository entries, CurrentBusiness business, LedgerEventPublisher events, Clock clock) {
    this.entries = entries;
    this.business = business;
    this.events = events;
    this.clock = clock;
  }

  @Override
  @Transactional
  public EntryId reverse(EntryId entryId, String reason) {
    Entry original =
        entries.findById(business.id(), entryId).orElseThrow(() -> new EntryNotFound(entryId));
    if (entries.hasReversal(entryId)) {
      throw LedgerRuleViolation.alreadyReversed();
    }

    Entry reversal =
        original.reverse(EntryId.generate(), reason, LocalDate.now(clock), clock.instant());
    entries.save(reversal);
    events.publish(
        new EntryReversed(reversal.id().value(), original.id().value(), original.businessId()));

    return reversal.id();
  }
}
