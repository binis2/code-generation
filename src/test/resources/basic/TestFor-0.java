/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import jdk.jfr.Description;
import javax.annotation.processing.Generated;

@Generated(value = "net.binis.codegen.TestPrototype", comments = "Test")
public class TestImpl implements Test {

    protected String subtitle;

    @Description("description")
    protected String title;

    public TestImpl() {
    }

    @Description("description")
    public String getSubtitle() {
        return subtitle;
    }

    public String getTitle() {
        return title;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
