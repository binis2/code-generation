package net.binis.codegen;

import net.binis.codegen.enrich.handler.CreatorModifierEnricher;
import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.enrich.handler.ModifierEnricher;

@CodePrototype(
        enrichers = {CreatorModifierEnricher.class, ModifierEnricher.class})
public interface TestPrototype {
    String title();
}