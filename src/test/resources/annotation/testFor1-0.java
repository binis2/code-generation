/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import org.springframework.transaction.annotation.Transactional;

public class TestImpl implements Test {

    protected Long id = 1L;

    public TestImpl() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Transactional
    public void test() {
    }

    public void test2() {
    }
}
