/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import javax.annotation.processing.Generated;

@Generated(value = "BasePrototype", comments = "BaseImpl")
public interface Base {

    <T> T as(Class<T> cls);

    Long getId();

    void setId(Long id);
}
