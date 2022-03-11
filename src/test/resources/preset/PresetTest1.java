package net.binis.codegen;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.enrich.QueryEnricher;
import net.binis.codegen.enrich.RegionEnricher;
import net.binis.codegen.spring.annotation.QueryPreset;
import net.binis.codegen.spring.query.Preset;
import static net.binis.codegen.spring.query.Preset.param;

@CodePrototype(enrichers = {QueryEnricher.class, RegionEnricher.class})
public interface PresetTestPrototype {
    String title();
    int data();

    @QueryPreset
    default void queryTitle() {
        Preset.declare()
                .field(title()).contains(param()).and()
                .field(data(), param());
    }

    @QueryPreset
    default void queryTitleString() {
        Preset.declare()
                .field(title(), "title");
    }

    @QueryPreset
    default void queryPrototype() {
        Preset.declare()
                .field(title(), "title");
    }


}