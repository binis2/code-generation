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

import static java.util.Objects.isNull;
import static net.binis.codegen.tools.Reflection.*;

@Slf4j
public class CGSymtab extends JavaCompilerObject {

    protected static CGSymtab inst;

    public static Class theClass() {
        return loadClass("com.sun.tools.javac.code.Symtab");
    }

    public CGSymtab() {
        super();
        instance = invokeStatic("instance", cls, context);
    }

    @Override
    protected void init() {
        cls = theClass();
    }

    protected static CGSymtab instance() {
        if (isNull(inst)) {
            inst = new CGSymtab();
        }
        return inst;
    }

    public static CGType type(String s) {
        return new CGType(invoke("enterClass", instance().instance, s));
    }

    public static CGType byteType() {
        return new CGType(getFieldValue(instance().instance, "byteType"));
    }

    public static CGType charType() {
        return new CGType(getFieldValue(instance().instance, "charType"));
    }

    public static CGType shortType() {
        return new CGType(getFieldValue(instance().instance, "shortType"));
    }

    public static CGType intType() {
        return new CGType(getFieldValue(instance().instance, "intType"));
    }

    public static CGType longType() {
        return new CGType(getFieldValue(instance().instance, "longType"));
    }

    public static CGType floatType() {
        return new CGType(getFieldValue(instance().instance, "floatType"));
    }

    public static CGType doubleType() {
        return new CGType(getFieldValue(instance().instance, "doubleType"));
    }

    public static CGType booleanType() {
        return new CGType(getFieldValue(instance().instance, "booleanType"));
    }

    public static CGType voidType() {
        return new CGType(getFieldValue(instance().instance, "voidType"));
    }

}
