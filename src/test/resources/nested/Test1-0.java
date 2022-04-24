/*Generated code by Binis' code generator.*/
package net.binis.codegen.test;

import net.binis.codegen.modifier.Modifiable;
import net.binis.codegen.factory.CodeFactory;
import javax.annotation.processing.Generated;

@Generated(value = "TestAnnotationPrototype", comments = "TestAnnotation")
public class TestAnnotationImpl implements TestAnnotation, Modifiable<TestAnnotation.Modify> {

    protected String value;

    // region constructor & initializer
    {
        CodeFactory.registerType(TestAnnotation.class, TestAnnotationImpl::new, null);
    }

    public TestAnnotationImpl() {
    }
    // endregion

    // region getters
    public String getValue() {
        return value;
    }

    public TestAnnotation.Modify with() {
        return new TestAnnotationModifyImpl();
    }
    // endregion

    // region inner classes
    protected class TestAnnotationModifyImpl implements TestAnnotation.Modify {

        public TestAnnotation done() {
            return TestAnnotationImpl.this;
        }

        public TestAnnotation.Modify value(String value) {
            TestAnnotationImpl.this.value = value;
            return this;
        }
    }
    // endregion
}
