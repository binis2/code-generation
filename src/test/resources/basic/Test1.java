package net.binis.codegen;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.annotation.DefaultString;

@CodePrototype
public interface TestPrototype {

    @DefaultString("asd")
    String title();
}