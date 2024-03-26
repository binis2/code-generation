/*Generated code by Binis' code generator.*/
package net.binis.codegen.test;

import net.binis.codegen.modifier.impl.BaseModifierImpl;
import net.binis.codegen.modifier.Modifiable;
import net.binis.codegen.factory.CodeFactory;
import javax.annotation.processing.Generated;

@Generated(value = "net.binis.codegen.test.TestPrototype", comments = "Test")
public class TestImpl implements Test, Modifiable<Test.Modify> {

    protected String other;

    protected Double payload;

    // region constructor & initializer
    static {
        CodeFactory.registerType(Test.class, TestImpl::new, null);
    }

    public TestImpl() {
    }
    // endregion

    // region getters
    public String getOther() {
        return other;
    }

    public Double getPayload() {
        return payload;
    }
    // endregion

    // region setters
    public void setOther(String other) {
        this.other = other;
    }

    public void setPayload(Double payload) {
        this.payload = payload;
    }

    public Test.Modify with() {
        return new TestModifyImpl(this);
    }
    // endregion

    // region inner classes
    @Generated("ModifierEnricher")
    protected class TestModifyImpl extends BaseModifierImpl<Test.Modify, Test> implements Test.Modify {

        protected TestModifyImpl(Test parent) {
            super(parent);
        }

        public Test done() {
            return TestImpl.this;
        }

        public Test.Modify other(String other) {
            TestImpl.this.other = other;
            return this;
        }

        public Test.Modify payload(Double payload) {
            TestImpl.this.payload = payload;
            return this;
        }
    }
    // endregion
}
