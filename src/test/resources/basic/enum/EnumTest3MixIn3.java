package net.binis.codegen;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.enrich.ModifierEnricher;
import net.binis.codegen.enrich.QueryEnricher;

@CodePrototype(enrichers = {ModifierEnricher.class, QueryEnricher.class})
public interface UseEnumPrototype {

    TestPrototype test();

    MixInPrototype mixIn();

    MixIn2Prototype mixIn2();

}