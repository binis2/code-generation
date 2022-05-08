/*Generated code by Binis' code generator.*/
package net.binis.codegen.test;

import net.binis.codegen.modifier.BaseModifier;
import net.binis.codegen.creator.EntityCreatorModifier;
import net.binis.codegen.annotation.Default;
import javax.annotation.processing.Generated;

@Generated(value = "TestAnnotationPrototype", comments = "TestAnnotationImpl")
@Default("net.binis.codegen.test.TestAnnotationImpl")
public interface TestAnnotation extends Extended {

    // region starters
    static TestAnnotation.Modify create() {
        return (TestAnnotation.Modify) EntityCreatorModifier.create(TestAnnotation.class).with();
    }
    // endregion

    String getValue();

    TestAnnotation.Modify with();

    // region inner classes
    interface Fields<T> {
        T extended(String extended);
        T value(String value);
    }

    interface Modify extends TestAnnotation.Fields<TestAnnotation.Modify>, BaseModifier<TestAnnotation.Modify, TestAnnotation> {
    }
    // endregion
}
