/*Generated code by Binis' code generator.*/
package net.binis.codegen;

public class TestImpl implements Test {

    protected String title;

    public TestImpl() {
    }

    public <T> T as(Class<T> cls) {
        return cls.cast(this);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
