package net.binis.codegen.compiler;

import lombok.extern.slf4j.Slf4j;

import static net.binis.codegen.tools.Reflection.invoke;
import static net.binis.codegen.tools.Reflection.loadClass;

@Slf4j
public class CGAssign extends CGExpression {

    public static Class theClass() {
        return loadClass("com.sun.tools.javac.tree.JCTree$JCAssign");
    }

    public CGAssign(Object instance) {
        super(instance);
    }

    public CGExpression getVariable() {
        return new CGExpression(invoke("getVariable", instance));
    }

    public CGExpression getExpression() {
        return new CGExpression(invoke("getExpression", instance));
    }

    @Override
    protected void init() {
        cls = theClass();
    }
}
