package net.binis.codegen;

import net.binis.codegen.annotation.CodeSnippet;
import net.binis.codegen.annotation.CodeFieldAnnotations;
import net.binis.codegen.annotation.Default;

import java.util.Map;

@CodeSnippet
public interface PropertiablePrototype {

    @Default("new java.util.HashMap<>()")
    @CodeFieldAnnotations("java.lang.Deprecated")
    Map<String, Object> properties();

}