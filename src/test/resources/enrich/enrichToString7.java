package net.binis.codegen;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.enrich.ToStringEnricher;
import net.binis.codegen.objects.Identifiable;
import net.binis.codegen.options.ToStringOnlyExplicitlyIncludedOption;

@CodePrototype(enrichers = {ToStringEnricher.class}, options = ToStringOnlyExplicitlyIncludedOption.class)
public interface TestPrototype extends Identifiable {


}