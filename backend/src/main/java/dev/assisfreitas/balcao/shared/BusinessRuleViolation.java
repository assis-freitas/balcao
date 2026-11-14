package dev.assisfreitas.balcao.shared;

/**
 * A broken business rule. Messages are written for the shop owner (pt-BR), because the web adapter
 * shows them as they are.
 */
public abstract class BusinessRuleViolation extends RuntimeException {

  protected BusinessRuleViolation(String message) {
    super(message);
  }
}
