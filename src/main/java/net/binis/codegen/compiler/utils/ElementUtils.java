package net.binis.codegen.compiler.utils;

import net.binis.codegen.compiler.*;

import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Map;

public class ElementUtils {

    public static void addClassAnnotation(Element element, Class<? extends Annotation> annotation, Map<String, Object> attributes) {
        var maker = TreeMaker.create();

        var decl = CGClassDeclaration.create(maker.getTrees(), element);

        var list = CGList.<CGExpression>nil();
        for (var attr : attributes.entrySet()) {
            list = list.append(maker.Assign(maker.Ident(Name.create(attr.getKey())), calcExpression(maker, attr.getValue())));
        }

        var ann = maker.Annotation(maker.QualIdent(maker.getSymbol(annotation.getCanonicalName())), list);

        decl.getModifiers().getAnnotations().append(ann);
    }

    public static void removeClassAnnotation(Element element, Class<? extends Annotation> annotation) {
        var maker = TreeMaker.create();
        var decl = CGClassDeclaration.create(maker.getTrees(), element);
        var list = CGList.<CGAnnotation>nil();
        for (var it = decl.getModifiers().getAnnotations().iterator(CGAnnotation.class); it.hasNext(); ) {
            var ann = it.next();
            if (!ann.getAnnotationType().getType().toString().equals(annotation.getCanonicalName())) {
                list = list.append(ann);
            }
        }
        decl.getModifiers().setAnnotations(list);
    }

    protected static CGExpression calcExpression(TreeMaker maker, Object value) {
        if (value instanceof String) {
            return maker.Literal(CGTypeTag.CLASS, value);
        }

        //TODO: Handle all possible cases.
        return null;
    }


}
