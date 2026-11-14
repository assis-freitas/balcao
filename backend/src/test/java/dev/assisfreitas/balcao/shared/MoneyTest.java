package dev.assisfreitas.balcao.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MoneyTest {

  @Test
  void addsExactlyWithoutFloatingPointDrift() {
    assertThat(Money.of("0.10").plus(Money.of("0.20"))).isEqualTo(Money.of("0.30"));
  }

  @Test
  void keepsCentsAndPrintsTwoDecimals() {
    assertThat(Money.of(new BigDecimal("12.5")).cents()).isEqualTo(1250);
    assertThat(Money.ofCents(1250).toBigDecimal()).isEqualByComparingTo("12.50");
    assertThat(Money.ofCents(1250).toBigDecimal().scale()).isEqualTo(2);
  }

  @Test
  void refusesFractionsOfACentInsteadOfRounding() {
    assertThatThrownBy(() -> Money.of("10.005"))
        .isInstanceOf(BusinessRuleViolation.class)
        .hasMessageContaining("dois dígitos");
  }

  @Test
  void acceptsTrailingZerosBeyondTwoDecimals() {
    assertThat(Money.of("10.500")).isEqualTo(Money.ofCents(1050));
  }

  @Test
  void subtractsAndNegates() {
    assertThat(Money.of("10").minus(Money.of("25")).negate()).isEqualTo(Money.of("15"));
    assertThat(Money.ZERO.isPositive()).isFalse();
  }
}
