package net.binis.codegen;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.enrich.ToStringEnricher;
import net.binis.codegen.options.ToStringOnlyExplicitlyIncludedOption;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CodePrototype(enrichers = {ToStringEnricher.class}, options = ToStringOnlyExplicitlyIncludedOption.class, base = true)
public interface BasePrototype {

    String baseTitle();

    List<Long> baseList();

    Set<Long> baseSet();

    Map<Long, String> baseMap();

}