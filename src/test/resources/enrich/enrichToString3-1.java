/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.objects.base.enumeration.CodeEnum;
import net.binis.codegen.factory.CodeFactory;
import net.binis.codegen.annotation.Default;
import javax.annotation.processing.Generated;

@Generated(value = "net.binis.codegen.TestPrototype", comments = "TestImpl")
@net.binis.codegen.annotation.Generated(by = "net.binis.codegen.TestPrototype")
@Default("net.binis.codegen.TestImpl")
public interface Test extends CodeEnum {

    static final Test ONE = CodeFactory.initializeEnumValue(Test.class, "ONE", 0, "One");

    static final Test TWO = CodeFactory.initializeEnumValue(Test.class, "TWO", 1, "Two");

    static Test valueOf(String name) {
        return CodeFactory.enumValueOf(Test.class, name);
    }

    static Test valueOf(int ordinal) {
        return CodeFactory.enumValueOf(Test.class, ordinal);
    }

    static Test[] values() {
        return CodeFactory.enumValues(Test.class);
    }
}
