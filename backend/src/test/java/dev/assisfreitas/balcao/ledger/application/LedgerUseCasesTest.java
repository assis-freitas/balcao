package dev.assisfreitas.balcao.ledger.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.assisfreitas.balcao.ledger.EntryRecorded;
import dev.assisfreitas.balcao.ledger.EntryReversed;
import dev.assisfreitas.balcao.ledger.application.port.in.EntryNotFound;
import dev.assisfreitas.balcao.ledger.application.port.in.RecordEntryUseCase;
import dev.assisfreitas.balcao.ledger.domain.EntryId;
import dev.assisfreitas.balcao.ledger.domain.EntryType;
import dev.assisfreitas.balcao.ledger.domain.LedgerRuleViolation;
import dev.assisfreitas.balcao.shared.BusinessId;
import dev.assisfreitas.balcao.shared.Money;
import dev.assisfreitas.balcao.shared.PaymentMethod;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Use cases with every outgoing port faked: no Spring, no database. */
class LedgerUseCasesTest {

  final BusinessId shop = new BusinessId(UUID.randomUUID());
  final InMemoryEntryRepository entries = new InMemoryEntryRepository();
  final List<Object> published = new ArrayList<>();
  final Clock clock =
      Clock.fixed(Instant.parse("2026-11-14T15:00:00Z"), ZoneId.of("America/Sao_Paulo"));

  final RecordEntryService record =
      new RecordEntryService(entries, () -> shop, published::add, clock);
  final ReverseEntryService reverse =
      new ReverseEntryService(entries, () -> shop, published::add, clock);

  EntryId sell(String amount, PaymentMethod method) {
    return record.record(
        new RecordEntryUseCase.Command(
            EntryType.INCOME,
            Money.of(amount),
            "Venda",
            "Vendas",
            method,
            LocalDate.of(2026, 11, 10)));
  }

  @Test
  void recordingSavesTheEntryForTheCurrentShopAndPublishesAnEvent() {
    EntryId id = sell("89.90", PaymentMethod.CREDIT_CARD);

    assertThat(entries.saved)
        .singleElement()
        .satisfies(
            e -> {
              assertThat(e.id()).isEqualTo(id);
              assertThat(e.businessId()).isEqualTo(shop);
              assertThat(e.recordedAt()).isEqualTo(clock.instant());
            });
    assertThat(published)
        .singleElement()
        .isEqualTo(
            new EntryRecorded(
                id.value(),
                shop,
                true,
                Money.of("89.90"),
                PaymentMethod.CREDIT_CARD,
                LocalDate.of(2026, 11, 10)));
  }

  @Test
  void reversingRecordsACounterEntryDatedTodayInTheShopsTimeZone() {
    EntryId sale = sell("50", PaymentMethod.CASH);
    published.clear();

    EntryId reversal = reverse.reverse(sale, "Troco errado");

    var counter = entries.findById(shop, reversal).orElseThrow();
    assertThat(counter.type()).isEqualTo(EntryType.EXPENSE);
    assertThat(counter.occurredOn()).isEqualTo(LocalDate.of(2026, 11, 14));
    assertThat(published).containsExactly(new EntryReversed(reversal.value(), sale.value(), shop));
  }

  @Test
  void anEntryIsReversedOnlyOnce() {
    EntryId sale = sell("50", PaymentMethod.CASH);
    reverse.reverse(sale, null);

    assertThatThrownBy(() -> reverse.reverse(sale, null))
        .isInstanceOf(LedgerRuleViolation.class)
        .hasMessageContaining("já foi estornado");
  }

  @Test
  void anotherShopsEntryIsNotFound() {
    EntryId sale = sell("50", PaymentMethod.CASH);
    var otherShop =
        new ReverseEntryService(
            entries, () -> new BusinessId(UUID.randomUUID()), published::add, clock);

    assertThatThrownBy(() -> otherShop.reverse(sale, null)).isInstanceOf(EntryNotFound.class);
  }
}
