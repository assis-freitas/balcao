package dev.assisfreitas.balcao.ledger.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.assisfreitas.balcao.shared.BusinessId;
import dev.assisfreitas.balcao.shared.Money;
import dev.assisfreitas.balcao.shared.PaymentMethod;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Pure domain: no Spring, no database, runs in milliseconds. */
class EntryTest {

  static final BusinessId SHOP = new BusinessId(UUID.randomUUID());
  static final LocalDate TODAY = LocalDate.of(2026, 11, 14);
  static final Instant NOW = Instant.parse("2026-11-14T18:00:00Z");

  static Entry sale(String amount, LocalDate on) {
    return Entry.record(
        EntryId.generate(),
        SHOP,
        EntryType.INCOME,
        Money.of(amount),
        "Venda balcão",
        new Category("Vendas"),
        PaymentMethod.PIX,
        on,
        TODAY,
        NOW);
  }

  @Test
  void recordsAnIncome() {
    Entry entry = sale("150.00", TODAY);

    assertThat(entry.signedAmount()).isEqualTo(Money.of("150"));
    assertThat(entry.isReversal()).isFalse();
  }

  @Test
  void refusesZeroOrNegativeAmounts() {
    assertThatThrownBy(() -> sale("0", TODAY)).hasMessageContaining("maior que zero");
    assertThatThrownBy(() -> sale("-5", TODAY)).hasMessageContaining("maior que zero");
  }

  @Test
  void refusesDatesInTheFutureButAcceptsThePast() {
    assertThatThrownBy(() -> sale("10", TODAY.plusDays(1))).hasMessageContaining("futuro");
    assertThat(sale("10", TODAY.minusMonths(2)).occurredOn()).isEqualTo(TODAY.minusMonths(2));
  }

  @Test
  void needsADescriptionAndACategory() {
    assertThatThrownBy(
            () ->
                Entry.record(
                    EntryId.generate(),
                    SHOP,
                    EntryType.EXPENSE,
                    Money.of("10"),
                    "   ",
                    new Category("Aluguel"),
                    PaymentMethod.CASH,
                    TODAY,
                    TODAY,
                    NOW))
        .hasMessageContaining("Descreva");
    assertThatThrownBy(() -> new Category(" ")).hasMessageContaining("categoria");
  }

  @Test
  void reversalCancelsTheOriginal() {
    Entry original = sale("150.00", TODAY.minusDays(3));

    Entry reversal = original.reverse(EntryId.generate(), "Cliente devolveu", TODAY, NOW);

    assertThat(reversal.type()).isEqualTo(EntryType.EXPENSE);
    assertThat(reversal.amount()).isEqualTo(original.amount());
    assertThat(reversal.category()).isEqualTo(original.category());
    assertThat(reversal.occurredOn()).isEqualTo(TODAY);
    assertThat(reversal.reversalOf()).contains(original.id());
    assertThat(reversal.description()).isEqualTo("Estorno: Cliente devolveu");
    assertThat(original.signedAmount().plus(reversal.signedAmount())).isEqualTo(Money.ZERO);
  }

  @Test
  void aReversalCannotBeReversed() {
    Entry reversal = sale("10", TODAY).reverse(EntryId.generate(), null, TODAY, NOW);

    assertThatThrownBy(() -> reversal.reverse(EntryId.generate(), null, TODAY, NOW))
        .isInstanceOf(LedgerRuleViolation.class)
        .hasMessageContaining("não pode ser estornado");
  }
}
