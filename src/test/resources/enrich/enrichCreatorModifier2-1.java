/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.creator.EntityCreator;

public interface Test {

    static Test create() {
        return EntityCreator.create(Test.class);
    }

    String getTitle();

    void setTitle(String title);
}