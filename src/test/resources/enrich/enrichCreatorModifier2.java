package net.binis.codegen;

import net.binis.codegen.enrich.handler.CreatorModifierEnricher;
import net.binis.codegen.annotation.CodePrototype;

@CodePrototype(enrichers = {CreatorModifierEnricher.class})
public interface TestPrototype {
    String title();
}