package net.binis.codegen;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.enrich.ToStringEnricher;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CodePrototype(enrichers = {ToStringEnricher.class}, base = true)
public interface BasePrototype {

    String baseTitle();
    List<Long> baseList();
    Set<Long> baseSet();
    Map<Long, String> baseMap();

}