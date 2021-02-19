package net.binis.codegen;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.enrich.handler.CreatorEnricher;

@CodePrototype(enrichers = {CreatorEnricher.class})
public interface TestPrototype {
    String title();
}