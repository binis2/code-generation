/*Generated code by Binis' code generator.*/
package net.binis.codegen;

public class BaseImpl implements Base {

    protected Long id;

    public BaseImpl() {
    }

    public <T> T as(Class<T> cls) {
        return cls.cast(this);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
