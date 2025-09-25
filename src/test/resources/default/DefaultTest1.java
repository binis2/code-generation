package net.binis.codegen;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.annotation.ForImplementation;
import net.binis.codegen.factory.CodeFactory;
import net.binis.codegen.map.Mapper;

import java.util.Map;

@CodePrototype
public interface EdgeMessagePrototype {

    Map<String, Object> payload();

    default EdgeMessagePrototype fromJson(String message) {
        var instance = CodeFactory.create(EdgeMessagePrototype.class);
        return Mapper.convert(message, instance);
    }

    static EdgeMessagePrototype json(String message) {
        var instance = CodeFactory.create(EdgeMessagePrototype.class);
        return Mapper.convert(message, instance);
    }

    @ForImplementation
    static EdgeMessagePrototype jsonForClass(String message) {
        var instance = CodeFactory.create(EdgeMessagePrototype.class);
        return Mapper.convert(message, instance);
    }


}