package net.binis.codegen.something;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.enrich.ModifierEnricher;
import net.binis.codegen.objects.prototype.BaseCompiledWithExternalGenericPrototype;

@CodePrototype(enrichers = ModifierEnricher.class)
public interface TestPrototype extends BaseCompiledWithExternalGenericPrototype {

    long id();

}