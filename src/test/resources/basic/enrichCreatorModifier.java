package net.binis.codegen;

import net.binis.codegen.enrich.handler.CreatorModifierEnricher;

@CodePrototype(
        generateModifier = true,
        enrichers = {CreatorModifierEnricher.class})
public interface TestPrototype {
    String title();
}