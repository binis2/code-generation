/*Generated code by Binis' code generator.*/
package net.binis.codegen.test;

import net.binis.codegen.modifier.impl.BaseModifierImpl;
import net.binis.codegen.modifier.Modifiable;
import net.binis.codegen.factory.CodeFactory;
import javax.annotation.processing.Generated;

@Generated(value = "net.binis.codegen.test.TestPrototype", comments = "Test")
public class TestImpl extends GenericImpl<TestPayload> implements Test, Modifiable<Test.Modify> {

    // region constructor & initializer
    {
        CodeFactory.registerType(Test.class, TestImpl::new, null);
    }

    public TestImpl() {
        super();
    }
    // endregion

    // region getters
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

        public Test.Modify payload(TestPayload payload) {
            TestImpl.this.payload = payload;
            return this;
        }
    }
    // endregion
}
