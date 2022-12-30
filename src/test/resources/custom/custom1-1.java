/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.spring.modifier.AsyncEntityModifier;
import net.binis.codegen.creator.EntityCreatorModifier;
import net.binis.codegen.annotation.Default;
import javax.annotation.processing.Generated;

@Generated(value = "TestPrototype", comments = "TestImpl")
@Default("net.binis.codegen.TestImpl")
public interface Test {

    // region starters
    @SuppressWarnings(value = "unchecked")
    static Test.Modify create() {
        return (Test.Modify) EntityCreatorModifier.create(Test.class).with();
    }
    // endregion

    Long getId();

    Test.Modify with();

    // region inner classes
    interface Fields<T> {
        T id(Long id);
    }

    interface Modify extends Test.Fields<Test.Modify>, AsyncEntityModifier<Test.Modify, Test> {
    }
    // endregion
}
