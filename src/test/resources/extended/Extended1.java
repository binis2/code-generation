package net.binis.codegen.test;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.annotation.Default;

@CodePrototype(interfaceSetters = false)
public interface ExtendedPrototype {

    @Default("\"asd\"")
    String extended();

    default String getExtended() {
        return extended();
    }

    default String getDoubleExtended() {
        return extended() + extended();
    }

}
