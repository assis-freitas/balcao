package dev.assisfreitas.balcao.ledger.adapter.out.events;

import dev.assisfreitas.balcao.ledger.application.port.out.LedgerEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Publishes through Spring, so Spring Modulith can deliver the events to other modules after the
 * transaction commits.
 */
@Component
class SpringLedgerEventPublisher implements LedgerEventPublisher {

  private final ApplicationEventPublisher publisher;

  SpringLedgerEventPublisher(ApplicationEventPublisher publisher) {
    this.publisher = publisher;
  }

  @Override
  public void publish(Object event) {
    publisher.publishEvent(event);
  }
}
