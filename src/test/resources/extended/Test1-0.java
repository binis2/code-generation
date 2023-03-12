/*Generated code by Binis' code generator.*/
package net.binis.codegen.test;

import net.binis.codegen.modifier.impl.BaseModifierImpl;
import net.binis.codegen.modifier.Modifiable;
import net.binis.codegen.factory.CodeFactory;
import javax.annotation.processing.Generated;

@Generated(value = "net.binis.codegen.test.TestAnnotationPrototype", comments = "TestAnnotation")
public class TestAnnotationImpl implements TestAnnotation, Modifiable<TestAnnotation.Modify> {

    protected String extended = "asd";

    protected String value;

    // region constructor & initializer
    {
        CodeFactory.registerType(TestAnnotation.class, TestAnnotationImpl::new, null);
    }

    public TestAnnotationImpl() {
    }
    // endregion

    // region getters
    public String getDoubleExtended() {
        return this.extended + this.extended;
    }

    public String getExtended() {
        return this.extended;
    }

    public String getValue() {
        return value;
    }

    public TestAnnotation.Modify with() {
        return new TestAnnotationModifyImpl(this);
    }
    // endregion

    // region inner classes
    protected class TestAnnotationModifyImpl extends BaseModifierImpl<TestAnnotation.Modify, TestAnnotation> implements TestAnnotation.Modify {

        protected TestAnnotationModifyImpl(TestAnnotation parent) {
            super(parent);
        }

        public TestAnnotation done() {
            return TestAnnotationImpl.this;
        }

        public TestAnnotation.Modify extended(String extended) {
            TestAnnotationImpl.this.extended = extended;
            return this;
        }

        public TestAnnotation.Modify value(String value) {
            TestAnnotationImpl.this.value = value;
            return this;
        }
    }
    // endregion
}
