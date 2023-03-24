package net.binis.codegen.compiler;

import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.compiler.base.BaseJavaCompilerObject;

import static net.binis.codegen.tools.Reflection.loadClass;

@Slf4j
public class CGType extends BaseJavaCompilerObject {

    public static Class theClass() {
        return loadClass("com.sun.tools.javac.code.Type");
    }

    public CGType(Object instance) {
        super();
        this.instance = instance;
    }

    @Override
    protected void init() {
        cls = theClass();
    }

    @Override
    public String toString() {
        return instance.toString();
    }

}
