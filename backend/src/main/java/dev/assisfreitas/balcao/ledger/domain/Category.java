package dev.assisfreitas.balcao.ledger.domain;

/** What the money was for: "Vendas", "Aluguel", "Fornecedores"... */
public record Category(String name) {

  public static final int MAX_LENGTH = 40;

  public Category {
    name = name == null ? "" : name.strip();
    if (name.isEmpty()) {
      throw LedgerRuleViolation.categoryRequired();
    }
    if (name.length() > MAX_LENGTH) {
      throw LedgerRuleViolation.categoryTooLong();
    }
  }
}
