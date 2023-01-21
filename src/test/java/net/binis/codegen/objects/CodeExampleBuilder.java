package net.binis.codegen.objects;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.annotation.type.GenerationStrategy;
import net.binis.codegen.enrich.RegionEnricher;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@CodePrototype(strategy = GenerationStrategy.IMPLEMENTATION, enrichers = RegionEnricher.class)
public @interface CodeExampleBuilder {
    String value();
}