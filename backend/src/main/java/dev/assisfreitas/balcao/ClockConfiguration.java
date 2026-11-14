package dev.assisfreitas.balcao;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * One clock for the whole app, in the shops' time zone: "today" for a shop in São Paulo must not
 * flip at 21:00 because the server runs on UTC. Tests replace it with a fixed clock.
 */
@Configuration
class ClockConfiguration {

  @Bean
  Clock clock() {
    return Clock.system(ZoneId.of("America/Sao_Paulo"));
  }
}
