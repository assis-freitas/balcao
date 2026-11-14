package dev.assisfreitas.balcao.ledger.application.port.out;

import dev.assisfreitas.balcao.shared.BusinessId;

/** Which shop the current request acts for. Will come from the logged-in user. */
public interface CurrentBusiness {

  BusinessId id();
}
