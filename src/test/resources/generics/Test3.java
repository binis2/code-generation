package net.binis.codegen.test;

import net.binis.codegen.annotation.Default;
import net.binis.codegen.annotation.builder.CodeBuilder;
import net.binis.codegen.objects.DefaultPayload;

@CodeBuilder
public interface TestPrototype extends GenericPrototype<TestPayloadPrototype> {

//    @Default("null")
//    DefaultPayload payload();

}
