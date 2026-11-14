package dev.assisfreitas.balcao.ledger.application;

import dev.assisfreitas.balcao.ledger.application.port.out.EntryRepository;
import dev.assisfreitas.balcao.ledger.domain.Entry;
import dev.assisfreitas.balcao.ledger.domain.EntryId;
import dev.assisfreitas.balcao.shared.BusinessId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** A fake for the outgoing port: use cases are tested without a database. */
class InMemoryEntryRepository implements EntryRepository {

  final List<Entry> saved = new ArrayList<>();

  @Override
  public void save(Entry entry) {
    saved.add(entry);
  }

  @Override
  public Optional<Entry> findById(BusinessId businessId, EntryId id) {
    return saved.stream()
        .filter(e -> e.id().equals(id) && e.businessId().equals(businessId))
        .findFirst();
  }

  @Override
  public boolean hasReversal(EntryId id) {
    return saved.stream().anyMatch(e -> e.reversalOf().filter(id::equals).isPresent());
  }
}
