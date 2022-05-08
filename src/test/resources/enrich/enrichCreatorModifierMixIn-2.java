/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.modifier.BaseModifier;
import net.binis.codegen.creator.EntityCreatorModifier;
import net.binis.codegen.annotation.Default;
import javax.annotation.processing.Generated;

@Generated(value = "MixInPrototype", comments = "MixInImpl")
@Default("net.binis.codegen.TestImpl")
public interface MixIn extends Test {
    MixIn.Modify asMixIn();

    static MixIn.Modify create() {
        return ((MixIn) EntityCreatorModifier.create(MixIn.class)).asMixIn();
    }

    String getSubtitle();

    void setSubtitle(String subtitle);

    interface Fields<T> extends Test.Fields<T> {
        T subtitle(String subtitle);
    }

    interface Modify extends MixIn.Fields<MixIn.Modify>, BaseModifier<MixIn.Modify, MixIn> {
    }
}
