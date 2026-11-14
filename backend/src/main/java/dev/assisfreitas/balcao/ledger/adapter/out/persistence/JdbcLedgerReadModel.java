package dev.assisfreitas.balcao.ledger.adapter.out.persistence;

import dev.assisfreitas.balcao.ledger.application.port.in.LedgerQueries.Balance;
import dev.assisfreitas.balcao.ledger.application.port.in.LedgerQueries.EntryView;
import dev.assisfreitas.balcao.ledger.application.port.out.LedgerReadModel;
import dev.assisfreitas.balcao.ledger.domain.EntryType;
import dev.assisfreitas.balcao.shared.BusinessId;
import dev.assisfreitas.balcao.shared.Money;
import dev.assisfreitas.balcao.shared.PaymentMethod;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/** Read side: plain SQL shaped for the screens, no aggregates loaded. */
@Component
class JdbcLedgerReadModel implements LedgerReadModel {

  private final JdbcClient jdbc;

  JdbcLedgerReadModel(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public List<EntryView> entries(BusinessId businessId, LocalDate from, LocalDate to) {
    return jdbc.sql(
            """
            SELECT e.id, e.type, e.amount_cents, e.description, e.category, e.payment_method,
                   e.occurred_on, e.reversal_of,
                   EXISTS (SELECT 1 FROM ledger_entries r WHERE r.reversal_of = e.id) AS reversed
              FROM ledger_entries e
             WHERE e.business_id = :business AND e.occurred_on BETWEEN :from AND :to
             ORDER BY e.occurred_on DESC, e.recorded_at DESC
            """)
        .param("business", businessId.value())
        .param("from", from)
        .param("to", to)
        .query(
            (rs, i) ->
                new EntryView(
                    rs.getObject("id", UUID.class),
                    EntryType.valueOf(rs.getString("type")),
                    Money.ofCents(rs.getLong("amount_cents")),
                    rs.getString("description"),
                    rs.getString("category"),
                    PaymentMethod.valueOf(rs.getString("payment_method")),
                    rs.getObject("occurred_on", LocalDate.class),
                    rs.getObject("reversal_of", UUID.class),
                    rs.getBoolean("reversed")))
        .list();
  }

  @Override
  public Balance balance(BusinessId businessId, LocalDate from, LocalDate to) {
    return jdbc.sql(
            """
            -- A reversal shrinks the total of the entry it cancels instead of inflating the
            -- opposite one: reversing a R$ 50 expense means R$ 50 less in expenses, not
            -- R$ 50 more in income. The net is the same either way.
            SELECT COALESCE(SUM(CASE
                     WHEN type = 'INCOME' AND reversal_of IS NULL THEN amount_cents
                     WHEN type = 'EXPENSE' AND reversal_of IS NOT NULL THEN -amount_cents
                   END), 0) AS income,
                   COALESCE(SUM(CASE
                     WHEN type = 'EXPENSE' AND reversal_of IS NULL THEN amount_cents
                     WHEN type = 'INCOME' AND reversal_of IS NOT NULL THEN -amount_cents
                   END), 0) AS expense
              FROM ledger_entries
             WHERE business_id = :business AND occurred_on BETWEEN :from AND :to
            """)
        .param("business", businessId.value())
        .param("from", from)
        .param("to", to)
        .query(
            (rs, i) ->
                new Balance(
                    Money.ofCents(rs.getLong("income")), Money.ofCents(rs.getLong("expense"))))
        .single();
  }
}
