package net.binis.codegen;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.annotation.Default;
import net.binis.codegen.annotation.ForImplementation;
import net.binis.codegen.annotation.ForInterface;
import org.springframework.transaction.annotation.Transactional;

@CodePrototype
public interface TestPrototype {
    @Default("1L")
    Long id();

    @ForImplementation
    @Transactional
    default void test() {};

    @ForInterface
    @Transactional
    default void test2() {};

}