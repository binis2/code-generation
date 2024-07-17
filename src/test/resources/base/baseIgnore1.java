package net.binis.codegen;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.annotation.Ignore;

@CodePrototype(base = true)
public interface BasePrototype {
    @Ignore(forSerialization = true)
    Long id();
}