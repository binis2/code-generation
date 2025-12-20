package net.binis.codegen;

import net.binis.codegen.annotation.CodeImplementation;
import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.annotation.DefaultString;

@CodePrototype
public interface TestPrototype {

    Integer title();

    @CodeImplementation("""
            if (title == null) {
                return "empty";
            } else {
                return title.toString();
            }
            """)
    default String getTitle() {
        return null;    }
}