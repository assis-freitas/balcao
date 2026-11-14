package dev.assisfreitas.balcao;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/** Bounded contexts as Spring Modulith modules: no cycles, no reaching into internals. */
class ModularityTest {

  static final ApplicationModules modules = ApplicationModules.of(BalcaoApplication.class);

  @Test
  void modulesRespectTheirBoundaries() {
    modules.verify();
  }

  @Test
  void writesModuleDiagrams() {
    new Documenter(modules).writeModulesAsPlantUml().writeIndividualModulesAsPlantUml();
  }
}
