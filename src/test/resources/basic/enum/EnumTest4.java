package net.binis.codegen;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.enrich.ModifierEnricher;
import net.binis.codegen.enrich.QueryEnricher;

import java.util.List;

@CodePrototype(enrichers = {ModifierEnricher.class, QueryEnricher.class})
public interface TestDataPrototype {

    List<TestPrototype> getTests();

}