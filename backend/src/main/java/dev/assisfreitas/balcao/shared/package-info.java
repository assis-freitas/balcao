/**
 * Shared kernel: value types every context speaks the same way. Kept tiny on purpose; anything with
 * behavior specific to one context belongs in that context.
 */
@org.springframework.modulith.ApplicationModule(
    type = org.springframework.modulith.ApplicationModule.Type.OPEN)
package dev.assisfreitas.balcao.shared;
