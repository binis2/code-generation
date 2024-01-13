/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.objects.base.enumeration.CodeEnumImpl;
import lombok.Getter;
import javax.annotation.processing.Generated;

@Generated(value = "net.binis.codegen.TestPrototype", comments = "Test")
public class TestImpl extends CodeEnumImpl implements Test {

    public static final String CONSTANT = "const";

    @Getter
    protected final boolean check;

    protected final String label;

    @Getter
    protected final int value;

    public TestImpl(int $ordinal, String $name, String label, boolean check, int value) {
        super($ordinal, $name);
        this.label = label;
        this.check = check;
        this.value = value;
    }

    public boolean equals(Object o) {
        return super.equals(o);
    }

    public String getLabel() {
        return label;
    }

    public int hashCode() {
        return super.hashCode();
    }
}
