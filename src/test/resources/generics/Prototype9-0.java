/*Generated code by Binis' code generator.*/
package net.binis.test;

import net.binis.codegen.types.TestType;
import javax.annotation.processing.Generated;

@Generated(value = "net.binis.test.prototype.TestPrototype", comments = "Test")
public class TestImpl implements Test {

    protected TestType type;

    public TestImpl() {
    }

    public TestType getType() {
        return type;
    }

    public void setType(TestType type) {
        this.type = type;
    }
}
