package net.binis.codegen;

import net.binis.codegen.enrich.handler.CreatorModifierEnricher;
import net.binis.codegen.enrich.handler.AsEnricher;

@CodePrototype(base = true,
        enrichers = {AsEnricher.class},
        inheritedEnrichers = {CreatorModifierEnricher.class})
public interface BasePrototype {
    Long id();
}