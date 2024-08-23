/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.annotation.Ignore;
import javax.annotation.processing.Generated;

@Generated(value = "net.binis.codegen.BasePrototype", comments = "Base")
public class BaseImpl implements Base {

    @Ignore(forSerialization = true)
    protected transient Long id;

    public BaseImpl() {
    }

    @Ignore(forSerialization = true)
    public Long getId() {
        return id;
    }

    @Ignore(forSerialization = true)
    public void setId(Long id) {
        this.id = id;
    }
}
