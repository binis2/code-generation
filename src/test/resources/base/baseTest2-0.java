/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.modifier.Modifiable;
import javax.annotation.processing.Generated;

@Generated(value = "TestPrototype", comments = "Test")
public class TestImpl extends BaseImpl implements Test, Modifiable<Test.Modify> {

    protected String title;

    public TestImpl() {
        super();
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

    protected class TestModifyImpl implements Test.Modify {

        public Test done() {
            return TestImpl.this;
        }

        public Test.Modify id(Long id) {
            TestImpl.this.id = id;
            return this;
        }

        public Test.Modify title(String title) {
            TestImpl.this.title = title;
            return this;
        }
    }
}
