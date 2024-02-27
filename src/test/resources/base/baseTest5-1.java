/*Generated code by Binis' code generator.*/
package net.binis.codegen.something;

import net.binis.codegen.objects.BaseCompiledWithExternal;
import net.binis.codegen.modifier.BaseModifier;
import javax.annotation.processing.Generated;

@Generated(value = "net.binis.codegen.something.TestPrototype", comments = "TestImpl")
public interface Test extends BaseCompiledWithExternal {
    Test.Modify with();

    interface Fields<T> {
        T id(long id);
    }

    interface Modify extends Test.Fields<Test.Modify>, BaseModifier<Test.Modify, Test> {
    }
}
