package dev.assisfreitas.balcao.ledger.application.port.in;

import dev.assisfreitas.balcao.ledger.domain.EntryType;
import dev.assisfreitas.balcao.shared.Money;
import dev.assisfreitas.balcao.shared.PaymentMethod;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Read side of the Ledger: flat views built for screens, not aggregates. */
public interface LedgerQueries {

  Statement statement(LocalDate from, LocalDate to);

  record Statement(LocalDate from, LocalDate to, Balance balance, List<EntryView> entries) {}

  record Balance(Money income, Money expense) {
    public Money net() {
      return income.minus(expense);
    }
  }

  record EntryView(
      UUID id,
      EntryType type,
      Money amount,
      String description,
      String category,
      PaymentMethod paymentMethod,
      LocalDate occurredOn,
      UUID reversalOf,
      boolean reversed) {}
}
