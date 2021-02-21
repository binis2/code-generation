package net.binis.codegen;

import net.binis.codegen.enrich.handler.CreatorModifierEnricher;
import net.binis.codegen.annotation.CodePrototype;

@CodePrototype(
        generateModifier = true,
        enrichers = {CreatorModifierEnricher.class},
        mixInClass = TestPrototype.class)
public interface MixInPrototype extends TestPrototype {
    String subtitle();
}