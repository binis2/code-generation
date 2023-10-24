/*Generated code by Binis' code generator.*/
package net.binis.test;

import net.binis.codegen.objects.TestCompiledEnum;
import net.binis.codegen.modifier.impl.BaseModifierImpl;
import net.binis.codegen.modifier.Modifiable;
import net.binis.codegen.factory.CodeFactory;
import javax.annotation.processing.Generated;

@Generated(value = "net.binis.test.prototype.EnumUsingTestPrototype", comments = "EnumUsingTest")
public class EnumUsingTestImpl implements EnumUsingTest, Modifiable<EnumUsingTest.Modify> {

    protected TestCompiledEnum type;

    // region constructor & initializer
    {
        CodeFactory.registerType(EnumUsingTest.class, EnumUsingTestImpl::new, null);
    }

    public EnumUsingTestImpl() {
    }
    // endregion

    // region getters
    public TestCompiledEnum getType() {
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

        public EnumUsingTest.Modify type(TestCompiledEnum type) {
            EnumUsingTestImpl.this.type = type;
            return this;
        }
    }
    // endregion
}
