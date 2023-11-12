/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.BasicsTest.TestTitle;
import javax.annotation.processing.Generated;

@Generated(value = "net.binis.codegen.TestPrototype", comments = "TestImpl")
public interface Test extends BasicsTest.TestTitle {
    String getTitle();

    void setTitle(String title);
}
