/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.modifier.BaseModifier;
import javax.annotation.processing.Generated;

@Generated(value = "net.binis.codegen.TestPrototype", comments = "TestImpl")
public interface Test {
    String getTest();
    String getTitle();

    void setTest(String test);
    void setTitle(String title);

    Test.Modify with();

    interface Fields<T> {
        T test(String test);
        T title(String title);
    }

    interface Modify extends Test.Fields<Test.Modify>, BaseModifier<Test.Modify, Test> {
    }
}
