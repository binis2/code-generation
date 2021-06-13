package net.binis.codegen;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.enrich.handler.ModifierEnricher;
import net.binis.codegen.spring.BaseEntityModifier;

@CodePrototype(baseModifierClass = BaseEntityModifier.class, enrichers = {ModifierEnricher.class})
public interface TestPrototype {
    String title();
}