/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.modifier.impl.BaseModifierImpl;
import net.binis.codegen.modifier.Modifiable;
import net.binis.codegen.factory.CodeFactory;
import javax.annotation.processing.Generated;

@Generated(value = "net.binis.codegen.TestPrototype", comments = "Test")
public class TestImpl implements Test, Modifiable<Test.Modify> {

    protected String title;

    // region constructor & initializer
    static {
        CodeFactory.registerType(Test.class, TestImpl::new, null);
    }

    public TestImpl() {
    }
    // endregion

    // region getters
    public String getTitle() {
        return title;
    }
    // endregion

    // region setters
    public void setTitle(String title) {
        this.title = title;
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

        public Test.Modify title(String title) {
            TestImpl.this.title = title;
            return this;
        }
    }
    // endregion
}
