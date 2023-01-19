package net.binis.codegen;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.annotation.Default;
import net.binis.codegen.annotation.type.GenerationStrategy;

@CodePrototype(strategy = GenerationStrategy.IMPLEMENTATION)
public interface TestStrategy {
    long long1(Object param1);
    String string1(long param1, String param2);
    short short1();
    char char1();
    boolean bool1();
    byte byte1();
    byte int1();
    float float1();
    double double1();
    void test();

}