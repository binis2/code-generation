package net.binis.codegen.proto;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.proto.base.InheirTestPrototype;

@CodePrototype(generateImplementation = false)
public interface ParentPrototype {

    @SuppressWarnings("unused")
    InheirTestPrototype parent();
    int data();

}