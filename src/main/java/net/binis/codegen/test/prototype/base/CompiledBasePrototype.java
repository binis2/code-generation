package net.binis.codegen.test.prototype.base;

import net.binis.codegen.annotation.CodePrototype;

@CodePrototype(base = true)
public interface CompiledBasePrototype {
    Long id();
}
