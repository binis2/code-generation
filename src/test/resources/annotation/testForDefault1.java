package net.binis.codegen.prototype;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.annotation.Keep;

@CodePrototype(generateImplementation = false)
public interface TestPrototype {

    boolean test();

    @Keep
    default boolean isTestable() {
        return test();
    }

}