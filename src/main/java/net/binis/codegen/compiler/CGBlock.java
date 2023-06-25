package net.binis.codegen.compiler;

import static net.binis.codegen.tools.Reflection.loadClass;

public class CGBlock extends CGStatement {
    public CGBlock(Object instance) {
        super(instance);
    }

    public static Class theClass() {
        return loadClass("com.sun.tools.javac.tree.JCTree$JCBlock");
    }

    @Override
    protected void init() {
        cls = theClass();
    }

}
