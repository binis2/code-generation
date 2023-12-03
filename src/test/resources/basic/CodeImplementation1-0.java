/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import javax.annotation.processing.Generated;

@Generated(value = "net.binis.codegen.TestPrototype", comments = "Test")
public class TestImpl implements Test {

    protected int title;

    public TestImpl() {
    }

    public String getTitle() {
        return Integer.toString(this.title);
    }

    public void setTitle(int title) {
        this.title = title;
    }
}
