/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.creator.EntityCreatorModifier;
import javax.annotation.processing.Generated;

@Generated(value = "MixInPrototype", comments = "MixInImpl")
public interface MixIn extends Test {

    MixIn.Modify asMixIn();

    static MixIn.Modify create() {
        return ((MixIn) EntityCreatorModifier.create(MixIn.class, "net.binis.codegen.TestImpl")).asMixIn();
    }

    String getSubtitle();

    void setSubtitle(String subtitle);

    interface Fields<T> extends Test.Fields<T> {

        T subtitle(String subtitle);
    }

    interface Modify extends MixIn.Fields<MixIn.Modify> {

        MixIn done();
    }
}
