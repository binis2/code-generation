/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.modifier.impl.BaseModifierImpl;
import net.binis.codegen.modifier.Modifiable;
import net.binis.codegen.factory.CodeFactory;
import net.binis.codegen.annotation.Ignore;
import javax.annotation.processing.Generated;

@Generated(value = "net.binis.codegen.TestPrototype", comments = "Test")
public class TestImpl implements Test, Modifiable<Test.Modify> {

    @Ignore(forMapper = true)
    protected String title;

    // region constructor & initializer
    static {
        CodeFactory.registerType(Test.class, TestImpl::new, null);
    }

    public TestImpl() {
    }
    // endregion

    // region getters
    @Ignore(forMapper = true)
    public String getTitle() {
        return title;
    }
    // endregion

    // region setters
    @Ignore(forMapper = true)
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

        @Ignore(forMapper = true)
        public Test.Modify title(String title) {
            TestImpl.this.title = title;
            return this;
        }
    }
    // endregion
}
