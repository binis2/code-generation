/*Generated code by Binis' code generator.*/
package net.binis.codegen.test;

import net.binis.test.enums.TestEnum;
import net.binis.codegen.modifier.impl.BaseModifierImpl;
import net.binis.codegen.modifier.Modifiable;
import net.binis.codegen.factory.CodeFactory;
import javax.annotation.processing.Generated;

@Generated(value = "net.binis.codegen.test.TestPrototype", comments = "Test")
public class TestImpl implements Test, Modifiable<Test.Modify> {

    protected java.lang.String other;

    protected TestEnum payload;

    // region constructor & initializer
    {
        CodeFactory.registerType(Test.class, TestImpl::new, null);
    }

    public TestImpl() {
    }
    // endregion

    // region getters
    public java.lang.String getOther() {
        return other;
    }

    public TestEnum getPayload() {
        return payload;
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

        public Test.Modify other(java.lang.String other) {
            TestImpl.this.other = other;
            return this;
        }

        public Test.Modify payload(TestEnum payload) {
            TestImpl.this.payload = payload;
            return this;
        }
    }
    // endregion
}
