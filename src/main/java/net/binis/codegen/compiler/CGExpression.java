package net.binis.codegen.compiler;

import lombok.extern.slf4j.Slf4j;

import static net.binis.codegen.tools.Reflection.loadClass;

@Slf4j
public class CGExpression extends CGTree {

    public CGExpression(Object instance) {
        super(instance);
    }

    public static Class theClass() {
        return loadClass("com.sun.tools.javac.tree.JCTree$JCExpression");
    }

    @Override
    protected void init() {
        cls = CGExpression.theClass();
    }
}
