package dev.assisfreitas.balcao.ledger.adapter.in.web;

import dev.assisfreitas.balcao.ledger.application.port.in.LedgerQueries;
import dev.assisfreitas.balcao.ledger.domain.EntryType;
import dev.assisfreitas.balcao.shared.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Amounts go out as decimal reais ("12.50"), never as floating point. */
record StatementResponse(
    LocalDate from, LocalDate to, BalanceResponse balance, List<EntryResponse> entries) {

  static StatementResponse from(LedgerQueries.Statement statement) {
    var b = statement.balance();
    return new StatementResponse(
        statement.from(),
        statement.to(),
        new BalanceResponse(
            b.income().toBigDecimal(), b.expense().toBigDecimal(), b.net().toBigDecimal()),
        statement.entries().stream()
            .map(
                e ->
                    new EntryResponse(
                        e.id(),
                        e.type(),
                        e.amount().toBigDecimal(),
                        e.description(),
                        e.category(),
                        e.paymentMethod(),
                        e.occurredOn(),
                        e.reversalOf(),
                        e.reversed()))
            .toList());
  }

  record BalanceResponse(BigDecimal income, BigDecimal expense, BigDecimal net) {}

  record EntryResponse(
      UUID id,
      EntryType type,
      BigDecimal amount,
      String description,
      String category,
      PaymentMethod paymentMethod,
      LocalDate occurredOn,
      UUID reversalOf,
      boolean reversed) {}
}
