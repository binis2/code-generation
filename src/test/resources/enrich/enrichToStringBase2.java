package net.binis.codegen;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.annotation.Ignore;
import net.binis.codegen.enrich.ToStringEnricher;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CodePrototype(enrichers = {ToStringEnricher.class}, base = true)
public interface BasePrototype {

    @Ignore(forToString = true)
    String baseTitle();
    List<Long> baseList();
    Set<Long> baseSet();
    @Ignore(forToString = true)
    Map<Long, String> baseMap();

}