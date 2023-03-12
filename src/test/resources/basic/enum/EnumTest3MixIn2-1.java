/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.objects.base.enumeration.CodeEnum;
import net.binis.codegen.factory.CodeFactory;
import javax.annotation.processing.Generated;

@Generated(value = "net.binis.codegen.MixIn2Prototype", comments = "TestImpl")
public interface MixIn2 extends CodeEnum {

    static final Test KNOWN = Test.KNOWN;

    static final Test MIXIN2_KNOWN = CodeFactory.initializeEnumValue(Test.class, "MIXIN2_KNOWN", 11);

    static final Test MIXIN2_NEXT = CodeFactory.initializeEnumValue(Test.class, "MIXIN2_NEXT", 12);

    static final Test MIXIN2_UNKNOWN = CodeFactory.initializeEnumValue(Test.class, "MIXIN2_UNKNOWN", 10);

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
