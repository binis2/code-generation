/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.objects.base.enumeration.CodeEnumImpl;
import net.binis.codegen.modifier.impl.BaseModifierImpl;
import net.binis.codegen.modifier.Modifiable;
import net.binis.codegen.factory.CodeFactory;
import net.binis.codegen.collection.CodeListImpl;
import net.binis.codegen.collection.CodeList;
import net.binis.codegen.Test.TestEnum;
import javax.annotation.processing.Generated;
import java.util.List;

@Generated(value = "net.binis.codegen.TestPrototype", comments = "Test")
public class TestImpl implements Test, Modifiable<Test.Modify> {

    protected List<Test.TestEnum> list;

    // region constructor & initializer
    static {
        CodeFactory.registerType(Test.class, TestImpl::new, null);
    }

    public TestImpl() {
    }
    // endregion

    // region getters
    public List<Test.TestEnum> getList() {
        return list;
    }

    public Test.Modify with() {
        return new TestModifyImpl(this);
    }
    // endregion

    // region inner classes
    public static class TestEnum2Impl extends CodeEnumImpl implements TestEnum2 {

        public TestEnum2Impl(int $ordinal, String $name) {
            super($ordinal, $name);
        }

        public boolean equals(Object o) {
            return super.equals(o);
        }

        public int hashCode() {
            return super.hashCode();
        }
    }

    public static class TestEnumImpl extends CodeEnumImpl implements TestEnum {

        public TestEnumImpl(int $ordinal, String $name) {
            super($ordinal, $name);
        }

        public boolean equals(Object o) {
            return super.equals(o);
        }

        public int hashCode() {
            return super.hashCode();
        }
    }

    @Generated("ModifierEnricher")
    @SuppressWarnings("unchecked")
    protected class TestModifyImpl extends BaseModifierImpl<Test.Modify, Test> implements Test.Modify {

        protected TestModifyImpl(Test parent) {
            super(parent);
        }

        public Test done() {
            return TestImpl.this;
        }

        public CodeList list() {
            if (TestImpl.this.list == null) {
                TestImpl.this.list = new java.util.ArrayList<>();
            }
            return new CodeListImpl<>(this, TestImpl.this.list);
        }

        public Test.Modify list(List<Test.TestEnum> list) {
            TestImpl.this.list = list;
            return this;
        }
    }
    // endregion
}
