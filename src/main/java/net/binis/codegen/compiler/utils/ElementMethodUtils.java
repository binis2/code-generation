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

import com.github.javaparser.ast.Node;
import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.compiler.*;

import javax.lang.model.element.Element;
import java.util.List;

import static java.util.Objects.nonNull;

@Slf4j
public class ElementMethodUtils extends ElementUtils {

    public static CGMethodDeclaration addMethod(Element element, String name, Class<?> returnType, long flags, List<CGVariableDecl> params, CGBlock body) {
        var maker = TreeMaker.create();
        var declaration = getDeclaration(element, maker);
        var def = maker.MethodDef(maker.Modifiers(flags), CGName.create(name), classToExpression(returnType), CGList.nil(CGTypeParameter.class), CGList.from(params, CGVariableDecl.class), CGList.nil(CGExpression.class), body, null);
        declaration.getDefs().append(def);
        return def;
    }

    public static CGMethodDeclaration addConstructor(Element element, long flags, List<CGVariableDecl> params) {
        var maker = TreeMaker.create();
        var declaration = getDeclaration(element, maker);
        return addConstructor((CGClassDeclaration) declaration, flags, params);
    }

    public static CGMethodDeclaration addConstructor(CGClassDeclaration cls, long flags, List<CGVariableDecl> params) {
        var body = createBlock(createStatement(createMethodInvocation("super")));
        var maker = TreeMaker.create();
        var def = maker.MethodDef(maker.Modifiers(flags), CGName.create("<init>"), null, CGList.nil(CGTypeParameter.class), CGList.from(params, CGVariableDecl.class), CGList.nil(CGExpression.class), body, null);
        cls.getDefs().append(def);
        return def;
    }


    public static CGExpression createMethodInvocation(String methodName, CGExpression... params) {
        var maker = TreeMaker.create();
        var method = chainDotsString(maker, methodName);
        return maker.Apply(CGList.nil(CGExpression.class), method, CGList.from(params, CGExpression.class));
    }

    public static CGExpression createStaticMethodInvocation(Class<?> cls, String methodName, CGExpression... params) {
        return createStaticMethodInvocation(cls.getCanonicalName(), methodName, params);
    }

    public static CGExpression createStaticMethodInvocation(String cls, String methodName, CGExpression... params) {
        var maker = TreeMaker.create();
        var method = chainDotsString(maker, cls + "." + methodName);
        return maker.Apply(CGList.nil(CGExpression.class), method, CGList.from(params, CGExpression.class));
    }

    public static CGMethodInvocation createClassMethodInvocation(Class<?> cls, String methodName, CGExpression... params) {
        var maker = TreeMaker.create();
        var method = maker.Select(toType(cls), maker.toName(methodName));
        return maker.Apply(CGList.nil(CGExpression.class), method, CGList.from(params, CGExpression.class));
    }

    public static CGMethodInvocation createClassMethodInvocation(String cls, String methodName, CGExpression... params) {
        var maker = TreeMaker.create();
        var method = chainDotsString(maker, cls + ".class." + methodName);
        return maker.Apply(CGList.nil(CGExpression.class), method, CGList.from(params, CGExpression.class));
    }

    public static CGBlock createBlock() {
        return TreeMaker.create().Block(0, CGList.nil(CGStatement.class));
    }

    public static CGBlock createBlock(CGStatement... statements) {
        return TreeMaker.create().Block(0, CGList.from(statements, CGStatement.class));
    }

    public static CGStatement createStatement(CGExpression expr) {
        expr.setType(CGSymtab.voidType());
        return TreeMaker.create().Call(expr);
    }


    public static CGVariableDecl createParameter(Class<?> cls, String name) {
        var maker = TreeMaker.create();

        return maker.VarDef(maker.Modifiers(CGFlags.PARAMETER), maker.toName(name), classToExpression(cls), null);
    }


    public static boolean paramsMatch(Element e, List<String> list) {
        if (getDeclaration(e) instanceof CGMethodDeclaration decl) {
            var params = decl.getParameters();
            if (params.size() == list.size()) {
                for (int i = 0; i < params.size(); i++) {
                    if (!params.get(i).getFullVariableType().equals(list.get(i))) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
}
