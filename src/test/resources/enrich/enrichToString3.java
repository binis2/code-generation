package net.binis.codegen;

import net.binis.codegen.enrich.ToStringEnricher;
import net.binis.codegen.options.ToStringFullCollectionInfoOption;
import net.binis.codegen.annotation.EnumPrototype;

@EnumPrototype(enrichers = {ToStringEnricher.class}, options = ToStringFullCollectionInfoOption.class)
public enum TestPrototype {

    ONE("One"),
    TWO("Two");

    private String title;

    TestPrototype(String title) {
        this.title = title;
    }

}