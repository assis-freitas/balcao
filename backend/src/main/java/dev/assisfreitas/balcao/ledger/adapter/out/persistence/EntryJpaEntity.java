package dev.assisfreitas.balcao.ledger.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Persistence shape of an entry. Deliberately separate from the domain {@code Entry}: JPA wants a
 * no-arg constructor and mutable fields, the domain wants neither.
 */
@Entity
@Table(name = "ledger_entries")
class EntryJpaEntity {

  @Id UUID id;

  @Column(name = "business_id", nullable = false)
  UUID businessId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  TypeColumn type;

  @Column(name = "amount_cents", nullable = false)
  long amountCents;

  @Column(nullable = false)
  String description;

  @Column(nullable = false)
  String category;

  @Column(name = "payment_method", nullable = false)
  String paymentMethod;

  @Column(name = "occurred_on", nullable = false)
  LocalDate occurredOn;

  @Column(name = "recorded_at", nullable = false)
  Instant recordedAt;

  @Column(name = "reversal_of")
  UUID reversalOf;

  protected EntryJpaEntity() {}

  enum TypeColumn {
    INCOME,
    EXPENSE
  }
}
