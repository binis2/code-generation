package net.binis.codegen.compiler;

import com.sun.source.util.Trees;
import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.compiler.base.BaseJavaCompilerObject;

import javax.lang.model.element.Element;

import static java.util.Objects.isNull;
import static net.binis.codegen.tools.Reflection.loadClass;

@Slf4j
public class CGClassDeclaration extends BaseJavaCompilerObject {

    private CGModifiers modifiers;

    public static CGClassDeclaration create(Trees trees, Element element) {
        return new CGClassDeclaration(trees, element);
    }

    @SuppressWarnings("unchecked")
    public CGClassDeclaration(Trees trees, Element element) {
        super();
        instance = trees.getTree(element);
        if (!cls.isAssignableFrom(instance.getClass())) {
            log.error("Unable to get class declaration!");
        }
    }

    @Override
    protected void init() {
        cls = loadClass("com.sun.tools.javac.tree.JCTree$JCClassDecl");
    }

    public CGModifiers getModifiers() {
        if (isNull(modifiers)) {
            modifiers = new CGModifiers(this);
        }
        return modifiers;
    }
}
