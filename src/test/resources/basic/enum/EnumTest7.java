package net.binis.codegen;

import net.binis.codegen.annotation.EnumPrototype;
import net.binis.codegen.annotation.builder.CodeBuilder;

import java.util.List;

@CodeBuilder
public interface TestPrototype {

    List<TestEnumPrototype> list();

    @EnumPrototype
    public enum TestEnumPrototype {
        ONE,
        TWO;
    }

    @EnumPrototype(mixIn = TestEnumPrototype.class)
    public enum TestEnum2Prototype {
        THREE,
        FOUR;
    }

}
