/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.spring.modifier.impl.AsyncEntityModifierImpl;
import net.binis.codegen.modifier.Modifiable;
import net.binis.codegen.factory.CodeFactory;
import javax.annotation.processing.Generated;

@Generated(value = "net.binis.codegen.TestPrototype", comments = "Test")
public class TestImpl implements Test, Modifiable<Test.Modify> {

    protected Long id;

    // region constructor & initializer
    {
        CodeFactory.registerType(Test.class, TestImpl::new, null);
    }

    public TestImpl() {
    }
    // endregion

    // region getters
    public Long getId() {
        return id;
    }

    public Test.Modify with() {
        return new TestModifyImpl(this);
    }
    // endregion

    // region inner classes
    protected class TestModifyImpl extends AsyncEntityModifierImpl<Test.Modify, Test> implements Test.Modify {

        protected TestModifyImpl(Test parent) {
            super(parent);
        }

        public Test done() {
            return TestImpl.this;
        }

        public Test.Modify id(Long id) {
            TestImpl.this.id = id;
            return this;
        }
    }
    // endregion
}
