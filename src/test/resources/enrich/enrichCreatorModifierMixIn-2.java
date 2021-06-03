/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.creator.EntityCreatorModifier;

public interface MixIn extends Test {

    MixIn.Modify asMixIn();

    static MixIn.Modify create() {
        return ((MixIn) EntityCreatorModifier.create(MixIn.class)).asMixIn();
    }

    String getSubtitle();

    void setSubtitle(String subtitle);

    interface Modify {

        MixIn done();

        Modify subtitle(String subtitle);

        Modify title(String title);
    }
}
