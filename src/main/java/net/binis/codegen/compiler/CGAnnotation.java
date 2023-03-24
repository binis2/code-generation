package net.binis.codegen.compiler;

import lombok.extern.slf4j.Slf4j;

import static java.util.Objects.isNull;
import static net.binis.codegen.tools.Reflection.*;

@Slf4j
public class CGAnnotation extends CGExpression {

    protected CGList<CGExpression> arguments;
    protected CGTree annotationType;

    public CGAnnotation(Object instance) {
        super(instance);
    }

    @Override
    protected void init() {
        cls = loadClass("com.sun.tools.javac.tree.JCTree$JCAnnotation");
    }

    public CGList<CGExpression> getArguments() {
        if (isNull(arguments)) {
            arguments = new CGList<>(invoke("getArguments", instance), this::onModify);
        }
        return arguments;
    }

    public CGTree getAnnotationType() {
        if (isNull(annotationType)) {
            annotationType = new CGTree(invoke("getAnnotationType", instance));
        }
        return annotationType;
    }

    private void onModify(CGList<CGExpression> list) {
        setFieldValue(instance, "args", list.getInstance());
        arguments = list;
    }
}
