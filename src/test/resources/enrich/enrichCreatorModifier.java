package net.binis.codegen;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.enrich.CreatorModifierEnricher;
import net.binis.codegen.enrich.ModifierEnricher;
import net.binis.codegen.enrich.RegionEnricher;

@CodePrototype(
        enrichers = {CreatorModifierEnricher.class, ModifierEnricher.class, RegionEnricher.class})
public interface TestPrototype {
    String title();
}