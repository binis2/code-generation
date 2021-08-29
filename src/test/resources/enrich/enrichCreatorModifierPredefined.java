package net.binis.codegen;

import net.binis.codegen.annotation.builder.CodeBuilder;
import net.binis.codegen.enrich.CreatorModifierEnricher;
import net.binis.codegen.enrich.ModifierEnricher;

@CodeBuilder(
        enrichers = {ModifierEnricher.class}, interfaceSetters = true, classSetters = true)
public interface TestPrototype {
    String title();
}