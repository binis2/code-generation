package net.binis.codegen;

import net.binis.codegen.enrich.handler.CreatorModifierEnricher;
import net.binis.codegen.annotation.CodePrototype;

@CodePrototype(
        generateModifier = true,
        enrichers = {CreatorModifierEnricher.class})
public interface TestPrototype {
    String title();
}