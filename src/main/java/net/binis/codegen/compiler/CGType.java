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

import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.compiler.base.JavaCompilerObject;

import javax.lang.model.type.TypeKind;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static net.binis.codegen.tools.Reflection.*;
import static net.binis.codegen.tools.Tools.withRes;

@Slf4j
public class CGType extends JavaCompilerObject {

    public static Class theClass() {
        return loadClass("com.sun.tools.javac.code.Type");
    }

    public CGType(Object instance) {
        super();
        this.instance = instance;
    }

    public CGList<CGType> getTypeArguments() {
        return new CGList<>(invoke("getTypeArguments", instance), null, CGType.class);
    }

    @Override
    protected void init() {
        cls = theClass();
    }

    @Override
    public String toString() {
        return instance.toString();
    }

    public String toSymbolString() {
        var field = findField(instance.getClass(), "sym");
        if (nonNull(field)) {
            var sym = getFieldValue(field, instance);
            if (nonNull(sym)) {
                return withRes(invoke("getQualifiedName", sym), Object::toString);
            }
        }
        return null;
    }

    public boolean isErrorType() {
        TypeKind kind = invoke("getKind", instance);
        return TypeKind.ERROR.equals(kind);
    }

    public Class toClass() {
        var cls = loadClass(toString());
        if (isNull(cls)) {
            cls = loadClass(toSymbolString());
        }
        return cls;
    }

}
