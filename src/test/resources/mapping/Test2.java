package net.binis.codegen;

import net.binis.codegen.annotation.builder.CodeBuilder;
import net.binis.codegen.annotation.Ignore;

@CodeBuilder(classSetters = true, interfaceSetters = true)
public interface TestPrototype {

    @Ignore(forMapper = true)
    String title();

}