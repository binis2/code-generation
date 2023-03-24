package net.binis.codegen.compiler;

import net.binis.codegen.compiler.base.BaseJavaCompilerObject;

import static net.binis.codegen.tools.Reflection.*;

public class Name extends BaseJavaCompilerObject {

    public static Name create(String name) {
        return new Name(name);
    }

    public static Class theClass() {
        return loadClass("com.sun.tools.javac.util.Name");
    }

    public Name(String name) {
        super();
        instance = invoke("fromString", instance, name);
    }

    @Override
    protected void init() {
        cls = loadClass("com.sun.tools.javac.util.Names");
        instance = invokeStatic("instance", cls, context);
    }
}
