package dev.assisfreitas.balcao.ledger.domain;

import dev.assisfreitas.balcao.shared.BusinessId;
import dev.assisfreitas.balcao.shared.Money;
import dev.assisfreitas.balcao.shared.PaymentMethod;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

/**
 * Aggregate root of the Ledger: one income or expense. Immutable by design: there are no setters
 * and no "edit". To fix a mistake you {@link #reverse reverse} the entry, which creates a new one
 * that cancels it, and the history keeps both.
 */
public final class Entry {

  public static final int DESCRIPTION_MAX_LENGTH = 140;

  private final EntryId id;
  private final BusinessId businessId;
  private final EntryType type;
  private final Money amount;
  private final String description;
  private final Category category;
  private final PaymentMethod paymentMethod;
  private final LocalDate occurredOn;
  private final Instant recordedAt;
  private final EntryId reversalOf;

  private Entry(
      EntryId id,
      BusinessId businessId,
      EntryType type,
      Money amount,
      String description,
      Category category,
      PaymentMethod paymentMethod,
      LocalDate occurredOn,
      Instant recordedAt,
      EntryId reversalOf) {
    this.id = Objects.requireNonNull(id);
    this.businessId = Objects.requireNonNull(businessId);
    this.type = Objects.requireNonNull(type);
    this.amount = Objects.requireNonNull(amount);
    this.description = Objects.requireNonNull(description);
    this.category = Objects.requireNonNull(category);
    this.paymentMethod = Objects.requireNonNull(paymentMethod);
    this.occurredOn = Objects.requireNonNull(occurredOn);
    this.recordedAt = Objects.requireNonNull(recordedAt);
    this.reversalOf = reversalOf;
  }

  /**
   * Records something that happened. {@code today} comes from the caller's clock, so the rule "no
   * future dates" is testable without touching the system time.
   */
  public static Entry record(
      EntryId id,
      BusinessId businessId,
      EntryType type,
      Money amount,
      String description,
      Category category,
      PaymentMethod paymentMethod,
      LocalDate occurredOn,
      LocalDate today,
      Instant now) {
    if (amount == null || !amount.isPositive()) {
      throw LedgerRuleViolation.amountMustBePositive();
    }
    if (occurredOn.isAfter(today)) {
      throw LedgerRuleViolation.dateInTheFuture();
    }
    return new Entry(
        id,
        businessId,
        type,
        amount,
        validDescription(description),
        category,
        paymentMethod,
        occurredOn,
        now,
        null);
  }

  /** Rebuilds an entry that was already validated when it was recorded. Persistence only. */
  public static Entry restore(
      EntryId id,
      BusinessId businessId,
      EntryType type,
      Money amount,
      String description,
      Category category,
      PaymentMethod paymentMethod,
      LocalDate occurredOn,
      Instant recordedAt,
      EntryId reversalOf) {
    return new Entry(
        id,
        businessId,
        type,
        amount,
        description,
        category,
        paymentMethod,
        occurredOn,
        recordedAt,
        reversalOf);
  }

  /**
   * Creates the entry that cancels this one: opposite type, same amount and category, dated today.
   * Whether this entry was already reversed is a fact about the whole ledger, so the use case
   * checks it (backed by a unique index).
   */
  public Entry reverse(EntryId reversalId, String reason, LocalDate today, Instant now) {
    if (isReversal()) {
      throw LedgerRuleViolation.cannotReverseAReversal();
    }
    String why = reason == null || reason.isBlank() ? description : reason.strip();
    return new Entry(
        reversalId,
        businessId,
        type.opposite(),
        amount,
        validDescription("Estorno: " + why),
        category,
        paymentMethod,
        today,
        now,
        id);
  }

  /** Positive for income, negative for expense: what this entry does to the balance. */
  public Money signedAmount() {
    return type == EntryType.INCOME ? amount : amount.negate();
  }

  public boolean isReversal() {
    return reversalOf != null;
  }

  private static String validDescription(String description) {
    String text = description == null ? "" : description.strip();
    if (text.isEmpty()) {
      throw LedgerRuleViolation.descriptionRequired();
    }
    if (text.length() > DESCRIPTION_MAX_LENGTH) {
      throw LedgerRuleViolation.descriptionTooLong();
    }
    return text;
  }

  public EntryId id() {
    return id;
  }

  public BusinessId businessId() {
    return businessId;
  }

  public EntryType type() {
    return type;
  }

  public Money amount() {
    return amount;
  }

  public String description() {
    return description;
  }

  public Category category() {
    return category;
  }

  public PaymentMethod paymentMethod() {
    return paymentMethod;
  }

  public LocalDate occurredOn() {
    return occurredOn;
  }

  public Instant recordedAt() {
    return recordedAt;
  }

  public Optional<EntryId> reversalOf() {
    return Optional.ofNullable(reversalOf);
  }
}
