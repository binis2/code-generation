/*Generated code by Binis' code generator.*/
package net.binis.codegen.test;

import net.binis.codegen.creator.EntityCreatorModifier;
import javax.annotation.processing.Generated;

@Generated(value = "TestAnnotationPrototype", comments = "TestAnnotationImpl")
public interface TestAnnotation extends Extended {

    static TestAnnotation.Modify create() {
        return (TestAnnotation.Modify) EntityCreatorModifier.create(TestAnnotation.class, "net.binis.codegen.test.TestAnnotationImpl").with();
    }

    String getValue();

    TestAnnotation.Modify with();

    interface Fields<T> {

        T extended(String extended);

        T value(String value);
    }

    interface Modify extends TestAnnotation.Fields<TestAnnotation.Modify> {

        TestAnnotation done();
    }
}
