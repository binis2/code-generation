package net.binis.codegen;

import net.binis.codegen.annotation.EnumPrototype;
import net.binis.codegen.enrich.field.GetterEnricher;

@EnumPrototype(enrichers = {GetterEnricher.class})
public enum TestEnumPrototype {

    ONE("One", 1, true),
    TWO("Two", 2, false);

    final String title;
    final int value;
    final boolean flag;

    String getTitle() {
        return "Test" +  title;
    }

}
