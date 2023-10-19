package net.binis.codegen;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.annotation.DefaultString;
import net.binis.codegen.enrich.ModifierEnricher;
import net.binis.codegen.enrich.TestAddFieldEnricher;

@CodePrototype(enrichers = {ModifierEnricher.class, TestAddFieldEnricher.class})
public interface TestPrototype {

    String title();
}