/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.objects.base.enumeration.CodeEnumImpl;
import javax.annotation.processing.Generated;

@Generated(value = "net.binis.codegen.TestPrototype", comments = "Test")
@net.binis.codegen.annotation.Generated(by = "net.binis.codegen.TestPrototype")
public class TestImpl extends CodeEnumImpl implements Test {

    protected final String title;

    public TestImpl(int $ordinal, String $name, String title) {
        super($ordinal, $name);
        this.title = title;
    }

    public boolean equals(Object o) {
        return super.equals(o);
    }

    public int hashCode() {
        return super.hashCode();
    }

    @Override()
    public String toString() {
        return "Test(title = " + title + ")";
    }
}
