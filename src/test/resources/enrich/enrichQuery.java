package net.binis.codegen;

import net.binis.codegen.enrich.handler.QueryEnricher;
import net.binis.codegen.annotation.CodePrototype;

@CodePrototype(enrichers = {QueryEnricher.class})
public interface TestPrototype {
    String title();
}