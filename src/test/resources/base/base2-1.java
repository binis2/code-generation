/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import javax.annotation.processing.Generated;

@Generated(value = "net.binis.codegen.BasePrototype", comments = "BaseImpl")
public interface Base {
    Long getId();

    void setId(Long id);

    interface Fields<T> {
        T id(Long id);
    }
}
