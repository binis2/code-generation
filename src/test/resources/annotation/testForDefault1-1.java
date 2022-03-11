/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import javax.annotation.processing.Generated;

@Generated(value = "TestPrototype", comments = "TestImpl")
public interface Test {
    boolean isTest();

    default boolean isTestable() {
        return isTest();
    }

    void setTest(boolean test);
}
