/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import jdk.jfr.Label;
import jdk.jfr.Description;
import javax.annotation.processing.Generated;

@Generated(value = "TestPrototype", comments = "TestImpl")
@SuppressWarnings("test")
@Label("test")
public interface Test {
    @Description("description")
    String getSubtitle();
    String getTitle();

    void setSubtitle(String subtitle);
    void setTitle(String title);
}
