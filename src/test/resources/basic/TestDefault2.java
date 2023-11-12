package net.binis.codegen;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.test.TestTitle;

@CodePrototype
public interface TestPrototype extends TestTitle {

    String title();

    default String getTitle() {
        return "test" + title();
    }
}