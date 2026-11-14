package dev.assisfreitas.balcao.ledger.application;

import dev.assisfreitas.balcao.ledger.application.port.in.LedgerQueries;
import dev.assisfreitas.balcao.ledger.application.port.out.CurrentBusiness;
import dev.assisfreitas.balcao.ledger.application.port.out.LedgerReadModel;
import java.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

public class LedgerQueryService implements LedgerQueries {

  private final LedgerReadModel readModel;
  private final CurrentBusiness business;

  public LedgerQueryService(LedgerReadModel readModel, CurrentBusiness business) {
    this.readModel = readModel;
    this.business = business;
  }

  @Override
  @Transactional(readOnly = true)
  public Statement statement(LocalDate from, LocalDate to) {
    if (to.isBefore(from)) {
      throw new IllegalArgumentException("'to' must not be before 'from'");
    }
    var id = business.id();
    return new Statement(
        from, to, readModel.balance(id, from, to), readModel.entries(id, from, to));
  }
}
