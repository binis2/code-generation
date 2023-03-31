package net.binis.codegen;

import net.binis.codegen.annotation.EnumPrototype;
import net.binis.codegen.enrich.TestEnumEnricher;

@EnumPrototype(enrichers = TestEnumEnricher.class)
public enum TestPrototype {
    UNKNOWN,
    KNOWN,
    NEXT;
}