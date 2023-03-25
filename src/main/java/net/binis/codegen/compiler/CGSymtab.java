package net.binis.codegen.compiler;

import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.compiler.base.BaseJavaCompilerObject;

import static net.binis.codegen.tools.Reflection.*;

@Slf4j
public class CGSymtab extends BaseJavaCompilerObject {

    public static Class theClass() {
        return loadClass("com.sun.tools.javac.code.Symtab");
    }

    public CGSymtab() {
        super();
        instance = invokeStatic("instance", cls, context);
    }

    @Override
    protected void init() {
        cls = theClass();
    }

    public CGType enterClass(String s) {
        return new CGType(invoke("enterClass", s));
    }

}
