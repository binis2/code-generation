package net.binis.codegen;

import net.binis.codegen.enrich.CreatorModifierEnricher;
import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.enrich.ModifierEnricher;

@CodePrototype(
        enrichers = {CreatorModifierEnricher.class, ModifierEnricher.class})
public interface TestPrototype {
    String title();
}