package net.binis.codegen;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.annotation.Default;
import net.binis.example.service.annotation.CodeExampleBuilder;

@CodeExampleBuilder
public interface TestPrototype {
    Long id();
}