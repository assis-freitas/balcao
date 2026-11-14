package dev.assisfreitas.balcao.ledger.application;

import dev.assisfreitas.balcao.ledger.EntryRecorded;
import dev.assisfreitas.balcao.ledger.application.port.in.RecordEntryUseCase;
import dev.assisfreitas.balcao.ledger.application.port.out.CurrentBusiness;
import dev.assisfreitas.balcao.ledger.application.port.out.EntryRepository;
import dev.assisfreitas.balcao.ledger.application.port.out.LedgerEventPublisher;
import dev.assisfreitas.balcao.ledger.domain.Category;
import dev.assisfreitas.balcao.ledger.domain.Entry;
import dev.assisfreitas.balcao.ledger.domain.EntryId;
import dev.assisfreitas.balcao.ledger.domain.EntryType;
import java.time.Clock;
import java.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

public class RecordEntryService implements RecordEntryUseCase {

  private final EntryRepository entries;
  private final CurrentBusiness business;
  private final LedgerEventPublisher events;
  private final Clock clock;

  public RecordEntryService(
      EntryRepository entries, CurrentBusiness business, LedgerEventPublisher events, Clock clock) {
    this.entries = entries;
    this.business = business;
    this.events = events;
    this.clock = clock;
  }

  @Override
  @Transactional
  public EntryId record(Command command) {
    Entry entry =
        Entry.record(
            EntryId.generate(),
            business.id(),
            command.type(),
            command.amount(),
            command.description(),
            new Category(command.category()),
            command.paymentMethod(),
            command.occurredOn(),
            LocalDate.now(clock),
            clock.instant());

    entries.save(entry);
    events.publish(
        new EntryRecorded(
            entry.id().value(),
            entry.businessId(),
            entry.type() == EntryType.INCOME,
            entry.amount(),
            entry.paymentMethod(),
            entry.occurredOn()));

    return entry.id();
  }
}
