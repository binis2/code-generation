/*Generated code by Binis' code generator.*/
package net.binis.test;

import net.binis.test.enums.TestEnum;
import net.binis.codegen.modifier.impl.BaseModifierImpl;
import net.binis.codegen.modifier.Modifiable;
import net.binis.codegen.factory.CodeFactory;
import javax.annotation.processing.Generated;

@Generated(value = "net.binis.test.prototype.EnumUsingTestPrototype", comments = "EnumUsingTest")
public class EnumUsingTestImpl implements EnumUsingTest, Modifiable<EnumUsingTest.Modify> {

    protected TestEnum type;

    // region constructor & initializer
    {
        CodeFactory.registerType(EnumUsingTest.class, EnumUsingTestImpl::new, null);
    }

    public EnumUsingTestImpl() {
    }
    // endregion

    // region getters
    public TestEnum getType() {
        return type;
    }

    public EnumUsingTest.Modify with() {
        return new EnumUsingTestModifyImpl(this);
    }
    // endregion

    // region inner classes
    @Generated("ModifierEnricher")
    protected class EnumUsingTestModifyImpl extends BaseModifierImpl<EnumUsingTest.Modify, EnumUsingTest> implements EnumUsingTest.Modify {

        protected EnumUsingTestModifyImpl(EnumUsingTest parent) {
            super(parent);
        }

        public EnumUsingTest done() {
            return EnumUsingTestImpl.this;
        }

        public EnumUsingTest.Modify type(TestEnum type) {
            EnumUsingTestImpl.this.type = type;
            return this;
        }
    }
    // endregion
}
