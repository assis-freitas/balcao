/**
 * Ledger (Lançamentos): every real that comes in or goes out of the shop.
 *
 * <p>Entries are immutable. A mistake is never edited or deleted: it is cancelled by a reversal
 * entry, so the history always adds up.
 *
 * <p>Public API of this module: the events in this package. Everything under {@code domain}, {@code
 * application} and {@code adapter} is internal.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Ledger")
package dev.assisfreitas.balcao.ledger;
