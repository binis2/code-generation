package net.binis.codegen;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.enrich.handler.AsEnricher;

@CodePrototype(enrichers = {AsEnricher.class})
public interface TestPrototype {
    String title();
}