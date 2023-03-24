package net.binis.codegen.compiler;

import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.compiler.base.BaseJavaCompilerObject;

import static net.binis.codegen.tools.Reflection.loadClass;

@Slf4j
public class CGExpression extends BaseJavaCompilerObject {

    public CGExpression(Object instance) {
        super();
        this.instance = instance;
    }

    public static Class theClass() {
        return loadClass("com.sun.tools.javac.tree.JCTree$JCExpression");
    }

    @Override
    protected void init() {
        cls = CGExpression.theClass();
    }
}
