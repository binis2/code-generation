/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import org.springframework.transaction.annotation.Transactional;
import javax.annotation.processing.Generated;

@Generated(value = "TestPrototype", comments = "TestImpl")
public interface Test {
    Long getId();

    void setId(Long id);

    void test();

    @Transactional
    void test2();
}
