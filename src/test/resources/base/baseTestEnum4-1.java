/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.objects.base.enumeration.CodeEnum;
import net.binis.codegen.factory.CodeFactory;
import net.binis.codegen.annotation.Default;
import javax.annotation.processing.Generated;

@Generated(value = "net.binis.codegen.prototype.TestEnumPrototype", comments = "TestEnumImpl")
@net.binis.codegen.annotation.Generated(by = "net.binis.codegen.prototype.TestEnumPrototype")
@Default("net.binis.codegen.TestEnumImpl")
public interface TestEnum extends CodeEnum {

    static final TestEnum UNKNOWN = CodeFactory.initializeEnumValue(TestEnum.class, "UNKNOWN", 0);

    static TestEnum valueOf(String name) {
        return CodeFactory.enumValueOf(TestEnum.class, name);
    }

    static TestEnum valueOf(int ordinal) {
        return CodeFactory.enumValueOf(TestEnum.class, ordinal);
    }

    static TestEnum[] values() {
        return CodeFactory.enumValues(TestEnum.class);
    }
}
