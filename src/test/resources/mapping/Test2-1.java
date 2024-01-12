/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.map.annotation.CodeMapping;
import javax.annotation.processing.Generated;

@Generated(value = "net.binis.codegen.TestPrototype", comments = "TestImpl")
public interface Test {
    @CodeMapping(ignore = true)
    String getTitle();

    void setTitle(String title);
}
