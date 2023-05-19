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

import javax.lang.model.element.Element;

public class ElementUtils {

    protected static CGDeclaration getDeclaration(Element element) {
        var maker = TreeMaker.create();
        return getDeclaration(element, maker);
    }

    protected static CGDeclaration getDeclaration(Element element, TreeMaker maker) {
        return switch (element.getKind()) {
            case CLASS, ENUM, INTERFACE -> CGClassDeclaration.create(maker.getTrees(), element);
            case METHOD -> CGMethodDeclaration.create(maker.getTrees(), element);
            case FIELD -> CGVariableDecl.create(maker.getTrees(), element);
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

    public static CGExpression chainDotsString(JavaCompilerObject node, String elems) {
        return chainDots(node, null, null, elems.split("\\."));
    }

    public static String getSymbolFullName(Element element) {
        var symbol = new CGSymbol(element);
        if (symbol.is(CGClassSymbol.theClass())) {
            return symbol.asClassSymbol().getQualifiedName().toString();
        }
        return element.getSimpleName().toString();
    }


}
