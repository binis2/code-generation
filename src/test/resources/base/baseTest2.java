package net.binis.codegen;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.enrich.handler.ModifierEnricher;

@CodePrototype(enrichers = {ModifierEnricher.class})
public interface TestPrototype extends BasePrototype {
    String title();
}