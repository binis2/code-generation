package net.binis.codegen;

import net.binis.codegen.annotation.EnumPrototype;
import net.binis.codegen.annotation.builder.CodeBuilder;
import net.binis.codegen.objects.TestCompiledEnumPrototype;

import java.util.List;

@EnumPrototype(mixIn = TestCompiledEnumPrototype.class)
public enum TestPrototype {

    EX_1,
    EX_2

}
