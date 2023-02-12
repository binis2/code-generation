package net.binis.codegen.redis;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.enrich.RedisEnricher;
import net.binis.codegen.enrich.RegionEnricher;

import java.util.List;

@CodePrototype(interfaceSetters = false, enrichers = RedisEnricher.class)
public interface RedisTestPrototype {
    String title();

    int data();

    List<String> list();

}