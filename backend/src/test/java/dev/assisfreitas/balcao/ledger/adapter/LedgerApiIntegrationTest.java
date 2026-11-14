package dev.assisfreitas.balcao.ledger.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import dev.assisfreitas.balcao.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/** The whole hexagon at once: HTTP in, Postgres out, Flyway schema, real SQL. */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class LedgerApiIntegrationTest {

  @Autowired MockMvcTester mvc;
  @Autowired JdbcClient jdbc;

  @BeforeEach
  void emptyLedger() {
    jdbc.sql("DELETE FROM ledger_entries WHERE reversal_of IS NOT NULL").update();
    jdbc.sql("DELETE FROM ledger_entries").update();
  }

  MvcTestResult record(String json) {
    return mvc.post()
        .uri("/api/entries")
        .contentType(MediaType.APPLICATION_JSON)
        .content(json)
        .exchange();
  }

  String recordedId(String json) {
    MvcTestResult result = record(json);
    assertThat(result).hasStatus(HttpStatus.CREATED);
    return result.getResponse().getHeader("Location").replace("/api/entries/", "");
  }

  @Test
  void recordsEntriesAndComputesTheMonthBalance() {
    recordedId(
        """
        {"type":"INCOME","amount":150.50,"description":"Venda balcão","category":"Vendas",
         "paymentMethod":"PIX","occurredOn":"2026-11-10"}
        """);
    recordedId(
        """
        {"type":"EXPENSE","amount":"40.25","description":"Conta de luz","category":"Contas",
         "paymentMethod":"BANK_SLIP","occurredOn":"2026-11-12"}
        """);
    recordedId(
        """
        {"type":"INCOME","amount":999,"description":"Outubro","category":"Vendas",
         "paymentMethod":"CASH","occurredOn":"2026-10-31"}
        """);

    assertThat(mvc.get().uri("/api/entries"))
        .hasStatusOk()
        .bodyJson()
        .satisfies(
            body -> {
              assertThat(body).extractingPath("$.from").isEqualTo("2026-11-01");
              assertThat(body).extractingPath("$.balance.income").isEqualTo(150.50);
              assertThat(body).extractingPath("$.balance.expense").isEqualTo(40.25);
              assertThat(body).extractingPath("$.balance.net").isEqualTo(110.25);
              assertThat(body).extractingPath("$.entries").asArray().hasSize(2);
              assertThat(body).extractingPath("$.entries[0].description").isEqualTo("Conta de luz");
            });
  }

  @Test
  void reversalZeroesTheEntryAndCanOnlyHappenOnce() {
    String sale =
        recordedId(
            """
            {"type":"INCOME","amount":80,"description":"Venda","category":"Vendas",
             "paymentMethod":"DEBIT_CARD","occurredOn":"2026-11-13"}
            """);

    assertThat(
            mvc.post()
                .uri("/api/entries/{id}/reversal", sale)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"Cliente desistiu\"}"))
        .hasStatus(HttpStatus.CREATED);
    assertThat(mvc.post().uri("/api/entries/{id}/reversal", sale))
        .hasStatus(HttpStatus.UNPROCESSABLE_CONTENT)
        .bodyJson()
        .extractingPath("$.detail")
        .isEqualTo("Este lançamento já foi estornado.");

    assertThat(mvc.get().uri("/api/entries"))
        .bodyJson()
        .satisfies(
            body -> {
              // The reversal cancels the income instead of showing up as an expense.
              assertThat(body).extractingPath("$.balance.income").isEqualTo(0.0);
              assertThat(body).extractingPath("$.balance.expense").isEqualTo(0.0);
              assertThat(body).extractingPath("$.balance.net").isEqualTo(0.0);
              assertThat(body)
                  .extractingPath("$.entries[0].description")
                  .isEqualTo("Estorno: Cliente desistiu");
              assertThat(body).extractingPath("$.entries[1].reversed").isEqualTo(true);
            });
  }

  @Test
  void businessRulesComeBackAs422WithAReadableMessage() {
    assertThat(
            record(
                """
                {"type":"INCOME","amount":10,"description":"Venda","category":"Vendas",
                 "paymentMethod":"PIX","occurredOn":"2026-11-15"}
                """))
        .hasStatus(HttpStatus.UNPROCESSABLE_CONTENT)
        .bodyJson()
        .extractingPath("$.detail")
        .isEqualTo("Não dá para lançar com data no futuro.");

    assertThat(
            record(
                """
                {"type":"INCOME","amount":10.999,"description":"Venda","category":"Vendas",
                 "paymentMethod":"PIX","occurredOn":"2026-11-14"}
                """))
        .hasStatus(HttpStatus.UNPROCESSABLE_CONTENT);
  }

  @Test
  void malformedRequestsAre400AndUnknownEntries404() {
    assertThat(record("{\"type\":\"INCOME\"}")).hasStatus(HttpStatus.BAD_REQUEST);
    assertThat(mvc.post().uri("/api/entries/{id}/reversal", "00000000-0000-0000-0000-00000000dead"))
        .hasStatus(HttpStatus.NOT_FOUND);
  }
}
