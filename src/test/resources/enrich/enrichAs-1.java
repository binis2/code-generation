/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import javax.annotation.processing.Generated;

@Generated(value = "TestPrototype", comments = "TestImpl")
public interface Test {
    <T> T as(Class<T> cls);

    String getTitle();

    void setTitle(String title);
}
