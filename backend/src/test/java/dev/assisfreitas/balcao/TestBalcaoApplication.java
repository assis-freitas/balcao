package dev.assisfreitas.balcao;

import org.springframework.boot.SpringApplication;

/** Runs the app locally with Postgres in a container: {@code ./gradlew bootTestRun}. */
public class TestBalcaoApplication {

  public static void main(String[] args) {
    SpringApplication.from(BalcaoApplication::main).with(DevDatabase.class).run(args);
  }
}
