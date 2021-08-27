/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.creator.EntityCreatorModifier;
import javax.annotation.processing.Generated;

@Generated(value = "TestPrototype", comments = "TestImpl")
public interface Test {

    static Test.Modify create() {
        return (Test.Modify) EntityCreatorModifier.create(Test.class, "net.binis.codegen.TestImpl").with();
    }

    String getTitle();

    void setTitle(String title);

    Test.Modify with();

    interface Fields<T> {

        T title(String title);
    }

    interface Modify extends Test.Fields<Test.Modify> {

        Test done();
    }
}
