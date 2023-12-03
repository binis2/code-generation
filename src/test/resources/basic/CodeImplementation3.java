package net.binis.codegen;

import net.binis.codegen.annotation.CodeImplementation;
import net.binis.codegen.annotation.CodePrototype;

@CodePrototype
public interface TestPrototype {

    int title();

    @CodeImplementation(value = "return Integer.toString(title)")
    default String getTitle() {
        return null;
    }
}