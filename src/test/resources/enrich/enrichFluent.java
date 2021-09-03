package net.binis.codegen;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.enrich.FluentEnricher;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CodePrototype(enrichers = {FluentEnricher.class}, interfaceSetters = false, classSetters = false)
public interface TestPrototype {

    String title();
    List<Long> list();
    Set<Long> set();
    Map<Long, String> map();

}