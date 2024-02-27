/*Generated code by Binis' code generator.*/
package net.binis.codegen.something;

import net.binis.codegen.objects.BaseCompiledWithExternalGeneric;
import net.binis.codegen.modifier.BaseModifier;
import javax.annotation.processing.Generated;

@Generated(value = "net.binis.codegen.something.TestPrototype", comments = "TestImpl")
public interface Test extends BaseCompiledWithExternalGeneric {
    Test.Modify with();

    interface Fields<T> {
        T id(long id);
        T type(String type);
    }

    interface Modify extends Test.Fields<Test.Modify>, BaseModifier<Test.Modify, Test> {
    }
}
