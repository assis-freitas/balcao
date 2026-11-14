package dev.assisfreitas.balcao.ledger.adapter.in.web;

import dev.assisfreitas.balcao.ledger.application.port.in.RecordEntryUseCase;
import dev.assisfreitas.balcao.ledger.domain.EntryType;
import dev.assisfreitas.balcao.shared.Money;
import dev.assisfreitas.balcao.shared.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Only checks the shape of the request. Business rules (positive amount, no future dates...) live
 * in the domain and come back as 422.
 */
record RecordEntryRequest(
    @NotNull EntryType type,
    @NotNull BigDecimal amount,
    String description,
    String category,
    @NotNull PaymentMethod paymentMethod,
    @NotNull LocalDate occurredOn) {

  RecordEntryUseCase.Command toCommand() {
    return new RecordEntryUseCase.Command(
        type, Money.of(amount), description, category, paymentMethod, occurredOn);
  }
}
