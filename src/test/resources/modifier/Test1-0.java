/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.objects.Pair;
import net.binis.codegen.modifier.impl.BaseModifierImpl;
import net.binis.codegen.modifier.Modifiable;
import net.binis.codegen.factory.CodeFactory;
import net.binis.codegen.collection.CodeListImpl;
import net.binis.codegen.collection.CodeList;
import javax.annotation.processing.Generated;
import java.util.List;

@Generated(value = "net.binis.codegen.TestPrototype", comments = "Test")
public class TestImpl implements Test, Modifiable<Test.Modify> {

    protected List<Pair<String, List<String>>> test;

    // region constructor & initializer
    static {
        CodeFactory.registerType(Test.class, TestImpl::new, null);
    }

    public TestImpl() {
    }
    // endregion

    // region getters
    public List<Pair<String, List<String>>> getTest() {
        return test;
    }

    public Test.Modify with() {
        return new TestModifyImpl(this);
    }
    // endregion

    // region inner classes
    @Generated("ModifierEnricher")
    @SuppressWarnings("unchecked")
    protected class TestModifyImpl extends BaseModifierImpl<Test.Modify, Test> implements Test.Modify {

        protected TestModifyImpl(Test parent) {
            super(parent);
        }

        public Test done() {
            return TestImpl.this;
        }

        public CodeList test() {
            if (TestImpl.this.test == null) {
                TestImpl.this.test = new java.util.ArrayList<>();
            }
            return new CodeListImpl<>(this, TestImpl.this.test);
        }

        public Test.Modify test(List<Pair<String, List<String>>> test) {
            TestImpl.this.test = test;
            return this;
        }
    }
    // endregion
}
