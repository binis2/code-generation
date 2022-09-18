/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.objects.base.enumeration.CodeEnum;
import net.binis.codegen.factory.CodeFactory;
import net.binis.codegen.annotation.Default;
import javax.annotation.processing.Generated;

@Generated(value = "MixInPrototype", comments = "TestImpl")
@Default("net.binis.codegen.TestImpl")
public interface MixIn extends CodeEnum {

    static final Test KNOWN = Test.KNOWN;

    static final Test MIXIN_KNOWN = CodeFactory.initializeEnumValue(Test.class, "MIXIN_KNOWN", 1);

    static final Test MIXIN_NEXT = CodeFactory.initializeEnumValue(Test.class, "MIXIN_NEXT", 2);

    static final Test MIXIN_UNKNOWN = CodeFactory.initializeEnumValue(Test.class, "MIXIN_UNKNOWN", 0);

    static final Test NEXT = Test.NEXT;

    static final Test UNKNOWN = Test.UNKNOWN;

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
