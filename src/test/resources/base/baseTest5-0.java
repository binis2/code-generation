/*Generated code by Binis' code generator.*/
package net.binis.codegen.something;

import net.binis.codegen.objects.BaseCompiledWithExternalImpl;
import net.binis.codegen.modifier.impl.BaseModifierImpl;
import net.binis.codegen.modifier.Modifiable;
import javax.annotation.processing.Generated;

@Generated(value = "net.binis.codegen.something.TestPrototype", comments = "Test")
public class TestImpl extends BaseCompiledWithExternalImpl implements Test, Modifiable<Test.Modify> {

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

        public Test.Modify id(long id) {
            TestImpl.this.id = id;
            return this;
        }
    }
}
