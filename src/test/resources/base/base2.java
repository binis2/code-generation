package net.binis.codegen;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.enrich.ModifierEnricher;

@CodePrototype(base = true, inheritedEnrichers = {ModifierEnricher.class}, enrichers = {ModifierEnricher.class})
public interface BasePrototype {
    Long id();
}