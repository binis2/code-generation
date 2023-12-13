/*Generated code by Binis' code generator.*/
package net.binis.codegen.test;

import net.binis.codegen.objects.Generic;
import net.binis.codegen.modifier.BaseModifier;
import net.binis.codegen.creator.EntityCreatorModifier;
import net.binis.codegen.annotation.Default;
import javax.annotation.processing.Generated;
import java.util.function.Consumer;

@Generated(value = "net.binis.codegen.test.TestPrototype", comments = "TestImpl")
@Default("net.binis.codegen.test.TestImpl")
@SuppressWarnings("unchecked")
public interface Test extends Generic<Test> {

    // region starters
    static Test.Modify create() {
        return (Test.Modify) EntityCreatorModifier.create(Test.class).with();
    }
    // endregion

    Test.Modify with();

    // region inner classes
    interface EmbeddedModify<T, R> extends BaseModifier<T, R>, Test.Fields<T> {
        Test.EmbeddedSoloModify<EmbeddedModify<T, R>> payload();
    }

    interface EmbeddedSoloModify<R> extends Test.EmbeddedModify<Test.EmbeddedSoloModify<R>, R> {
    }

    interface Fields<T> {
        T other(java.lang.String other);
        T payload(Test payload);
    }

    interface Modify extends EmbeddedModify<Test.Modify, Test> {
        Modify payload$(Consumer<Test.Modify> init);
    }
    // endregion
}
