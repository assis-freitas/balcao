package dev.assisfreitas.balcao.ledger.domain;

public enum EntryType {
  INCOME,
  EXPENSE;

  public EntryType opposite() {
    return this == INCOME ? EXPENSE : INCOME;
  }
}
