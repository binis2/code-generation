package net.binis.codegen.compiler;

/*-
 * #%L
 * code-generator
 * %%
 * Copyright (C) 2021 - 2026 Binis Belev
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

import lombok.extern.slf4j.Slf4j;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static net.binis.codegen.tools.Reflection.*;

@Slf4j
public class CGLambda extends CGExpression {

    protected CGList<CGVariableDecl> params;

    public CGLambda(Object instance) {
        super(instance);
    }

    @Override
    protected void init() {
        cls = loadClass("com.sun.tools.javac.tree.JCTree$JCLambda");
    }

    public CGExpression getExpression() {
        var value = invoke("getExpression", instance);
        return nonNull(value) ? new CGExpression(value) : null;
    }

    public CGBlock getBody() {
        var body = getFieldValue(instance, "body");
        return nonNull(body) ? new CGBlock(body) : null;
    }

    public void setBody(CGBlock body) {
        setFieldValue(instance, "body", nonNull(body) ? body.getInstance() : null);
    }

    public CGList<CGVariableDecl> getParameters() {
        if (isNull(params)) {
            params = new CGList<>(getFieldValue(instance, "params"), this::onModify, CGVariableDecl.class);
        }
        return params;
    }

    protected void onModify(CGList<CGVariableDecl> list) {
        setFieldValue(instance, "params", list.getInstance());
        params = list;
    }



}
