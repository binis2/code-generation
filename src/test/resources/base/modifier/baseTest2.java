package net.binis.codegen;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.enrich.ModifierEnricher;
import net.binis.codegen.spring.AsyncEntityModifier;

@CodePrototype(baseModifierClass = AsyncEntityModifier.class, enrichers = {ModifierEnricher.class})
public interface TestPrototype {
    String title();
}