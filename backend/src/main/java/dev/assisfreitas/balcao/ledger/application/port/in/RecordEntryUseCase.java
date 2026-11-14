package dev.assisfreitas.balcao.ledger.application.port.in;

import dev.assisfreitas.balcao.ledger.domain.EntryId;
import dev.assisfreitas.balcao.ledger.domain.EntryType;
import dev.assisfreitas.balcao.shared.Money;
import dev.assisfreitas.balcao.shared.PaymentMethod;
import java.time.LocalDate;

public interface RecordEntryUseCase {

  EntryId record(Command command);

  record Command(
      EntryType type,
      Money amount,
      String description,
      String category,
      PaymentMethod paymentMethod,
      LocalDate occurredOn) {}
}
