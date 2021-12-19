/*Generated code by Binis' code generator.*/
package net.binis.codegen.test;

import net.binis.codegen.creator.EntityCreatorModifier;
import net.binis.codegen.annotation.Default;
import javax.annotation.processing.Generated;

@Generated(value = "TestAnnotationPrototype", comments = "TestAnnotationImpl")
@Default("net.binis.codegen.test.TestAnnotationImpl")
public interface TestAnnotation {

    static TestAnnotation.Modify create() {
        return (TestAnnotation.Modify) EntityCreatorModifier.create(TestAnnotation.class).with();
    }

    String getValue();

    TestAnnotation.Modify with();

    interface Fields<T> {
        T value(String value);
    }

    interface Modify extends TestAnnotation.Fields<TestAnnotation.Modify> {
        TestAnnotation done();
    }
}
