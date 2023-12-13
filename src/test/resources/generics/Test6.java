package net.binis.codegen.test;

import net.binis.codegen.annotation.Default;
import net.binis.codegen.annotation.builder.CodeBuilder;
import net.binis.codegen.objects.DefaultPayload;
import net.binis.codegen.test.intf.Generic;

@CodeBuilder
public interface TestPrototype extends Generic<Double> {

}
