package net.binis.codegen.jackson;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.annotation.EnumPrototype;
import net.binis.codegen.annotation.builder.CodeBuilder;
import net.binis.codegen.annotation.type.GenerationStrategy;

@CodePrototype(strategy = GenerationStrategy.NONE)
public class CodeJacksonTestCollectionPrototype {

    @EnumPrototype
    enum Item {
        TEST,
        TEST2
    }

}
