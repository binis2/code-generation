package net.binis.codegen;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.annotation.DefaultString;

@CodePrototype
public interface TestPrototype {

    int title();

    default String getTitle() {
        return Integer.toString(title());
    }
}