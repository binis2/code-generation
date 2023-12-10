/*Generated code by Binis' code generator.*/
package net.binis.codegen.test;

import javax.annotation.processing.Generated;

@Generated(value = "net.binis.codegen.test.ExtendedPrototype", comments = "Extended")
public class ExtendedImpl implements Extended {

    protected String extended = "asd";

    public ExtendedImpl() {
    }

    public String getDoubleExtended() {
        return getExtended() + getExtended();
    }

    public String getExtended() {
        return this.extended;
    }

    public void setExtended(String extended) {
        this.extended = extended;
    }
}
