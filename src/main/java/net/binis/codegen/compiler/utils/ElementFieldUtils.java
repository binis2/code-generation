package net.binis.codegen.compiler.utils;

import net.binis.codegen.compiler.*;

import javax.lang.model.element.Element;

import static java.util.Objects.nonNull;

public class ElementFieldUtils extends ElementUtils {

    public static CGVariableDecl addField(Element element, String name, Class<?> type, long flags, CGExpression init) {
        var maker = TreeMaker.create();
        var declaration = getDeclaration(element, maker);
        var def = maker.VarDef(CGVarSymbol.create(flags, CGName.create(name), CGSymtab.type(type.getCanonicalName()), declaration.getSymbol()), nonNull(init) ? init : null);
        declaration.getDefs().append(def);
        return def;
    }

    public static CGVariableDecl addField(Element element, String name, Class<?> type, long flags) {
        return addField(element, name, type, flags, null);
    }



}
