package dev.assisfreitas.balcao.ledger.adapter.config;

import dev.assisfreitas.balcao.ledger.application.LedgerQueryService;
import dev.assisfreitas.balcao.ledger.application.RecordEntryService;
import dev.assisfreitas.balcao.ledger.application.ReverseEntryService;
import dev.assisfreitas.balcao.ledger.application.port.out.CurrentBusiness;
import dev.assisfreitas.balcao.ledger.application.port.out.EntryRepository;
import dev.assisfreitas.balcao.ledger.application.port.out.LedgerEventPublisher;
import dev.assisfreitas.balcao.ledger.application.port.out.LedgerReadModel;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the use cases. They are plain classes with no Spring stereotype, so this is the one place
 * that knows which adapter plugs into which port.
 */
@Configuration
class LedgerConfiguration {

  @Bean
  RecordEntryService recordEntryService(
      EntryRepository entries, CurrentBusiness business, LedgerEventPublisher events, Clock clock) {
    return new RecordEntryService(entries, business, events, clock);
  }

  @Bean
  ReverseEntryService reverseEntryService(
      EntryRepository entries, CurrentBusiness business, LedgerEventPublisher events, Clock clock) {
    return new ReverseEntryService(entries, business, events, clock);
  }

  @Bean
  LedgerQueryService ledgerQueryService(LedgerReadModel readModel, CurrentBusiness business) {
    return new LedgerQueryService(readModel, business);
  }
}
