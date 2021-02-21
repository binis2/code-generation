/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.modifier.Modifiable;
import net.binis.codegen.factory.CodeFactory;

public class TestImpl implements Test, Modifiable<Test.Modify>, MixIn {

    protected String subtitle;

    protected String title;

    {
        CodeFactory.registerType(Test.class, TestImpl::new, null);
        CodeFactory.registerType(MixIn.class, TestImpl::new, null);
    }

    public TestImpl() {
    }

    public MixIn.Modify asMixIn() {
        return new MixInModifyImpl();
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getTitle() {
        return title;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Test.Modify with() {
        return new TestModifyImpl();
    }

    protected class MixInModifyImpl implements MixIn.Modify {

        public MixIn done() {
            return TestImpl.this;
        }

        public MixIn.Modify subtitle(String subtitle) {
            TestImpl.this.subtitle = subtitle;
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
}
