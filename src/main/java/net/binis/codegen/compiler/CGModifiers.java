package net.binis.codegen.compiler;

import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.compiler.base.BaseJavaCompilerObject;

import static java.util.Objects.isNull;
import static net.binis.codegen.tools.Reflection.*;

@Slf4j
public class CGModifiers extends BaseJavaCompilerObject {

    protected final CGClassDeclaration declaration;
    protected CGList<CGAnnotation> annotations;

    public CGModifiers(CGClassDeclaration declaration) {
        super();
        this.declaration = declaration;
        instance = invoke("getModifiers", declaration.getInstance());
    }

    @Override
    protected void init() {
        cls = loadClass("com.sun.tools.javac.tree.JCTree$JCModifiers");
    }

    public CGList<CGAnnotation> getAnnotations() {
        if (isNull(annotations)) {
            annotations = new CGList<>(invoke("getAnnotations", instance), this::onModify);
        }
        return annotations;
    }

    private void onModify(CGList<CGAnnotation> list) {
        setFieldValue(instance, "annotations", list.getInstance());
        annotations = list;
    }

    public void setAnnotations(CGList<CGAnnotation> list) {
        onModify(list);
    }
}
