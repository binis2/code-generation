package net.binis.codegen.test.objects;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.annotation.type.GenerationStrategy;

public class TestParent {

    @CodePrototype(strategy = GenerationStrategy.IMPLEMENTATION)
    interface SubRequest {

        String value();

    }

}
