package net.binis.codegen;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.enrich.ToStringEnricher;
import net.binis.codegen.options.ToStringFullCollectionInfoOption;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CodePrototype(enrichers = {ToStringEnricher.class}, options = ToStringFullCollectionInfoOption.class)
public interface TestPrototype extends BasePrototype {

    String title();
    List<Long> list();
    Set<Long> set();
    Map<Long, String> map();

}