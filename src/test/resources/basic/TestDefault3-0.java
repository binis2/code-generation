/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import javax.annotation.processing.Generated;

@Generated(value = "net.binis.codegen.TestPrototype", comments = "Test")
public class TestImpl implements Test {

    protected boolean title;

    public TestImpl() {
    }

    public boolean isTitle() {
        return !this.title;
    }

    public void setTitle(boolean title) {
        this.title = title;
    }
}
