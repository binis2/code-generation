package net.binis.codegen;

import net.binis.codegen.annotation.CodePrototype;

@CodePrototype
public interface TestPrototype {

    boolean title();

    default boolean isTitle() {
        return !title();
    }
}