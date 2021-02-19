package net.binis.codegen;

import net.binis.codegen.annotation.CodePrototype;

@CodePrototype(generateModifier = true)
public interface TestPrototype extends BasePrototype {
    String title();
}