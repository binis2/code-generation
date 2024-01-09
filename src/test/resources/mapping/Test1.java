package net.binis.codegen;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.annotation.DefaultString;
import net.binis.codegen.map.annotation.CodeMapping;

@CodePrototype
public interface TestPrototype {

    @CodeMapping(ignore = true)
    String title();

}