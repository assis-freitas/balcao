package dev.assisfreitas.balcao.shared;

/**
 * How money moved. Shared because other contexts react to it: a credit card sale becomes a
 * receivable, cash goes through the till.
 */
public enum PaymentMethod {
  CASH,
  PIX,
  DEBIT_CARD,
  CREDIT_CARD,
  BANK_SLIP
}
