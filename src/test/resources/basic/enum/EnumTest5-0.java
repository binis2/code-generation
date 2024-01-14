/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.objects.base.enumeration.CodeEnumImpl;
import javax.annotation.processing.Generated;

@Generated(value = "net.binis.codegen.TestEnumPrototype", comments = "TestEnum")
public class TestEnumImpl extends CodeEnumImpl implements TestEnum {

    protected final boolean flag;

    protected final String title;

    protected final int value;

    public TestEnumImpl(int $ordinal, String $name, String title, int value, boolean flag) {
        super($ordinal, $name);
        this.title = title;
        this.value = value;
        this.flag = flag;
    }

    public boolean equals(Object o) {
        return super.equals(o);
    }

    public String getTitle() {
        return "Test" + title;
    }

    public int getValue() {
        return value;
    }

    public int hashCode() {
        return super.hashCode();
    }

    public boolean isFlag() {
        return flag;
    }
}
