/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import javax.annotation.processing.Generated;

@Generated(value = "net.binis.codegen.TestPrototype", comments = "Test")
public class TestImpl implements Test {

    protected Integer title;

    public TestImpl() {
    }

    public String getTitle() {
        if (title == null) {
            return "empty";
        } else {
            return title.toString();
        }
    }

    public void setTitle(Integer title) {
        this.title = title;
    }
}
