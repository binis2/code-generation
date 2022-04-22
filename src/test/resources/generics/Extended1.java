package net.binis.codegen.test;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.enrich.ModifierEnricher;
import net.binis.codegen.objects.Payload;

@CodePrototype(generateImplementation = false, inheritedEnrichers = {ModifierEnricher.class})
public interface GenericPrototype<T extends Payload> {

    T payload();

}
