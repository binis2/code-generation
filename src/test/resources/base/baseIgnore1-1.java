/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.annotation.Ignore;
import javax.annotation.processing.Generated;

@Generated(value = "net.binis.codegen.BasePrototype", comments = "BaseImpl")
public interface Base {
    @Ignore(forSerialization = true)
    Long getId();

    @Ignore(forSerialization = true)
    void setId(Long id);
}
