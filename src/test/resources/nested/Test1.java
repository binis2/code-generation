package net.binis.codegen.test;

import net.binis.codegen.annotation.builder.CodeBuilder;

public class AnnotationTest {

    @CodeBuilder
    public interface TestAnnotationPrototype {
        String value();
    }

}
