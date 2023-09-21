package net.binis.codegen;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.objects.prototype.BaseCompiledPrototype;

@CodePrototype(interfaceSetters = false)
public interface TestPrototype extends BaseCompiledPrototype {
    Long other();
}