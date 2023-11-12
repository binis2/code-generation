package net.binis.codegen;

import net.binis.codegen.annotation.CodePrototype;

@CodePrototype
public interface TestPrototype extends BasicsTest.TestTitle {

    String title();

    default String getTitle() {
        return "test" + title();
    }
}