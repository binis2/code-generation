package net.binis.codegen;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.annotation.Default;
import net.binis.codegen.annotation.type.GenerationStrategy;
import net.binis.codegen.objects.CompiledGeneric;

@CodePrototype(strategy = GenerationStrategy.IMPLEMENTATION)
public interface TestStrategy extends CompiledGeneric<String> {
    void test();

    String getPayload();

}