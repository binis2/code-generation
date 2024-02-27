/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import javax.annotation.processing.Generated;

@Generated(value = "net.binis.codegen.prototype.BasePrototype", comments = "Base")
public class BaseImpl implements Base {

    protected TestEnum type;

    public BaseImpl() {
    }

    public TestEnum getType() {
        return type;
    }

    public void setType(TestEnum type) {
        this.type = type;
    }
}
