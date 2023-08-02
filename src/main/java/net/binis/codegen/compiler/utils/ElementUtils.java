package net.binis.codegen.compiler.utils;

/*-
 * #%L
 * code-generator
 * %%
 * Copyright (C) 2021 - 2023 Binis Belev
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import net.binis.codegen.compiler.*;
import net.binis.codegen.compiler.base.JavaCompilerObject;
import net.binis.codegen.exception.GenericCodeGenException;
import net.binis.codegen.factory.CodeFactory;

import javax.lang.model.element.Element;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.nonNull;
import static net.binis.codegen.tools.Reflection.invokeStatic;

public class ElementUtils {

    public static Map<String, Class<? extends JavaCompilerObject>> CLASS_MAP = initClassMap();

    public static CGDeclaration getDeclaration(Element element) {
        var maker = TreeMaker.create();
        return getDeclaration(element, maker);
    }

    protected static CGDeclaration getDeclaration(Element element, TreeMaker maker) {
        return switch (element.getKind()) {
            case CLASS, ENUM, INTERFACE, ANNOTATION_TYPE -> CGClassDeclaration.create(maker.getTrees(), element);
            case METHOD, CONSTRUCTOR -> CGMethodDeclaration.create(maker.getTrees(), element);
            case FIELD, PARAMETER -> CGVariableDecl.create(maker.getTrees(), element);
            default -> throw new GenericCodeGenException("Invalid element kind: " + element.getKind().toString());
        };
    }

    protected static CGFieldAccess selfType(CGClassDeclaration decl) {
        var maker = TreeMaker.create();
        var name = decl.getName();
        return maker.Select(maker.Ident(name), decl.toName("class"));
    }

    protected static CGFieldAccess toType(Class<?> cls) {
        var maker = TreeMaker.create();
        return maker.Select(chainDotsString(maker, cls.getCanonicalName()), maker.toName("class"));
    }

    protected static CGExpression chainDots(JavaCompilerObject node, String elem1, String elem2, String... elems) {
        return chainDots(node, -1, elem1, elem2, elems);
    }

    protected static CGExpression chainDots(JavaCompilerObject node, String[] elems) {
        return chainDots(node, -1, null, null, elems);
    }

    protected static CGExpression chainDots(JavaCompilerObject node, int pos, String elem1, String elem2, String... elems) {
        var maker = TreeMaker.create();
        if (pos != -1) {
            maker = maker.at(pos);
        }
        CGExpression e = null;
        if (elem1 != null) {
            e = maker.Ident(node.toName(elem1));
        }
        if (elem2 != null) {
            e = e == null ? maker.Ident(node.toName(elem2)) : maker.Select(e, node.toName(elem2));
        }
        for (var elem : elems) {
            e = e == null ? maker.Ident(node.toName(elem)) : maker.Select(e, node.toName(elem));
        }

        assert e != null;

        return e;
    }

    public static CGExpression chainDotsString(String elems) {
        return chainDots(TreeMaker.create(), null, null, elems.split("\\."));
    }

    public static CGExpression chainDotsString(JavaCompilerObject node, String elems) {
        return chainDots(node, null, null, elems.split("\\."));
    }

    public static String getSymbolFullName(Element element) {
        var symbol = new CGSymbol(element);
        if (symbol.is(CGClassSymbol.theClass())) {
            return symbol.asClassSymbol().getQualifiedName().toString();
        } else if (symbol.is(CGVarSymbol.theClass())) {
            return symbol.asVarSymbol().getVariableType();
        }
        return element.getSimpleName().toString();
    }

    public static CGExpression classToExpression(Class<?> cls) {
        var maker = TreeMaker.create();

        if (cls.isPrimitive()) {
            return maker.TypeIdent(primitiveTypeTag(cls));
        } else if (cls.isArray()) {
            return maker.TypeArray(classToExpression(cls.getComponentType()));
        }

        return maker.QualIdent(maker.getSymbol(cls.getCanonicalName()));
    }

    public static CGTypeTag primitiveTypeTag(Class<?> cls) {
        return switch (cls.getName()) {
            case "byte" -> CGTypeTag.BYTE;
            case "char" -> CGTypeTag.CHAR;
            case "short" -> CGTypeTag.SHORT;
            case "long" -> CGTypeTag.LONG;
            case "float" -> CGTypeTag.FLOAT;
            case "int" -> CGTypeTag.INT;
            case "double" -> CGTypeTag.DOUBLE;
            case "boolean" -> CGTypeTag.BOOLEAN;
            case "void" -> CGTypeTag.VOID;
            default -> throw new IllegalStateException("Unexpected value: " + cls.getName());
        };
    }

    public static CGExpression calcExpression(TreeMaker maker, Object value) {
        if (value instanceof CGExpression v) {
            return v;
        } else if (value instanceof String) {
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
        } else if (value instanceof Enum e) {
            var symbol = maker.getSymbol(value.getClass().getCanonicalName());
            return maker.Select(maker.QualIdent(symbol), CGName.create(e.name()));
        } else if (value instanceof Class c) {
            try {
                return maker.Select(maker.TypeIdent(primitiveTypeTag(c)), CGName.create("class"));
            } catch (IllegalStateException e) {
                var symbol = maker.getSymbol(c.getCanonicalName());
                return maker.Select(maker.QualIdent(symbol), CGName.create("class"));
            }
        } else if (value.getClass().isArray()) {
            var length = Array.getLength(value);
            var list = CGList.nil(CGExpression.class);
            for (var i = 0; i < length; i++) {
                list.append(calcExpression(maker, Array.get(value, i)));
            }
            return maker.NewArray(null, CGList.nil(CGExpression.class), list);
        }

        //TODO: Handle all possible cases.
        return classIdent(maker, value.toString());
    }

    protected static CGExpression classIdent(TreeMaker maker, String className) {
        String[] strings = className.split("\\.");

        CGExpression classNameIdent = maker.Ident(CGName.create(strings[0]));

        for (int i = 1; i < strings.length; i++) {
            classNameIdent = maker.Select(classNameIdent, CGName.create(strings[i]));
        }

        return classNameIdent;
    }

    protected static Map<String, Class<? extends JavaCompilerObject>> initClassMap() {
        var result = new HashMap<String, Class<? extends JavaCompilerObject>>();
        registerClass(result, CGVariableDecl.class);
        registerClass(result, CGMethodDeclaration.class);
        return result;
    }

    protected static void registerClass(Map<String, Class<? extends JavaCompilerObject>> map, Class<? extends JavaCompilerObject> registerClass) {
        if (invokeStatic("theClass", registerClass) instanceof Class<?> cls) {
            map.put(cls.getCanonicalName(), registerClass);
            CodeFactory.registerType(cls, params -> CodeFactory.create(registerClass, params));
        }
    }

}
