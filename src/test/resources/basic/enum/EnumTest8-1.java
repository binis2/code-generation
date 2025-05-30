/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.objects.base.enumeration.CodeEnum;
import net.binis.codegen.objects.TestCompiledEnum;
import net.binis.codegen.factory.CodeFactory;
import net.binis.codegen.annotation.Default;
import javax.annotation.processing.Generated;

@Generated(value = "net.binis.codegen.TestPrototype", comments = "TestCompiledEnumImpl")
@net.binis.codegen.annotation.Generated(by = "net.binis.codegen.TestPrototype")
@Default("net.binis.codegen.objects.TestCompiledEnumImpl")
public interface Test extends CodeEnum {

    static final TestCompiledEnum EX_1 = CodeFactory.initializeEnumValue(TestCompiledEnum.class, "EX_1", 2);

    static final TestCompiledEnum EX_2 = CodeFactory.initializeEnumValue(TestCompiledEnum.class, "EX_2", 3);

    static final TestCompiledEnum KNOWN = TestCompiledEnum.KNOWN;

    static final TestCompiledEnum UNKNOWN = TestCompiledEnum.UNKNOWN;

    static TestCompiledEnum valueOf(String name) {
        return CodeFactory.enumValueOf(TestCompiledEnum.class, name);
    }

    static TestCompiledEnum valueOf(int ordinal) {
        return CodeFactory.enumValueOf(TestCompiledEnum.class, ordinal);
    }

    static TestCompiledEnum[] values() {
        return CodeFactory.enumValues(TestCompiledEnum.class);
    }
}
