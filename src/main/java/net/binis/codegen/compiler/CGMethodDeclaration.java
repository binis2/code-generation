package net.binis.codegen.compiler;

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

import com.sun.source.util.Trees;
import lombok.extern.slf4j.Slf4j;

import javax.lang.model.element.Element;

import static java.util.Objects.isNull;
import static net.binis.codegen.tools.Reflection.*;

@Slf4j
public class CGMethodDeclaration extends CGDeclaration {

    protected CGList<CGVariableDecl> params;

    public static CGMethodDeclaration create(Trees trees, Element element) {
        return new CGMethodDeclaration(trees, element);
    }

    public CGMethodDeclaration(Trees trees, Element element) {
        super(trees, element);
    }

    public CGList<CGVariableDecl> getParameters() {
        if (isNull(params)) {
            params = new CGList<>(getFieldValue(instance, "params"), this::onModify, CGVariableDecl.class);
        }
        return params;
    }

    @Override
    protected void init() {
        cls = loadClass("com.sun.tools.javac.tree.JCTree$JCMethodDecl");
    }

    private void onModify(CGList<CGVariableDecl> list) {
        setFieldValue(instance, "annotations", list.getInstance());
        params = list;
    }


}
