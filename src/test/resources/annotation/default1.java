package net.binis.codegen;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.annotation.Default;

@CodePrototype
public interface TestPrototype {
    @Default("1L")
    Long id();
}