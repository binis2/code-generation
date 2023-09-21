/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.objects.BaseCompiled;
import net.binis.codegen.modifier.BaseModifier;
import javax.annotation.processing.Generated;

@Generated(value = "net.binis.codegen.TestPrototype", comments = "TestImpl")
public interface Test extends BaseCompiled {
    Long getOther();

    Test.Modify with();

    interface Fields<T> extends BaseCompiled.Fields<T> {
        T other(Long other);
    }

    interface Modify extends Test.Fields<Test.Modify>, BaseModifier<Test.Modify, Test> {
    }
}
