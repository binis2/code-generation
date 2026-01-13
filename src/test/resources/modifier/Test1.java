package net.binis.codegen;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.annotation.DefaultString;
import net.binis.codegen.annotation.builder.CodeBuilder;
import net.binis.codegen.objects.Pair;

import java.util.List;

@CodeBuilder
public interface TestPrototype {

    List<Pair<String, List<String>>> test();

}