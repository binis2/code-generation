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

import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.tools.Reflection;

import static java.util.Objects.nonNull;
import static net.binis.codegen.tools.Reflection.loadClass;

@Slf4j
public class CGVarSymbol extends CGSymbol {

    public static Class theClass() {
        return loadClass("com.sun.tools.javac.code.Symbol$VarSymbol");
    }

    @SuppressWarnings("unchecked")
    public static CGVarSymbol create(long flags, CGName name, CGType type, CGSymbol owner) {
        return new CGVarSymbol(Reflection.instantiate(theClass(), flags, name.getInstance(), type.getInstance(), owner.getInstance()));
    }

    public CGVarSymbol(Object instance) {
        super(instance);
    }

    public String getVariableType() {
        return trimGenerics(Reflection.getFieldValue(instance, "type").toString());
    }

    public String getVariableFullType() {
        return Reflection.getFieldValue(instance, "type").toString();
    }

    protected String trimGenerics(String type) {
        var idx = type.indexOf('<');
        if (idx > 0) {
            return type.substring(0, idx);
        }
        return type;
    }

    public String getVariableSimpleType() {
        var type = Reflection.getFieldValue(instance, "type");
        if (nonNull(type)) {
            var tsym = Reflection.getFieldValue(type, "tsym");
            if (nonNull(tsym)) {
                return Reflection.getFieldValue(tsym, "name").toString();
            }
        }
        return type.toString();
    }


    @Override
    protected void init() {
        cls = theClass();
    }

}
