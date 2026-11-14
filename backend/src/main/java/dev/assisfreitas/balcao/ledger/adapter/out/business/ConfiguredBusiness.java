package dev.assisfreitas.balcao.ledger.adapter.out.business;

import dev.assisfreitas.balcao.ledger.application.port.out.CurrentBusiness;
import dev.assisfreitas.balcao.shared.BusinessId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Until the Access context exists, every request acts for one shop set in configuration. Swapping
 * this adapter for one that reads the logged-in user is the only change the Ledger will need.
 */
@Component
class ConfiguredBusiness implements CurrentBusiness {

  private final BusinessId id;

  ConfiguredBusiness(@Value("${balcao.business-id}") String id) {
    this.id = BusinessId.of(id);
  }

  @Override
  public BusinessId id() {
    return id;
  }
}
