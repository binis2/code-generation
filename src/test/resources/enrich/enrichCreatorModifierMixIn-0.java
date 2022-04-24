/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.modifier.Modifiable;
import net.binis.codegen.factory.CodeFactory;
import javax.annotation.processing.Generated;

@Generated(value = "TestPrototype", comments = "Test")
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
        return new MixInModifyImpl();
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
        return new TestModifyImpl();
    }
    // endregion

    // region inner classes
    protected class MixInModifyImpl implements MixIn.Modify {

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

    protected class TestModifyImpl implements Test.Modify {

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
