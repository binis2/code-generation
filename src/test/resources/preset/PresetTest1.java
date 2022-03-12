package net.binis.codegen;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.enrich.QueryEnricher;
import net.binis.codegen.enrich.RegionEnricher;
import net.binis.codegen.spring.annotation.QueryPreset;
import net.binis.codegen.spring.query.Preset;

import java.util.List;

@CodePrototype(enrichers = {QueryEnricher.class, RegionEnricher.class})
public interface PresetTestPrototype {
    String title();
    int data();
    PresetTestPrototype parent();
    List<String> list();

    @QueryPreset
    default void queryTitle(String title, int data) {
        Preset.declare()
                .field(title()).contains(title).and()
                .field(data(), data);
    }

    @QueryPreset
    default void queryTitleString() {
        Preset.declare()
                .field(title(), "title");
    }

    @QueryPreset
    default void queryPrototype(PresetTestPrototype parent) {
        Preset.declare()
                .field(parent(), parent).and()
                .prototype(parent()).field(title()).isNull().and()
                .collection(list()).isEmpty();
    }

    @QueryPreset
    default String queryString(String title, PresetTestPrototype parent, int data) {
        return "title(title).and().parent(parent).and().data().greater(data)";
    }

}