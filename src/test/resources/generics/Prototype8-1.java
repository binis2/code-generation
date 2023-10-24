/*Generated code by Binis' code generator.*/
package net.binis.test;

import net.binis.codegen.objects.Typeable;
import net.binis.codegen.objects.TestCompiledEnum;
import net.binis.codegen.modifier.BaseModifier;
import net.binis.codegen.creator.EntityCreatorModifier;
import net.binis.codegen.annotation.Default;
import javax.annotation.processing.Generated;

@Generated(value = "net.binis.test.prototype.EnumUsingTestPrototype", comments = "EnumUsingTestImpl")
@Default("net.binis.test.EnumUsingTestImpl")
@SuppressWarnings("unchecked")
public interface EnumUsingTest extends Typeable<TestCompiledEnum> {

    // region starters
    static EnumUsingTest.Modify create() {
        return (EnumUsingTest.Modify) EntityCreatorModifier.create(EnumUsingTest.class).with();
    }
    // endregion

    EnumUsingTest.Modify with();

    // region inner classes
    interface Fields<T> {
        T type(TestCompiledEnum type);
    }

    interface Modify extends EnumUsingTest.Fields<EnumUsingTest.Modify>, BaseModifier<EnumUsingTest.Modify, EnumUsingTest> {
    }
    // endregion
}
