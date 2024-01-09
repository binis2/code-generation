/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.map.annotation.CodeMapping;
import javax.annotation.processing.Generated;

@Generated(value = "net.binis.codegen.TestPrototype", comments = "Test")
public class TestImpl implements Test {

    @CodeMapping(ignore = true)
    protected String title;

    public TestImpl() {
    }

    @CodeMapping(ignore = true)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
