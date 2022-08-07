/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.factory.CodeFactory;
import javax.annotation.processing.Generated;

@Generated(value = "TestPrototype", comments = "Test")
public class TestImpl implements Test {

    protected String title;

    public TestImpl() {
    }

    public <T> T as(Class<T> cls) {
        return CodeFactory.projection(this, cls);
    }

    public <T> T cast(Class<T> cls) {
        return CodeFactory.cast(this, cls);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
