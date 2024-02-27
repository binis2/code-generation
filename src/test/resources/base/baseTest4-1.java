/*Generated code by Binis' code generator.*/
package net.binis.codegen.something;

import net.binis.codegen.modifier.BaseModifier;
import net.binis.codegen.TestEnum;
import net.binis.codegen.Base;
import javax.annotation.processing.Generated;

@Generated(value = "net.binis.codegen.something.TestPrototype", comments = "TestImpl")
public interface Test extends Base {
    Test.Modify with();

    interface Fields<T> {
        T type(TestEnum type);
    }

    interface Modify extends Test.Fields<Test.Modify>, BaseModifier<Test.Modify, Test> {
    }
}
