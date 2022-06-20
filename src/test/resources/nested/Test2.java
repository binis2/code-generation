package net.binis.codegen.test.objects;

import net.binis.codegen.annotation.builder.CodeRequest;

@CodeRequest
public interface TestRequestPrototype {

    String name();

    String value();

    String numbers();

    SubRequestPrototype sub();

    @CodeRequest
    interface SubRequestPrototype {

        String value();

    }

}
