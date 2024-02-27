/*Generated code by Binis' code generator.*/
package net.binis.codegen.something;

import net.binis.codegen.modifier.impl.BaseModifierImpl;
import net.binis.codegen.modifier.Modifiable;
import net.binis.codegen.TestEnum;
import net.binis.codegen.BaseImpl;
import javax.annotation.processing.Generated;

@Generated(value = "net.binis.codegen.something.TestPrototype", comments = "Test")
public class TestImpl extends BaseImpl implements Test, Modifiable<Test.Modify> {

    public TestImpl() {
        super();
    }

    public Test.Modify with() {
        return new TestModifyImpl(this);
    }

    @Generated("ModifierEnricher")
    protected class TestModifyImpl extends BaseModifierImpl<Test.Modify, Test> implements Test.Modify {

        protected TestModifyImpl(Test parent) {
            super(parent);
        }

        public Test done() {
            return TestImpl.this;
        }

        public Test.Modify type(TestEnum type) {
            TestImpl.this.type = type;
            return this;
        }
    }
}
