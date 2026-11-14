package dev.assisfreitas.balcao.ledger.adapter.in.web;

import dev.assisfreitas.balcao.ledger.application.port.in.EntryNotFound;
import dev.assisfreitas.balcao.ledger.application.port.in.LedgerQueries;
import dev.assisfreitas.balcao.ledger.application.port.in.RecordEntryUseCase;
import dev.assisfreitas.balcao.ledger.application.port.in.ReverseEntryUseCase;
import dev.assisfreitas.balcao.ledger.domain.EntryId;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Driving adapter: translates HTTP into use case calls and back. No business rules here. */
@RestController
@RequestMapping("/api/entries")
class EntryController {

  private final RecordEntryUseCase recordEntry;
  private final ReverseEntryUseCase reverseEntry;
  private final LedgerQueries queries;
  private final Clock clock;

  EntryController(
      RecordEntryUseCase recordEntry,
      ReverseEntryUseCase reverseEntry,
      LedgerQueries queries,
      Clock clock) {
    this.recordEntry = recordEntry;
    this.reverseEntry = reverseEntry;
    this.queries = queries;
    this.clock = clock;
  }

  /** Defaults to the current month. */
  @GetMapping
  StatementResponse statement(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
    YearMonth month = YearMonth.now(clock);
    return StatementResponse.from(
        queries.statement(
            from != null ? from : month.atDay(1), to != null ? to : month.atEndOfMonth()));
  }

  @PostMapping
  ResponseEntity<IdResponse> record(@Valid @RequestBody RecordEntryRequest request) {
    EntryId id = recordEntry.record(request.toCommand());
    return ResponseEntity.created(URI.create("/api/entries/" + id.value()))
        .body(new IdResponse(id.value()));
  }

  @PostMapping("/{id}/reversal")
  ResponseEntity<IdResponse> reverse(
      @PathVariable UUID id, @RequestBody(required = false) ReverseEntryRequest request) {
    EntryId reversal =
        reverseEntry.reverse(new EntryId(id), request == null ? null : request.reason());
    return ResponseEntity.status(HttpStatus.CREATED).body(new IdResponse(reversal.value()));
  }

  @ExceptionHandler(EntryNotFound.class)
  ProblemDetail notFound(EntryNotFound e) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Lançamento não encontrado.");
  }

  record IdResponse(UUID id) {}
}
