package dev.assisfreitas.balcao.ledger.domain;

import dev.assisfreitas.balcao.shared.BusinessRuleViolation;

public final class LedgerRuleViolation extends BusinessRuleViolation {

  private LedgerRuleViolation(String message) {
    super(message);
  }

  static LedgerRuleViolation amountMustBePositive() {
    return new LedgerRuleViolation("O valor precisa ser maior que zero.");
  }

  static LedgerRuleViolation dateInTheFuture() {
    return new LedgerRuleViolation("Não dá para lançar com data no futuro.");
  }

  static LedgerRuleViolation descriptionRequired() {
    return new LedgerRuleViolation("Descreva o lançamento.");
  }

  static LedgerRuleViolation descriptionTooLong() {
    return new LedgerRuleViolation(
        "A descrição pode ter no máximo " + Entry.DESCRIPTION_MAX_LENGTH + " caracteres.");
  }

  static LedgerRuleViolation categoryRequired() {
    return new LedgerRuleViolation("Escolha uma categoria.");
  }

  static LedgerRuleViolation categoryTooLong() {
    return new LedgerRuleViolation(
        "A categoria pode ter no máximo " + Category.MAX_LENGTH + " caracteres.");
  }

  static LedgerRuleViolation cannotReverseAReversal() {
    return new LedgerRuleViolation("Um estorno não pode ser estornado. Lance de novo, se preciso.");
  }

  public static LedgerRuleViolation alreadyReversed() {
    return new LedgerRuleViolation("Este lançamento já foi estornado.");
  }
}
