/*Generated code by Binis' code generator.*/
package net.binis.codegen.test;

import net.binis.codegen.creator.EntityCreatorModifier;
import net.binis.codegen.annotation.Default;
import javax.annotation.processing.Generated;

@Generated(value = "TestPrototype", comments = "TestImpl")
@Default("net.binis.codegen.test.TestImpl")
public interface Test extends Generic<TestPayload> {

    static Test.Modify create() {
        return (Test.Modify) EntityCreatorModifier.create(Test.class).with();
    }

    Test.Modify with();

    interface Fields<T> {
        T payload(TestPayload payload);
    }

    interface Modify extends Test.Fields<Test.Modify> {
        Test done();
    }
}
