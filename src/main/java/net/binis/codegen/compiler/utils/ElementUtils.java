package net.binis.codegen.compiler.utils;

import net.binis.codegen.compiler.*;

import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
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
        } else if (value instanceof Boolean b) {
            return maker.Literal(CGTypeTag.BOOLEAN, b ? 1 : 0);
        } else if (value instanceof Long) {
            return maker.Literal(CGTypeTag.LONG, value);
        } else if (value instanceof Integer) {
            return maker.Literal(CGTypeTag.INT, value);
        } else if (value instanceof Double) {
            return maker.Literal(CGTypeTag.DOUBLE, value);
        } else if (value instanceof Float) {
            return maker.Literal(CGTypeTag.FLOAT, value);
        } else if (value instanceof Character c) {
            return maker.Literal(CGTypeTag.CHAR, (int) c);
        } else if (value instanceof Short) {
            return maker.TypeCast(maker.TypeIdent(CGTypeTag.SHORT), maker.Literal(CGTypeTag.INT, value));
        } else if (value instanceof Byte) {
            return maker.TypeCast(maker.TypeIdent(CGTypeTag.BYTE), maker.Literal(CGTypeTag.INT, value));
        } else if (value instanceof Enum) {
            var symbol = maker.getSymbol(value.getClass().getCanonicalName());
            return maker.Select(maker.QualIdent(symbol), Name.create(value.toString()));
        } else if (value instanceof Class c) {
            var symbol = maker.getSymbol(c.getCanonicalName());
            return maker.Select(maker.QualIdent(symbol), Name.create("class"));
        } else if (value.getClass().isArray()) {
            var length = Array.getLength(value);
            var list = CGList.<CGExpression>nil();
            for (var i = 0; i < length; i++) {
                list.append(calcExpression(maker, Array.get(value, i)));
            }
            return maker.NewArray(null, CGList.nil(), list);
        }

        //TODO: Handle all possible cases.
        return classIdent(maker, value.toString());
    }

    protected static CGExpression classIdent(TreeMaker maker, String className) {
        String[] strings = className.split("\\.");

        CGExpression classNameIdent = maker.Ident(Name.create(strings[0]));

        for (int i = 1; i < strings.length; i++) {
            classNameIdent = maker.Select(classNameIdent, Name.create(strings[i]));
        }

        return classNameIdent;
    }


}