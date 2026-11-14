package dev.assisfreitas.balcao;

import dev.assisfreitas.balcao.shared.BusinessRuleViolation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** A broken business rule is the user's to fix: 422 with a message they can read. */
@RestControllerAdvice
class ApiErrorHandler {

  @ExceptionHandler(BusinessRuleViolation.class)
  ProblemDetail businessRule(BusinessRuleViolation e) {
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage());
    problem.setTitle("Regra de negócio");
    return problem;
  }
}
