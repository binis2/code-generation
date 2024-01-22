package net.binis.codegen.compiler;

/*-
 * #%L
 * code-generator
 * %%
 * Copyright (C) 2021 - 2024 Binis Belev
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

import com.sun.source.util.Trees;
import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.compiler.utils.ElementUtils;

import javax.lang.model.element.Element;

import static java.util.Objects.nonNull;
import static net.binis.codegen.tools.Reflection.getFieldValue;
import static net.binis.codegen.tools.Reflection.loadClass;

@Slf4j
public class CGVariableDecl extends CGDeclaration {

    public static CGVariableDecl create(Trees trees, Element element) {
        return new CGVariableDecl(trees, element);
    }

    public CGVariableDecl(Trees trees, Element element) {
        super(trees, element);
    }

    public CGVariableDecl(Object instance) {
        super(instance);
    }

    public String getVariableType() {
        return getFieldValue(instance, "vartype").toString();
    }

    public String getFullVariableType() {
        return ElementUtils.getSymbolFullName(getSymbol().getElement());
    }

    public CGExpression getVarType() {
        var type = getFieldValue(instance, "vartype");
        return nonNull(type) ? new CGExpression(type) : null;
    }

    public static Class theClass() {
        return loadClass("com.sun.tools.javac.tree.JCTree$JCVariableDecl");
    }

    public CGExpression getInitializer() {
        var init = getFieldValue(instance, "init");
        return nonNull(init) ? new CGExpression(init) : null;
    }

    @Override
    protected void init() {
        cls = CGVariableDecl.theClass();
    }
}
