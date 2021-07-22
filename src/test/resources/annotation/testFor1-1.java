/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import org.springframework.transaction.annotation.Transactional;

public interface Test {

    Long getId();

    void setId(Long id);

    void test();

    @Transactional
    void test2();
}
