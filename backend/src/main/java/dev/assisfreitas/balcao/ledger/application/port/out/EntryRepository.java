package dev.assisfreitas.balcao.ledger.application.port.out;

import dev.assisfreitas.balcao.ledger.domain.Entry;
import dev.assisfreitas.balcao.ledger.domain.EntryId;
import dev.assisfreitas.balcao.shared.BusinessId;
import java.util.Optional;

public interface EntryRepository {

  void save(Entry entry);

  Optional<Entry> findById(BusinessId businessId, EntryId id);

  boolean hasReversal(EntryId id);
}
