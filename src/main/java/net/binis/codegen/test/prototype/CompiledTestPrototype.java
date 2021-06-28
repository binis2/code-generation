package net.binis.codegen.test.prototype;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.test.prototype.base.CompiledBasePrototype;

@CodePrototype
public interface CompiledTestPrototype extends CompiledBasePrototype {

    String title();

}
