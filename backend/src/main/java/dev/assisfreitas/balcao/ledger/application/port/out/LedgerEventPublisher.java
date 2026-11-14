package dev.assisfreitas.balcao.ledger.application.port.out;

public interface LedgerEventPublisher {

  void publish(Object event);
}
