/*Generated code by Binis' code generator.*/
package net.binis.codegen.test;

import net.binis.codegen.modifier.Modifiable;
import net.binis.codegen.factory.CodeFactory;
import javax.annotation.processing.Generated;

@Generated(value = "TestAnnotationPrototype", comments = "TestAnnotation")
public class TestAnnotationImpl implements TestAnnotation, Modifiable<TestAnnotation.Modify> {

    protected String extended = "asd";

    protected String value;

    {
        CodeFactory.registerType(TestAnnotation.class, TestAnnotationImpl::new, null);
    }

    public TestAnnotationImpl() {
    }

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
        return new TestAnnotationModifyImpl();
    }

    protected class TestAnnotationModifyImpl implements TestAnnotation.Modify {

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
}