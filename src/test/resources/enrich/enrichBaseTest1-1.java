/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.creator.EntityCreator;
import net.binis.codegen.annotation.Default;
import javax.annotation.processing.Generated;

@Generated(value = "net.binis.codegen.TestPrototype", comments = "TestImpl")
@Default("net.binis.codegen.TestImpl")
public interface Test extends Base {

    static Test create() {
        return EntityCreator.create(Test.class);
    }

    String getTitle();

    void setTitle(String title);
}
