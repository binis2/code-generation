/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.modifier.impl.BaseModifierImpl;
import net.binis.codegen.modifier.Modifiable;
import net.binis.codegen.factory.CodeFactory;
import javax.annotation.processing.Generated;

@Generated(value = "net.binis.codegen.TestPrototype", comments = "Test")
public class TestImpl implements Test, MixIn, Modifiable<Test.Modify> {

    protected String subtitle;

    protected String title;

    // region constructor & initializer
    {
        CodeFactory.registerType(Test.class, TestImpl::new, null);
        CodeFactory.registerType(MixIn.class, TestImpl::new, null);
    }

    public TestImpl() {
    }
    // endregion

    // region getters
    public MixIn.Modify asMixIn() {
        return new MixInModifyImpl(this);
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getTitle() {
        return title;
    }
    // endregion

    // region setters
    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Test.Modify with() {
        return new TestModifyImpl(this);
    }
    // endregion

    // region inner classes
    @Generated("ModifierEnricher")
    protected class MixInModifyImpl extends BaseModifierImpl<MixIn.Modify, MixIn> implements MixIn.Modify {

        protected MixInModifyImpl(MixIn parent) {
            super(parent);
        }

        public MixIn done() {
            return TestImpl.this;
        }

        public MixIn.Modify subtitle(String subtitle) {
            TestImpl.this.subtitle = subtitle;
            return this;
        }

        public MixIn.Modify title(String title) {
            TestImpl.this.title = title;
            return this;
        }
    }

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
