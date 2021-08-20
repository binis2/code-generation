/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.spring.BaseEntityModifier;
import net.binis.codegen.modifier.Modifier;
import net.binis.codegen.modifier.Modifiable;
import javax.annotation.processing.Generated;

@Generated(value = "TestPrototype", comments = "Test")
public class TestImpl implements Test, Modifiable<Test.Modify> {

    protected String title;

    public TestImpl() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Test.Modify with() {
        return new TestModifyImpl();
    }

    protected class TestModifyImpl extends BaseEntityModifier<Test.Modify, Test> implements Test.Modify {

        protected TestModifyImpl() {
            setObject(TestImpl.this);
        }

        public Test done() {
            return TestImpl.this;
        }

        public Test.Modify title(String title) {
            TestImpl.this.title = title;
            return this;
        }
    }
}
