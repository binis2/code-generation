package net.binis.test.cards.prototype.payload;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.enrich.CreatorModifierEnricher;
import net.binis.codegen.enrich.ModifierEnricher;
import net.binis.codegen.enrich.RegionEnricher;
import net.binis.codegen.objects.Payload;

@CodePrototype(interfaceSetters = false, classSetters = false, enrichers = {CreatorModifierEnricher.class, ModifierEnricher.class, RegionEnricher.class})
public interface AccountOverviewCardPayloadPrototype extends Payload {
    int raised();

    int donated();

    int matching();
}