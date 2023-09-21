/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.objects.BaseCompiledImpl;
import net.binis.codegen.modifier.impl.BaseModifierImpl;
import net.binis.codegen.modifier.Modifiable;
import javax.annotation.processing.Generated;

@Generated(value = "net.binis.codegen.TestPrototype", comments = "Test")
public class TestImpl extends BaseCompiledImpl implements Test, Modifiable<Test.Modify> {

    protected Long other;

    public TestImpl() {
        super();
    }

    public Long getOther() {
        return other;
    }

    public void setOther(Long other) {
        this.other = other;
    }

    public Test.Modify with() {
        return new TestModifyImpl(this);
    }

    protected class TestModifyImpl extends BaseModifierImpl<Test.Modify, Test> implements Test.Modify {

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

        public Test.Modify other(Long other) {
            TestImpl.this.other = other;
            return this;
        }
    }
}
