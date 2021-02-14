/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.creator.EntityCreatorModifier;

public interface Test {

    static Test.Modify create() {
        return (Test.Modify) EntityCreatorModifier.create(Test.class).with();
    }

    String getTitle();

    void setTitle(String title);

    Test.Modify with();

    interface Modify {

        Test done();

        Modify title(String title);
    }
}
