package net.binis.codegen;

import net.binis.codegen.annotation.EnumPrototype;
import net.binis.codegen.annotation.Ordinal;
import net.binis.codegen.enrich.field.GetterEnricher;

@EnumPrototype(enrichers = {GetterEnricher.class})
public enum TestEnumPrototype {

    @Ordinal(5)
    ONE,
    @Ordinal(value = Integer.MAX_VALUE)
    TWO;

}
