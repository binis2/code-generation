package net.binis.codegen.compiler;

import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.compiler.base.BaseJavaCompilerObject;

import static net.binis.codegen.tools.Reflection.getFieldValue;
import static net.binis.codegen.tools.Reflection.loadClass;

@Slf4j
public class CGTree extends BaseJavaCompilerObject {

    public static Class theClass() {
        return loadClass("com.sun.tools.javac.tree.JCTree");
    }

    public CGTree(Object instance) {
        super();
        this.instance = instance;
    }

    public CGType getType() {
        return new CGType(getFieldValue(instance, "type"));
    }

    @Override
    protected void init() {
        cls = theClass();
    }
}
