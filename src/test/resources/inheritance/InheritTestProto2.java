package net.binis.codegen.proto2;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.proto.ParentPrototype;

import java.util.List;

@CodePrototype(generateImplementation = false)
public interface Parent2Prototype extends ParentPrototype {
    String title();

    List<String> list();

}