/*Generated code by Binis' code generator.*/
package net.binis.codegen.test;

import net.binis.codegen.modifier.impl.BaseModifierImpl;
import net.binis.codegen.modifier.Modifiable;
import net.binis.codegen.factory.CodeFactory;
import javax.annotation.processing.Generated;
import java.util.function.Consumer;

@Generated(value = "net.binis.codegen.test.TestPrototype", comments = "Test")
public class TestImpl implements Test, Modifiable<Test.Modify> {

    protected java.lang.String other;

    protected Test payload;

    // region constructor & initializer
    static {
        CodeFactory.registerType(Test.class, TestImpl::new, (p, v, r) -> ((TestImpl) v).new TestImplSoloModifyImpl(p));
    }

    public TestImpl() {
    }
    // endregion

    // region getters
    public java.lang.String getOther() {
        return other;
    }

    public Test getPayload() {
        return payload;
    }

    public Test.Modify with() {
        return new TestModifyImpl(this);
    }
    // endregion

    // region inner classes
    @SuppressWarnings("unchecked")
    @Generated("ModifierEnricher")
    protected class TestImplEmbeddedModifyImpl<T, R> extends BaseModifierImpl<T, R> implements Test.EmbeddedModify<T, R> {

        protected TestImplEmbeddedModifyImpl(R parent) {
            super(parent);
        }

        public T other(java.lang.String other) {
            TestImpl.this.other = other;
            return (T) this;
        }

        public Test.EmbeddedSoloModify<Test.EmbeddedModify<T, R>> payload() {
            if (TestImpl.this.payload == null) {
                TestImpl.this.payload = CodeFactory.create(Test.class);
            }
            return CodeFactory.modify(this, TestImpl.this.payload, Test.class);
        }

        public T payload(Test payload) {
            TestImpl.this.payload = payload;
            return (T) this;
        }
    }

    @SuppressWarnings("unchecked")
    @Generated("ModifierEnricher")
    protected class TestImplSoloModifyImpl extends TestImplEmbeddedModifyImpl implements Test.EmbeddedSoloModify {

        protected TestImplSoloModifyImpl(Object parent) {
            super(parent);
        }
    }

    @Generated("ModifierEnricher")
    @SuppressWarnings("unchecked")
    protected class TestModifyImpl extends TestImplEmbeddedModifyImpl<Test.Modify, Test> implements Test.Modify {

        protected TestModifyImpl(Test parent) {
            super(parent);
        }

        public Test.Modify payload$(Consumer<Test.Modify> init) {
            if (TestImpl.this.payload == null) {
                TestImpl.this.payload = CodeFactory.create(Test.class);
            }
            init.accept(TestImpl.this.payload.with());
            return this;
        }
    }
    // endregion
}
