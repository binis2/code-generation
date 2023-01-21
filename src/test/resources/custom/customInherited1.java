package net.binis.codegen;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.annotation.Default;
import net.binis.example.service.annotation.CodeExampleBuilder;

@CodeExampleBuilder("hello")
public interface TestPrototype {
    Long id();
}