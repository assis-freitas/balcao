package dev.assisfreitas.balcao.ledger.application.port.out;

import dev.assisfreitas.balcao.ledger.application.port.in.LedgerQueries.Balance;
import dev.assisfreitas.balcao.ledger.application.port.in.LedgerQueries.EntryView;
import dev.assisfreitas.balcao.shared.BusinessId;
import java.time.LocalDate;
import java.util.List;

/** Queries straight from storage, skipping the aggregate (CQRS-lite). */
public interface LedgerReadModel {

  List<EntryView> entries(BusinessId businessId, LocalDate from, LocalDate to);

  Balance balance(BusinessId businessId, LocalDate from, LocalDate to);
}
