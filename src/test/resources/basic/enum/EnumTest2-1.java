/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.objects.base.enumeration.CodeEnum;
import net.binis.codegen.factory.CodeFactory;
import net.binis.codegen.annotation.Default;
import javax.annotation.processing.Generated;

@Generated(value = "net.binis.codegen.TestPrototype", comments = "TestImpl")
@Default("net.binis.codegen.TestImpl")
public interface Test extends CodeEnum {

    static final String CONSTANT = "const";

    static final Test KNOWN = CodeFactory.initializeEnumValue(Test.class, "KNOWN", 1, "known", false, 10);

    static final Test NEXT = CodeFactory.initializeEnumValue(Test.class, "NEXT", 2, "next", true, 15);

    static final Test UNKNOWN = CodeFactory.initializeEnumValue(Test.class, "UNKNOWN", 0, "unknown", true, 5);

    String getLabel();
    int getValue();

    boolean isCheck();

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
