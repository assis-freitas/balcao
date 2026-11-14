package dev.assisfreitas.balcao.ledger.adapter.out.persistence;

import dev.assisfreitas.balcao.ledger.application.port.out.EntryRepository;
import dev.assisfreitas.balcao.ledger.domain.Category;
import dev.assisfreitas.balcao.ledger.domain.Entry;
import dev.assisfreitas.balcao.ledger.domain.EntryId;
import dev.assisfreitas.balcao.ledger.domain.EntryType;
import dev.assisfreitas.balcao.shared.BusinessId;
import dev.assisfreitas.balcao.shared.Money;
import dev.assisfreitas.balcao.shared.PaymentMethod;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Write side: maps the aggregate to and from its JPA shape. */
@Component
class EntryPersistenceAdapter implements EntryRepository {

  private final SpringDataEntryRepository jpa;

  EntryPersistenceAdapter(SpringDataEntryRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public void save(Entry entry) {
    jpa.save(toJpa(entry));
  }

  @Override
  public Optional<Entry> findById(BusinessId businessId, EntryId id) {
    return jpa.findByIdAndBusinessId(id.value(), businessId.value())
        .map(EntryPersistenceAdapter::toDomain);
  }

  @Override
  public boolean hasReversal(EntryId id) {
    return jpa.existsByReversalOf(id.value());
  }

  static EntryJpaEntity toJpa(Entry entry) {
    var row = new EntryJpaEntity();
    row.id = entry.id().value();
    row.businessId = entry.businessId().value();
    row.type = EntryJpaEntity.TypeColumn.valueOf(entry.type().name());
    row.amountCents = entry.amount().cents();
    row.description = entry.description();
    row.category = entry.category().name();
    row.paymentMethod = entry.paymentMethod().name();
    row.occurredOn = entry.occurredOn();
    row.recordedAt = entry.recordedAt();
    row.reversalOf = entry.reversalOf().map(EntryId::value).orElse(null);
    return row;
  }

  static Entry toDomain(EntryJpaEntity row) {
    return Entry.restore(
        new EntryId(row.id),
        new BusinessId(row.businessId),
        EntryType.valueOf(row.type.name()),
        Money.ofCents(row.amountCents),
        row.description,
        new Category(row.category),
        PaymentMethod.valueOf(row.paymentMethod),
        row.occurredOn,
        row.recordedAt,
        row.reversalOf == null ? null : new EntryId(row.reversalOf));
  }
}
