/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.modifier.BaseModifier;
import net.binis.codegen.creator.EntityCreatorModifier;
import net.binis.codegen.annotation.Ignore;
import net.binis.codegen.annotation.Default;
import javax.annotation.processing.Generated;

@Generated(value = "net.binis.codegen.TestPrototype", comments = "TestImpl")
@Default("net.binis.codegen.TestImpl")
@SuppressWarnings("unchecked")
public interface Test {

    // region starters
    static Test.Modify create() {
        return (Test.Modify) EntityCreatorModifier.create(Test.class).with();
    }
    // endregion

    @Ignore(forMapper = true)
    String getTitle();

    @Ignore(forMapper = true)
    void setTitle(String title);

    Test.Modify with();

    // region inner classes
    interface Fields<T> {
        T title(String title);
    }

    interface Modify extends Test.Fields<Test.Modify>, BaseModifier<Test.Modify, Test> {
    }
    // endregion
}
