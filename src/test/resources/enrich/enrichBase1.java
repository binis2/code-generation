package net.binis.codegen;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.enrich.AsEnricher;
import net.binis.codegen.enrich.CreatorModifierEnricher;

@CodePrototype(base = true,
        enrichers = {AsEnricher.class},
        inheritedEnrichers = {CreatorModifierEnricher.class})
public interface BasePrototype {
    Long id();
}