package net.binis.codegen.compiler;

import lombok.extern.slf4j.Slf4j;

import static net.binis.codegen.tools.Reflection.loadClass;

@Slf4j
public class CGFieldAccess extends CGExpression {

    public CGFieldAccess(Object instance) {
        super(instance);
    }

    @Override
    protected void init() {
        cls = loadClass("com.sun.tools.javac.tree.JCTree$JCLiteral");
    }
}
