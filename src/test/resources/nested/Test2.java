package net.binis.codegen.test.objects;

import net.binis.codegen.annotation.builder.CodeRequest;
import net.binis.codegen.options.HiddenCreateMethodOption;

@CodeRequest
public interface TestRequestPrototype {

    String name();

    String value();

    String numbers();

    SubRequestPrototype sub();

    @CodeRequest(options = {HiddenCreateMethodOption.class})
    interface SubRequestPrototype {

        String value();

    }

}
