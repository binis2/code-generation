package net.binis.codegen.proto.base;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.enrich.RedisEnricher;

import java.util.List;

@CodePrototype
public interface InheirTestPrototype {
    String title();

    int data();

    List<String> list();

}