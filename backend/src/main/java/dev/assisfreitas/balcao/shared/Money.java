package dev.assisfreitas.balcao.shared;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * An amount in Brazilian reais, stored as whole cents. Never a double: R$ 0,10 + R$ 0,20 must be
 * exactly R$ 0,30.
 */
public record Money(long cents) implements Comparable<Money> {

  public static final Money ZERO = new Money(0);

  public static Money ofCents(long cents) {
    return new Money(cents);
  }

  /** Accepts at most two decimal places; "10.005" is a mistake, not something to round. */
  public static Money of(BigDecimal reais) {
    if (reais == null) {
      throw new IllegalArgumentException("Amount is required");
    }
    if (reais.stripTrailingZeros().scale() > 2) {
      throw new InvalidMoney("Use no máximo dois dígitos depois da vírgula.");
    }
    return new Money(reais.movePointRight(2).longValueExact());
  }

  public static Money of(String reais) {
    return of(new BigDecimal(reais));
  }

  public Money plus(Money other) {
    return new Money(Math.addExact(cents, other.cents));
  }

  public Money minus(Money other) {
    return new Money(Math.subtractExact(cents, other.cents));
  }

  public Money negate() {
    return new Money(Math.negateExact(cents));
  }

  public boolean isPositive() {
    return cents > 0;
  }

  public BigDecimal toBigDecimal() {
    return BigDecimal.valueOf(cents, 2).setScale(2, RoundingMode.UNNECESSARY);
  }

  @Override
  public int compareTo(Money other) {
    return Long.compare(cents, other.cents);
  }

  @Override
  public String toString() {
    return "R$ " + toBigDecimal();
  }

  public static final class InvalidMoney extends BusinessRuleViolation {
    InvalidMoney(String message) {
      super(message);
    }
  }
}
