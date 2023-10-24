package net.binis.test.prototype;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.enrich.CreatorModifierEnricher;
import net.binis.codegen.enrich.ModifierEnricher;
import net.binis.codegen.enrich.RegionEnricher;
import net.binis.codegen.objects.Typeable;
import net.binis.test.prototype.enums.TestEnumPrototype;

@CodePrototype(interfaceSetters = false, classSetters = false, enrichers = {CreatorModifierEnricher.class, ModifierEnricher.class, RegionEnricher.class})
public interface EnumUsingTestPrototype extends Typeable<TestEnumPrototype> {

}