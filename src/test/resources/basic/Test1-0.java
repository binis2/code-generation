/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import javax.annotation.processing.Generated;

@Generated(value = "net.binis.codegen.TestPrototype", comments = "Test")
public class TestImpl implements Test {

    protected byte[] data;

    protected String title = "asd";

    public TestImpl() {
    }

    public byte[] getData() {
        return data;
    }

    public String getTitle() {
        return title;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
