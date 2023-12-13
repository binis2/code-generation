/*Generated code by Binis' code generator.*/
package net.binis.codegen.test;

import net.binis.codegen.test.intf.Generic;
import net.binis.codegen.modifier.BaseModifier;
import net.binis.codegen.creator.EntityCreatorModifier;
import net.binis.codegen.annotation.Default;
import javax.annotation.processing.Generated;

@Generated(value = "net.binis.codegen.test.TestPrototype", comments = "TestImpl")
@Default("net.binis.codegen.test.TestImpl")
@SuppressWarnings("unchecked")
public interface Test extends Generic<Double> {

    // region starters
    static Test.Modify create() {
        return (Test.Modify) EntityCreatorModifier.create(Test.class).with();
    }
    // endregion

    Test.Modify with();

    // region inner classes
    interface Fields<T> {
        T other(String other);
        T payload(Double payload);
    }

    interface Modify extends Test.Fields<Test.Modify>, BaseModifier<Test.Modify, Test> {
    }
    // endregion
}
