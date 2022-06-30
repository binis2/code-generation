package net.binis.codegen;

import jdk.jfr.Description;
import jdk.jfr.Label;
import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.annotation.ForInterface;

@ForInterface
@SuppressWarnings("test")
@ForInterface
@Label("test")
@CodePrototype
public interface TestPrototype {

    @Description("description")
    String title();

    @ForInterface
    @Description("description")
    String subtitle();

}