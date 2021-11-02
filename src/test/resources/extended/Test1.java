package net.binis.codegen.test;

import net.binis.codegen.annotation.builder.CodeBuilder;

@CodeBuilder
public interface TestAnnotationPrototype extends ExtendedPrototype {
    String value();
}
