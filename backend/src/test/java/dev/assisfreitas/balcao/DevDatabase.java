package dev.assisfreitas.balcao;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

/** Postgres in a container, wired by @ServiceConnection. Used by tests and by bootTestRun. */
@TestConfiguration(proxyBeanMethods = false)
public class DevDatabase {

  @Bean
  @ServiceConnection
  PostgreSQLContainer postgres() {
    return new PostgreSQLContainer("postgres:17-alpine");
  }
}
