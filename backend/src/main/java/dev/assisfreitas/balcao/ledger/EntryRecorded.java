package dev.assisfreitas.balcao.ledger;

import dev.assisfreitas.balcao.shared.BusinessId;
import dev.assisfreitas.balcao.shared.Money;
import dev.assisfreitas.balcao.shared.PaymentMethod;
import java.time.LocalDate;
import java.util.UUID;

/** Published after an income or expense is recorded. */
public record EntryRecorded(
    UUID entryId,
    BusinessId businessId,
    boolean income,
    Money amount,
    PaymentMethod paymentMethod,
    LocalDate occurredOn) {}
