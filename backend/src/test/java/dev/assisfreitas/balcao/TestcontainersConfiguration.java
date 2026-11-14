package dev.assisfreitas.balcao;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

/** For integration tests: real Postgres plus a clock frozen on 14 Nov 2026, 12:00 in São Paulo. */
@TestConfiguration(proxyBeanMethods = false)
@Import(DevDatabase.class)
public class TestcontainersConfiguration {

  public static final Instant NOW = Instant.parse("2026-11-14T15:00:00Z");

  @Bean
  @Primary
  Clock fixedClock() {
    return Clock.fixed(NOW, ZoneId.of("America/Sao_Paulo"));
  }
}
