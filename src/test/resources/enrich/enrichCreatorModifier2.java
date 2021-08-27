package net.binis.codegen;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.enrich.CreatorModifierEnricher;

@CodePrototype(enrichers = {CreatorModifierEnricher.class})
public interface TestPrototype {
    String title();
}